package isv.sap.payment.service.executor.request.converter.creditcard;

import java.math.BigDecimal;
import javax.inject.Inject;
import javax.inject.Named;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;

import isv.cjl.payment.configuration.transaction.PaymentTransaction;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.executor.request.populator.Populator;
import isv.cjl.payment.service.request.Request;

import static isv.cjl.module.util.ConfigurationConstants.DEFAULT_PROCESSING_LEVEL_POPULATOR;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.AMOUNT;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.CC_CREDIT_SERVICE_CAPTURE_REQUEST_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.CC_CREDIT_SERVICE_RUN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_CURRENCY;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT;
import static isv.cjl.payment.constants.PaymentServiceConstants.CreditCard.REFUND_FOLLOW_ON;
import static java.lang.Boolean.TRUE;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for payment refund follow-on request.
 */
public class RefundFollowOnRequestConverter extends AbstractRequestConverter
{
    @Inject
    @Named(DEFAULT_PROCESSING_LEVEL_POPULATOR)
    private Populator<PaymentServiceRequest, PaymentTransaction> processingLevelPopulator;

    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final PaymentTransaction target = populateCommonFields(populateSpecificFields(source), source);

        processingLevelPopulator.populate(source, target);

        return target.request();
    }

    protected PaymentTransaction populateSpecificFields(final PaymentServiceRequest source)
    {
        return requestFactory.request(REFUND_FOLLOW_ON)
                .addParam(MERCHANT_ID, source.getParam(MERCHANT_ID));
    }

    protected PaymentTransaction populateCommonFields(final PaymentTransaction transaction,
            final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(ORDER);
        final PaymentTransactionEntryModel capture = source.getRequiredParam(TRANSACTION);
        final BigDecimal amount = source.getRequiredParam(AMOUNT);

        return transaction
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(PURCHASE_TOTALS_CURRENCY, order.getCurrency().getIsocode())
                .addParam(PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT, amount)
                .addParam(CC_CREDIT_SERVICE_CAPTURE_REQUEST_ID, capture.getRequestId())
                .addParam(CC_CREDIT_SERVICE_RUN, TRUE);
    }

    public void setProcessingLevelPopulator(
            final Populator<PaymentServiceRequest, PaymentTransaction> processingLevelPopulator)
    {
        this.processingLevelPopulator = processingLevelPopulator;
    }
}
