package isv.sap.payment.service.executor.request.converter.creditcard;

import java.math.BigDecimal;

import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;

import isv.cjl.payment.configuration.transaction.PaymentTransaction;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.CC_AUTH_REVERSAL_SERVICE_AUTH_REQUEST_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.CC_AUTH_REVERSAL_SERVICE_RUN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_CURRENCY;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT;
import static isv.cjl.payment.constants.PaymentServiceConstants.CreditCard.AUTHORIZATION_REVERSAL;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static org.apache.commons.lang3.Validate.notNull;

public class AuthorizationReversalRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(ORDER);
        final PaymentTransactionEntryModel authorization = source.getRequiredParam(TRANSACTION);

        return populateCommonFields(populateSpecificFields(source), order, authorization).request();
    }

    protected PaymentTransaction populateSpecificFields(final PaymentServiceRequest source)
    {
        return requestFactory.request(AUTHORIZATION_REVERSAL)
                .addParam(MERCHANT_ID, source.getParam(MERCHANT_ID));
    }

    protected PaymentTransaction populateCommonFields(final PaymentTransaction transaction,
            final AbstractOrderModel order, final PaymentTransactionEntryModel authorization)
    {
        final CurrencyModel currency = order.getCurrency();

        notNull(currency, format("The currency property is missing on order [%s]", order.getCode()));

        return transaction
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(CC_AUTH_REVERSAL_SERVICE_AUTH_REQUEST_ID, authorization.getRequestId())
                .addParam(CC_AUTH_REVERSAL_SERVICE_RUN, TRUE)
                .addParam(PURCHASE_TOTALS_CURRENCY, currency.getIsocode())
                .addParam(PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT, BigDecimal.valueOf(order.getTotalPrice()));
    }
}
