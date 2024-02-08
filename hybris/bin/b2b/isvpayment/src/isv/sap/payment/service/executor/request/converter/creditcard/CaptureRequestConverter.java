package isv.sap.payment.service.executor.request.converter.creditcard;

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
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.CC_CAPTURE_SERVICE_AUTH_REQUEST_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.CC_CAPTURE_SERVICE_AUTH_REQUEST_TOKEN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.CC_CAPTURE_SERVICE_RUN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_CURRENCY;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT;
import static isv.cjl.payment.constants.PaymentServiceConstants.CreditCard.CAPTURE;
import static java.lang.Boolean.TRUE;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for payment capture request.
 */
public class CaptureRequestConverter extends AbstractRequestConverter
{
    @Inject
    @Named(DEFAULT_PROCESSING_LEVEL_POPULATOR)
    private Populator<PaymentServiceRequest, PaymentTransaction> processingLevelPopulator;

    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(ORDER);
        final PaymentTransactionEntryModel auth = source.getRequiredParam(TRANSACTION);

        final PaymentTransaction transaction = populateSpecificFields(source);
        processingLevelPopulator.populate(source, populateCommonFields(transaction, order, auth));

        return transaction.request();
    }

    protected PaymentTransaction populateSpecificFields(final PaymentServiceRequest source)
    {
        return requestFactory.request(CAPTURE)
                .addParam(MERCHANT_ID, source.getParam(MERCHANT_ID));
    }

    protected PaymentTransaction populateCommonFields(final PaymentTransaction transaction,
            final AbstractOrderModel order, final PaymentTransactionEntryModel authorization)
    {
        return transaction
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(CC_CAPTURE_SERVICE_RUN, TRUE)
                .addParam(CC_CAPTURE_SERVICE_AUTH_REQUEST_ID, authorization.getRequestId())
                .addParam(PURCHASE_TOTALS_CURRENCY, authorization.getCurrency().getIsocode())
                .addParam(CC_CAPTURE_SERVICE_AUTH_REQUEST_TOKEN, authorization.getRequestToken())
                .addParam(PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT, authorization.getAmount());
    }

    public void setProcessingLevelPopulator(
            final Populator<PaymentServiceRequest, PaymentTransaction> processingLevelPopulator)
    {
        this.processingLevelPopulator = processingLevelPopulator;
    }
}
