package isv.sap.payment.addon.facade;

import java.util.Optional;
import javax.annotation.Resource;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.commons.lang3.StringUtils;

import isv.cjl.payment.constants.PaymentConstants;
import isv.cjl.payment.enums.AlternativePaymentMethod;
import isv.cjl.payment.enums.CheckStatusDecision;
import isv.cjl.payment.enums.PaymentTransactionType;
import isv.cjl.payment.service.executor.PaymentServiceExecutor;
import isv.cjl.payment.service.executor.PaymentServiceResult;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.builder.alternative.CheckStatusRequestBuilder;
import isv.sap.payment.addon.enums.CheckStatusResponse;
import isv.sap.payment.enums.IsvAlternativePaymentStatus;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;
import isv.sap.payment.model.IsvPaymentTransactionModel;
import isv.sap.payment.service.alternativepayment.AlternativePaymentOrderStatusService;

import static isv.cjl.payment.constants.PaymentConstants.AlternativePaymentsResponseFields.PAYMENT_STATUS;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.sap.payment.addon.constants.IsvPaymentAddonConstants.AlternativePayments.CHECK_STATUS_RECONCILIATION_ID;
import static isv.sap.payment.addon.enums.CheckStatusResponse.CHECK_STATUS_ERROR;
import static isv.sap.payment.addon.enums.CheckStatusResponse.CHECK_STATUS_TOO_MANY_ATTEMPTS;
import static isv.sap.payment.addon.enums.CheckStatusResponse.PAYMENT_PENDING;
import static isv.sap.payment.addon.enums.CheckStatusResponse.PAYMENT_SUCCESS;
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.ACCEPT;
import static isv.sap.payment.enums.IsvAlternativePaymentStatus.SETTLED;
import static java.lang.Boolean.FALSE;
import static java.util.Optional.ofNullable;

/**
 * Encapsulates an implementation of {@link AlternativePaymentStatusFacade} interface used at addon level.
 * <p>
 * This component is consumed through storefront checkout controllers.
 */
public class AlternativePaymentStatusFacadeImpl implements AlternativePaymentStatusFacade
{
    @Resource(name = "isv.sap.payment.alternativePaymentOrderStatusService")
    private AlternativePaymentOrderStatusService alternativePaymentOrderStatusService;

    @Resource(name = "isv.sap.payment.paymentServiceExecutor")
    private PaymentServiceExecutor paymentServiceExecutor;

    @Resource
    private ConfigurationService configurationService;

    public CheckStatusResponse resolveCheckStatusResponse(final AbstractOrderModel orderModel)
    {
        final IsvPaymentTransactionModel transaction =
                alternativePaymentOrderStatusService.getAlternativePaymentTransaction(orderModel);

        final CheckStatusDecision checkStatusDecision = alternativePaymentOrderStatusService
                .shouldRunCheckStatus(transaction);

        switch (checkStatusDecision)
        {
            case RUN:
                return isOrderSettled(orderModel, transaction) ? PAYMENT_SUCCESS : PAYMENT_PENDING;
            case ATTEMPTS_EXCEEDED:
                return CHECK_STATUS_TOO_MANY_ATTEMPTS;
            case SKIP:
                return PAYMENT_PENDING;
            case ERROR:
                return CHECK_STATUS_ERROR;
            default:
                return PAYMENT_PENDING;
        }
    }

    private boolean isOrderSettled(final AbstractOrderModel orderModel, final IsvPaymentTransactionModel transaction)
    {
        final PaymentServiceResult checkStatusResult = paymentServiceExecutor
                .execute(createCheckStatusRequest(orderModel, transaction));

        return resolvePaymentStatus(checkStatusResult)
                .map(SETTLED::equals)
                .orElse(FALSE);
    }

    private PaymentServiceRequest createCheckStatusRequest(final AbstractOrderModel order,
            final IsvPaymentTransactionModel transaction)
    {
        final IsvPaymentTransactionEntryModel transactionEntry =
                alternativePaymentOrderStatusService.getAlternativePaymentTransactionEntry(transaction);

        return createCheckStatusRequest(order, transaction, transactionEntry);
    }

    private PaymentServiceRequest createCheckStatusRequest(final AbstractOrderModel order,
            final IsvPaymentTransactionModel transaction, final IsvPaymentTransactionEntryModel transactionEntry)
    {
        return new CheckStatusRequestBuilder()
                .setMerchantId(transaction.getMerchantId())
                .setReconciliationId(configurationService.getConfiguration().getString(CHECK_STATUS_RECONCILIATION_ID))
                .addParam(ORDER, order)
                .addParam(TRANSACTION, transactionEntry)
                .addParam("alternatePaymentMethod",
                        AlternativePaymentMethod.valueOf(transaction.getAlternativePaymentMethod().getCode()))
                .addParam("paymentTransactionType",
                        PaymentTransactionType.valueOf(transactionEntry.getType().getCode()))
                .build();
    }

    private Optional<IsvAlternativePaymentStatus> resolvePaymentStatus(final PaymentServiceResult result)
    {
        return ofNullable(result.getData(PaymentConstants.CommonFields.TRANSACTION))
                .map(IsvPaymentTransactionEntryModel.class::cast)
                .filter(transaction -> ACCEPT.equals(transaction.getTransactionStatus()))
                .map(transaction -> transaction.getProperties().get(PAYMENT_STATUS))
                .filter(StringUtils::isNotBlank)
                .map(String::toUpperCase)
                .map(IsvAlternativePaymentStatus::valueOf);
    }

    public void setAlternativePaymentOrderStatusService(
            final AlternativePaymentOrderStatusService alternativePaymentOrderStatusService)
    {
        this.alternativePaymentOrderStatusService = alternativePaymentOrderStatusService;
    }

    public void setPaymentServiceExecutor(final PaymentServiceExecutor paymentServiceExecutor)
    {
        this.paymentServiceExecutor = paymentServiceExecutor;
    }
}
