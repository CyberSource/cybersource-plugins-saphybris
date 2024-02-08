package isv.sap.payment.service.executor.request.converter.applepay.strategies;

import java.math.BigDecimal;
import java.util.Map;

import com.google.common.base.Function;
import de.hybris.platform.core.model.c2l.RegionModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.AddressModel;

import isv.cjl.payment.configuration.transaction.PaymentTransaction;
import isv.cjl.payment.constants.PaymentConstants;
import isv.cjl.payment.enums.CardType;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.utils.PaymentParamUtils;

import static isv.cjl.payment.constants.PaymentRequestParamConstants.*;
import static isv.cjl.payment.constants.PaymentServiceConstants.ApplePay.AUTHORIZATION;

public abstract class AbstractAuthorizationRequestConverterStrategy extends AbstractRequestConverter
        implements AuthorizationRequestConverterStrategy
{
    protected PaymentTransaction createBaseRequest(final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(PaymentConstants.CommonFields.ORDER);
        final AddressModel address = order.getPaymentInfo().getBillingAddress();
        final String shortStateCode = PaymentParamUtils
                .getValue(address.getRegion(), new Function<RegionModel, String>()
                {
                    @Override
                    public String apply(final RegionModel region)
                    {
                        return region.getIsocodeShort();
                    }
                });

        return requestFactory.request(AUTHORIZATION)
                .addParam(MERCHANT_ID, source.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(PURCHASE_TOTALS_CURRENCY, order.getCurrency().getIsocode())
                .addParam(PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT, BigDecimal.valueOf(order.getTotalPrice()))
                .addParam(CC_AUTH_SERVICE_RUN, true)
                .addParam(PAYMENT_SOLUTION, APPLE_PAY_PAYMENT_SOLUTION_VALUE)
                .addParam(BILL_TO_CITY, address.getTown())
                .addParam(BILL_TO_COUNTRY, address.getCountry().getIsocode())
                .addParam(BILL_TO_EMAIL, address.getEmail())
                .addParam(BILL_TO_FIRST_NAME, address.getFirstname())
                .addParam(BILL_TO_LAST_NAME, address.getLastname())
                .addParam(BILL_TO_POSTAL_CODE, address.getPostalcode())
                .addParam(BILL_TO_STATE, shortStateCode)
                .addParam(BILL_TO_STREET1, address.getLine1());
    }

    protected PaymentTransaction createMerchDecryptBaseRequest(final PaymentServiceRequest source)
    {
        final Map<String, Object> paymentToken = source.getRequiredParam(PAYMENT_TOKEN);
        final String expirationDate = PaymentParamUtils.getParam(APPLICATION_EXPIRATION_DATE, paymentToken);
        final CardType cardType = source.getRequiredParam(CARD_TYPE);

        final PaymentTransaction baseRequest = createBaseRequest(source);
        baseRequest.addParam(CARD_ACCOUNT_NUMBER,
                PaymentParamUtils.getParam(APPLICATION_PRIMARY_ACCOUNT_NUMBER, paymentToken))
                .addParam(CARD_EXPIRATION_MONTH, expirationDate.substring(2, 4))
                .addParam(CARD_EXPIRATION_YEAR, expirationDate.substring(0, 2))
                .addParam(CARD_TYPE, cardType)
                .addParam(PAYMENT_NETWORK_TOKEN_TRANSACTION_TYPE,
                        APPLE_PAY_PAYMENT_NETWORK_TOKEN_TRANSACTION_TYPE_VALUE);

        return baseRequest;
    }
}
