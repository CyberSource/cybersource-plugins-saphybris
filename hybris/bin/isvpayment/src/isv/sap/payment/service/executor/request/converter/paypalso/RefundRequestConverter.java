package isv.sap.payment.service.executor.request.converter.paypalso;

import java.math.BigDecimal;
import java.util.Optional;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.AddressModel;

import isv.cjl.payment.configuration.transaction.PaymentTransaction;
import isv.cjl.payment.constants.PaymentRequestParamConstants;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.*;
import static isv.cjl.payment.constants.PaymentServiceConstants.PayPalSo.REFUND;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for paypal payment refund request.
 */
public class RefundRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest request)
    {
        final AbstractOrderModel order = request.getRequiredParam(ORDER);
        final IsvPaymentTransactionEntryModel captureTransactionEntry = request.getRequiredParam(TRANSACTION);

        final PaymentTransaction payPalRefund = setBaseFields(order, captureTransactionEntry, request);
        setBillingFields(order, payPalRefund);

        return payPalRefund.request();
    }

    protected void setBillingFields(final AbstractOrderModel order, final PaymentTransaction payPalSet)
    {
        final Optional<AddressModel> addressOptional = Optional.ofNullable(order.getPaymentAddress());

        if (addressOptional.isPresent())
        {
            final AddressModel address = addressOptional.get();

            payPalSet.addParam(BILL_TO_EMAIL, address.getEmail())
                    .addParam(BILL_TO_FIRST_NAME, address.getFirstname())
                    .addParam(BILL_TO_LAST_NAME, address.getLastname());
        }
    }

    protected PaymentTransaction setBaseFields(final AbstractOrderModel order,
            final IsvPaymentTransactionEntryModel captureTransactionEntry,
            final PaymentServiceRequest request)
    {
        final Number amount = (Number) Optional.ofNullable(request.getParam("amount")).orElse(order.getTotalPrice());

        return requestFactory.request(REFUND)
                .addParam(PaymentRequestParamConstants.MERCHANT_ID,
                        request.getRequiredParam(PaymentRequestParamConstants.MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(PAYPAL_REFUND_SERVICE_PAYPAL_CAPTURE_ID,
                        captureTransactionEntry.getProperties().get(PAYPAL_DO_CAPTURE_REPLY_TRANSACTION_ID))
                .addParam(PAYPAL_REFUND_SERVICE_PAYPAL_DO_CAPTURE_REQUEST_ID, captureTransactionEntry.getRequestId())
                .addParam(PAYPAL_REFUND_SERVICE_PAYPAL_DO_CAPTURE_REQUEST_TOKEN,
                        captureTransactionEntry.getRequestToken())
                .addParam(PAYPAL_REFUND_SERVICE_RUN, true)
                .addParam(PURCHASE_TOTALS_CURRENCY, order.getCurrency().getIsocode())
                .addParam(PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT, BigDecimal.valueOf(amount.doubleValue()))
                .addParam(PAYPAL_REFUND_SERVICE_PAYPAL_NOTE, request.getParam("note"));
    }
}
