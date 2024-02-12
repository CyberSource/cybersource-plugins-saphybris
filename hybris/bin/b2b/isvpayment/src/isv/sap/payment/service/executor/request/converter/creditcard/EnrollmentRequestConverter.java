package isv.sap.payment.service.executor.request.converter.creditcard;

import java.math.BigDecimal;

import de.hybris.platform.core.model.c2l.RegionModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.payment.dto.CardInfo;

import isv.cjl.payment.configuration.transaction.PaymentTransaction;
import isv.cjl.payment.enums.CardType;
import isv.cjl.payment.exception.PaymentException;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;
import isv.cjl.payment.utils.Assert;
import isv.cjl.payment.utils.PaymentParamUtils;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.Enrollment.PAYER_AUTH_ENROLL_SERVICE_TRANSACTION_MODE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.*;
import static isv.cjl.payment.constants.PaymentServiceConstants.CreditCard.ENROLLMENT;
import static isv.sap.payment.constants.IsvPaymentConstants.CreditCardRequestFields.CARD_INFO;
import static isv.sap.payment.constants.IsvPaymentConstants.CreditCardRequestFields.FLEX_TOKEN;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for payment enrollment request.
 */
public class EnrollmentRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(ORDER);
        final AddressModel billingAddress = order.getPaymentInfo().getBillingAddress();
        final AddressModel deliveryAddress = order.getDeliveryAddress();

        final PaymentTransaction enrollmentRequest = requestFactory.request(ENROLLMENT)
                .addParam(MERCHANT_ID, source.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(PURCHASE_TOTALS_CURRENCY, order.getCurrency().getIsocode())
                .addParam(PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT, BigDecimal.valueOf(order.getTotalPrice()))
                .addParam(PAYER_AUTH_ENROLL_SERVICE_RUN, true)
                .addParam(PAYER_AUTH_ENROLL_SERVICE_REFERENCE_ID,
                        source.getRequiredParam(PAYER_AUTH_ENROLL_SERVICE_REFERENCE_ID))
                .addParam(CC_AUTH_SERVICE_RUN, true)
                .addParam(DEVICE_FINGERPRINT_ID, order.getFingerPrintSessionID())
                .addParam(CC_AUTH_SERVICE_CARD_TYPE_SELECTION_INDICATOR, 1)
                .addParam(PAYER_AUTH_ENROLL_SERVICE_TRANSACTION_MODE,
                        source.getRequiredParam(PAYER_AUTH_ENROLL_SERVICE_TRANSACTION_MODE));

        setCardInfoFields(source, enrollmentRequest);
        setBillToFields(billingAddress, enrollmentRequest);
        setShipToFields(deliveryAddress, enrollmentRequest);

        return enrollmentRequest.request();
    }

    private void setBillToFields(final AddressModel billingAddress, final PaymentTransaction enrollment)
    {
        enrollment
                .addParam(BILL_TO_EMAIL, billingAddress.getEmail())
                .addParam(BILL_TO_FIRST_NAME, billingAddress.getFirstname())
                .addParam(BILL_TO_LAST_NAME, billingAddress.getLastname())
                .addParam(BILL_TO_STREET1, billingAddress.getLine1())
                .addParam(BILL_TO_CITY, billingAddress.getTown())
                .addParam(BILL_TO_POSTAL_CODE, billingAddress.getPostalcode())
                .addParam(BILL_TO_COUNTRY, billingAddress.getCountry().getIsocode())
                .addParam(BILL_TO_STATE,
                        PaymentParamUtils.getValue(billingAddress.getRegion(), RegionModel::getIsocodeShort))
                .addParam(BILL_TO_STREET2, billingAddress.getLine2())
                .addParam(BILL_TO_PHONE_NUMBER, billingAddress.getPhone1());
    }

    private void setShipToFields(final AddressModel deliveryAddress, final PaymentTransaction enrollment)
    {
        enrollment
                .addParam(SHIP_TO_FIRST_NAME, deliveryAddress.getFirstname())
                .addParam(SHIP_TO_LAST_NAME, deliveryAddress.getLastname())
                .addParam(SHIP_TO_COUNTRY, deliveryAddress.getCountry().getIsocode())
                .addParam(SHIP_TO_CITY, deliveryAddress.getTown())
                .addParam(SHIP_TO_POSTAL_CODE, deliveryAddress.getPostalcode())
                .addParam(SHIP_TO_STATE,
                        PaymentParamUtils.getValue(deliveryAddress.getRegion(), RegionModel::getIsocodeShort))
                .addParam(SHIP_TO_STREET1, deliveryAddress.getLine1())
                .addParam(SHIP_TO_STREET2, deliveryAddress.getLine2())
                .addParam(SHIP_TO_PHONE_NUMBER, deliveryAddress.getPhone1());
    }

    protected void setCardInfoFields(final PaymentServiceRequest request, final PaymentTransaction enrollment)
    {
        final String flexToken = request.getParam(FLEX_TOKEN);
        final CardInfo cardInfo = request.getParam(CARD_INFO);

        Assert.isTrue(
            cardInfo != null || isNotEmpty(flexToken),
            () -> new PaymentException("one of card info or flex transient token must be supplied")
        );

        if (isNotEmpty(flexToken))
        {
            enrollment.addParam(TOKEN_SOURCE_TRANSIENT_TOKEN, flexToken);
        }
        else if (cardInfo != null && cardInfo.getCardType() != null)
        {
            enrollment.addParam(CARD_TYPE, CardType.valueOf(PaymentParamUtils.toString(cardInfo.getCardType())))
                    .addParam(CARD_ACCOUNT_NUMBER, cardInfo.getCardNumber())
                    .addParam(CARD_EXPIRATION_MONTH, PaymentParamUtils.getMonth(cardInfo.getExpirationMonth()))
                    .addParam(CARD_EXPIRATION_YEAR, PaymentParamUtils.toString(cardInfo.getExpirationYear()))
                    .addParam(CARD_START_MONTH, PaymentParamUtils.getMonth(cardInfo.getIssueMonth()))
                    .addParam(CARD_START_YEAR, PaymentParamUtils.toString(cardInfo.getIssueYear()));
        }
    }
}
