package isv.sap.payment.addon.facade.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Resource;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentModeModel;
import de.hybris.platform.order.PaymentModeService;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import org.springframework.beans.factory.annotation.Autowired;

import isv.cjl.payment.service.executor.PaymentServiceResult;
import isv.sap.payment.addon.assertions.AlternativePaymentModeAssertions;
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
    private static final Map<AlternativePaymentMethod, Function<IsvPaymentTransactionEntryModel, Boolean>> TYPE_MAP = Map
            .of(
                    AlternativePaymentMethod.APY, entry -> PaymentTransactionType.INITIATE.equals(entry.getType()),
                    AlternativePaymentMethod.AYM, entry -> PaymentTransactionType.INITIATE.equals(entry.getType()),
                    AlternativePaymentMethod.IDL, entry -> PaymentTransactionType.SALE.equals(entry.getType()),
                    AlternativePaymentMethod.MCH, entry -> PaymentTransactionType.SALE.equals(entry.getType()),
                    AlternativePaymentMethod.SOF, entry -> PaymentTransactionType.SALE.equals(entry.getType()),
                    AlternativePaymentMethod.KLI, entry -> PaymentTransactionType.AUTHORIZATION.equals(entry.getType())
                        && isKlarnaAuthAccepted(entry.getProperties().get("status")),
                    AlternativePaymentMethod.WQR, entry -> PaymentTransactionType.CHECK_STATUS.equals(entry.getType())
                        && isWeChatSaleSettled(entry.getProperties().get("apCheckStatusReplyPaymentStatus"))
            );

    @Resource
    private PaymentModeService paymentModeService;

    @Autowired
    private List<AlternativePaymentSaleRequester> saleRequesters;

    private static boolean isKlarnaAuthAccepted(final String status)
    {
        return IsvAlternativePaymentStatus.PENDING.getCode().equals(status)
                || IsvAlternativePaymentStatus.AUTHORIZED.getCode().equals(status);
    }

    private static boolean isWeChatSaleSettled(final String status)
    {
        return IsvAlternativePaymentStatus.SETTLED.getCode().equalsIgnoreCase(status);
    }

    private static boolean typeSpecificCheck(final IsvPaymentTransactionEntryModel entry,
            final AlternativePaymentMethod type)
    {
        return TYPE_MAP.getOrDefault(type, ent -> {
            throw new IllegalStateException("Unexpected Alternative Payments type: " + type);
        }).apply(entry);
    }

    @Override
    public Optional<String> makeSaleRequestForAlternativePayment(final CartModel cart, final String paymentModeCode,
            final Map<String, Object> optionalParameters)
    {
        ServicesUtil.validateParameterNotNullStandardMessage("cart", cart);
        ServicesUtil.validateParameterNotNullStandardMessage("paymentModeCode", paymentModeCode);

        final PaymentModeModel paymentMode = paymentModeService.getPaymentModeForCode(paymentModeCode);
        AlternativePaymentModeAssertions.assertValidPaymentModeClazz(paymentMode);

        final IsvPaymentModeModel isvPaymentMode = (IsvPaymentModeModel) paymentMode;
        AlternativePaymentModeAssertions.assertValidPaymentType(isvPaymentMode);

        final AlternativePaymentMethod alternativePaymentType = isvPaymentMode.getPaymentSubType();
        AlternativePaymentModeAssertions.assertAlternativePaymentSubTypeIsSet(alternativePaymentType);

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
                .filter(txn -> isAlternativePaymentType(type, txn))
                .flatMap(txn -> txn.getEntries().stream())
                .map(IsvPaymentTransactionEntryModel.class::cast)
                .anyMatch(entry -> isTxnEntryValid(entry, type));
    }

    private boolean isAlternativePaymentType(final AlternativePaymentMethod type, final IsvPaymentTransactionModel txn)
    {
        return PaymentType.ALTERNATIVE_PAYMENT.name().equals(txn.getPaymentProvider()) && txn
                .getAlternativePaymentMethod().equals(type);
    }

    private boolean isTxnEntryValid(final IsvPaymentTransactionEntryModel entry,
            final AlternativePaymentMethod type)
    {
        return ACCEPT.equals(entry.getTransactionStatus()) && typeSpecificCheck(entry, type);
    }

    private boolean wasResultSuccessful(final IsvPaymentTransactionEntryModel transaction)
    {
        return ACCEPT.equals(transaction.getTransactionStatus())
                && isNotBlank(transaction.getProperties().get(MERCHANT_URL));
    }
}
