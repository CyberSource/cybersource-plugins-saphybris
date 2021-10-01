package isv.sap.payment.addon.facade.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Resource;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentModeModel;
import de.hybris.platform.order.PaymentModeService;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import org.springframework.beans.factory.annotation.Autowired;

import isv.cjl.payment.service.executor.PaymentServiceResult;
import isv.sap.payment.addon.facade.AlternativePaymentFacade;
import isv.sap.payment.addon.strategy.AlternativePaymentSaleRequester;
import isv.sap.payment.enums.AlternativePaymentMethod;
import isv.sap.payment.enums.IsvAlternativePaymentStatus;
import isv.sap.payment.enums.PaymentType;
import isv.sap.payment.model.IsvPaymentModeModel;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;
import isv.sap.payment.model.IsvPaymentTransactionModel;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.cjl.payment.constants.PaymentConstants.TransactionStatus.ACCEPT;
import static isv.cjl.payment.enums.PaymentType.ALTERNATIVE_PAYMENT;
import static isv.sap.payment.constants.IsvPaymentConstants.AlternativePaymentsResponseFields.MERCHANT_URL;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class AlternativePaymentFacadeImpl extends AbstractPaymentFacade implements AlternativePaymentFacade
{
    @Resource
    private PaymentModeService paymentModeService;

    @Autowired
    private List<AlternativePaymentSaleRequester> saleRequesters;

    @Override
    public Optional<String> makeSaleRequestForAlternativePayment(final CartModel cart, final String paymentModeCode,
            final Map<String, Object> optionalParameters)
    {
        ServicesUtil.validateParameterNotNullStandardMessage("cart", cart);
        ServicesUtil.validateParameterNotNullStandardMessage("paymentModeCode", paymentModeCode);

        final PaymentModeModel paymentMode = paymentModeService.getPaymentModeForCode(paymentModeCode);
        assertValidPaymentModeClazz(paymentMode);

        final IsvPaymentModeModel isvPaymentMode = (IsvPaymentModeModel) paymentMode;
        assertValidPaymentType(isvPaymentMode);

        final AlternativePaymentMethod alternativePaymentType = isvPaymentMode.getPaymentSubType();
        assertAlternativePaymentSubTypeIsSet(alternativePaymentType);

        final isv.cjl.payment.enums.AlternativePaymentMethod alternativePaymentMethod = isv.cjl.payment.enums.AlternativePaymentMethod
                .valueOf(alternativePaymentType.getCode());
        final PaymentServiceResult paymentServiceResult = AlternativePaymentSaleRequester
                .getSaleRequester(saleRequesters, alternativePaymentMethod).
                        initiateSale(cart, alternativePaymentMethod, getMerchantID(ALTERNATIVE_PAYMENT),
                                optionalParameters);

        final IsvPaymentTransactionEntryModel transaction = paymentServiceResult.getData(TRANSACTION);

        return wasResultSuccessful(transaction) ? of(transaction.getProperties().get(MERCHANT_URL)) : empty();
    }

    @Override
    public boolean validateAlternativePaymentResponse(final AbstractOrderModel cart,
            final String alternativePaymentType)
    {
        final AlternativePaymentMethod type = AlternativePaymentMethod.valueOf(alternativePaymentType);

        return cart.getPaymentTransactions().stream()
                .filter(txn -> txn instanceof IsvPaymentTransactionModel)
                .map(IsvPaymentTransactionModel.class::cast)
                .filter(txn -> PaymentType.ALTERNATIVE_PAYMENT.name().equals(txn.getPaymentProvider()) && txn
                        .getAlternativePaymentMethod().equals(type))
                .flatMap(txn -> txn.getEntries().stream())
                .map(IsvPaymentTransactionEntryModel.class::cast)
                .anyMatch(entry -> isTxnEntryValid(entry, type));
    }

    private static void assertValidPaymentType(final IsvPaymentModeModel isvPaymentMode)
    {
        if (!PaymentType.ALTERNATIVE_PAYMENT.equals(isvPaymentMode.getPaymentType()))
        {
            throw new IllegalStateException("Expected payment type is: ALTERNATIVE_PAYMENT but got "
                    + isvPaymentMode.getPaymentType());
        }
    }

    private static void assertValidPaymentModeClazz(final PaymentModeModel paymentMode)
    {
        if (!(paymentMode instanceof IsvPaymentModeModel))
        {
            throw new IllegalStateException("Expected payment mode type is IsvPaymentModeModel");
        }
    }

    private static void assertAlternativePaymentSubTypeIsSet(final AlternativePaymentMethod alternativePaymentType)
    {
        if (alternativePaymentType == null)
        {
            throw new IllegalArgumentException("IsvPaymentModeModel.paymentSubType should be specified");
        }
    }

    private boolean isTxnEntryValid(final IsvPaymentTransactionEntryModel entry,
            final AlternativePaymentMethod type)
    {
        return ACCEPT.equals(entry.getTransactionStatus()) && typeSpecificCheck(entry, type);
    }

    private static boolean typeSpecificCheck(final IsvPaymentTransactionEntryModel entry,
            final AlternativePaymentMethod type)
    {
        switch (type)
        {
            case APY:
            case AYM:
                return PaymentTransactionType.INITIATE.equals(entry.getType());
            case IDL:
            case MCH:
            case SOF:
                return PaymentTransactionType.SALE.equals(entry.getType());
            case KLI:
                return PaymentTransactionType.AUTHORIZATION.equals(entry.getType()) && isKlarnaAuthAccepted(
                        entry.getProperties().get("status"));
            case WQR:
                return PaymentTransactionType.CHECK_STATUS.equals(entry.getType()) && isWeChatSaleSettled(
                        entry.getProperties().get("apCheckStatusReplyPaymentStatus"));
            default:
                throw new IllegalStateException("Unexpected Alternative Payments type: " + type);
        }
    }

    private static boolean isKlarnaAuthAccepted(final String status)
    {
        return IsvAlternativePaymentStatus.PENDING.getCode().equals(status) || IsvAlternativePaymentStatus.AUTHORIZED
                .getCode().equals(status);
    }

    private static boolean isWeChatSaleSettled(final String status)
    {
        return IsvAlternativePaymentStatus.SETTLED.getCode().equalsIgnoreCase(status);
    }

    protected boolean wasResultSuccessful(final IsvPaymentTransactionEntryModel transaction)
    {
        return wasTxnAccepted(transaction) && isNotBlank(transaction.getProperties().get(MERCHANT_URL));
    }

    private boolean wasTxnAccepted(final IsvPaymentTransactionEntryModel txn)
    {
        return ACCEPT.equals(txn.getTransactionStatus());
    }

    public void setPaymentModeService(final PaymentModeService paymentModeService)
    {
        this.paymentModeService = paymentModeService;
    }

    public void setSaleRequesters(final List<AlternativePaymentSaleRequester> saleRequesters)
    {
        this.saleRequesters = saleRequesters;
    }
}
