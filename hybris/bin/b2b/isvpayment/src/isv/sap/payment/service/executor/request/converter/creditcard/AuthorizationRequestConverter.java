package isv.sap.payment.service.executor.request.converter.creditcard;

import java.math.BigDecimal;

import de.hybris.platform.core.model.c2l.RegionModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.payment.dto.CardInfo;

import de.hybris.platform.util.Config;
import isv.cjl.payment.configuration.transaction.PaymentTransaction;
import isv.cjl.payment.enums.CardType;
import isv.cjl.payment.exception.PaymentException;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;
import isv.cjl.payment.utils.Assert;
import isv.cjl.payment.utils.PaymentParamUtils;
import org.apache.commons.lang.StringUtils;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.*;
import static isv.cjl.payment.constants.PaymentServiceConstants.CreditCard.AUTHORIZATION;
import static isv.sap.payment.constants.IsvPaymentConstants.CreditCardRequestFields.CARD_INFO;
import static isv.sap.payment.constants.IsvPaymentConstants.CreditCardRequestFields.FLEX_TOKEN;
import static isv.sap.payment.constants.IsvPaymentConstants.CreditCardRequestFields.SUBSCRIPTION_ID;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for payment authorization request.
 */
public class AuthorizationRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final PaymentTransaction authorization = setCommonFields(source);

        setDecisionManagerFields(source, authorization);
        setCardInfoFields(source, authorization);
        setPayerAuthenticationFields(source, authorization);
        setCheckReplyFields(source, authorization);

        return authorization.request();
    }

    protected PaymentTransaction setCommonFields(final PaymentServiceRequest request)
    {
        final AbstractOrderModel order = request.getRequiredParam(ORDER);
        final AddressModel billingAddress = order.getPaymentInfo().getBillingAddress();

        notNull(billingAddress, "billingAddress is required on order");

        return requestFactory.request(AUTHORIZATION)
                .addParam(MERCHANT_ID, request.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(CC_AUTH_SERVICE_RUN, true)
                .addParam(PURCHASE_TOTALS_CURRENCY, order.getCurrency().getIsocode())
                .addParam(PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT, BigDecimal.valueOf(order.getTotalPrice()))
                .addParam(BILL_TO_FIRST_NAME, billingAddress.getFirstname())
                .addParam(BILL_TO_LAST_NAME, billingAddress.getLastname())
                .addParam(BILL_TO_EMAIL, billingAddress.getEmail())
                .addParam(BILL_TO_COUNTRY, billingAddress.getCountry().getIsocode())
                .addParam(BILL_TO_CITY, billingAddress.getTown())
                .addParam(BILL_TO_POSTAL_CODE, billingAddress.getPostalcode())
                .addParam(BILL_TO_STATE,
                        PaymentParamUtils.getValue(billingAddress.getRegion(), RegionModel::getIsocodeShort))
                .addParam(BILL_TO_STREET1, billingAddress.getLine1())
                .addParam(BILL_TO_STREET2, billingAddress.getLine2())
                .addParam(BILL_TO_PHONE_NUMBER, billingAddress.getPhone1())
                .addParam(CC_AUTH_SERVICE_CARD_TYPE_SELECTION_INDICATOR, 1)
                .addParam(DEVICE_FINGERPRINT_ID, order.getFingerPrintSessionID());
    }

    private void setCheckReplyFields(final PaymentServiceRequest request, final PaymentTransaction authorization)
    {
        final String authServiceCommerceIndicator = request.getParam("authServiceCommerceIndicator");
        final String ucafCollectionIndicator = request.getParam("ucafCollectionIndicator");
        final String ucafAuthenticationData = request.getParam("ucafAuthenticationData");
        final String authServiceVeresEnrolled = request.getParam("ccAuthServiceVeresEnrolled");
        final String xid = request.getParam("ccAuthServiceXid");
        final String eci = request.getParam("ccAuthServiceEciRaw");
        final String cavv = request.getParam("ccAuthServiceCavv");

        authorization
                .addParam(CC_AUTH_SERVICE_COMMERCE_INDICATOR, authServiceCommerceIndicator)
                .addParam(CC_AUTH_SERVICE_VERES_ENROLLED, authServiceVeresEnrolled)
                .addParam(UCAF_COLLECTION_INDICATOR, ucafCollectionIndicator)
                .addParam(UCAF_AUTHENTICATION_DATA, ucafAuthenticationData)
                .addParam(CC_AUTH_SERVICE_XID, xid)
                .addParam(CC_AUTH_SERVICE_ECI_RAW, eci)
                .addParam(CC_AUTH_SERVICE_CAVV, cavv);
    }

    private void setDecisionManagerFields(final PaymentServiceRequest request, final PaymentTransaction authorization)
    {
        final Boolean decisionManagerEnabled = request.getParam(DECISION_MANAGER_ENABLED);

        if (decisionManagerEnabled != null)
        {
            authorization.addParam(DECISION_MANAGER_ENABLED, decisionManagerEnabled);
        }
    }

    protected void setCardInfoFields(final PaymentServiceRequest request, final PaymentTransaction authorization)
    {
        final String subscriptionID = request.getParam(SUBSCRIPTION_ID);
        final String flexToken = request.getParam(FLEX_TOKEN);
        final CardInfo cardInfo = request.getParam(CARD_INFO);

        Assert.isTrue(
            cardInfo != null || isNotEmpty(flexToken) || isNotEmpty(subscriptionID),
            () -> new PaymentException("one of card info ,flex transient token or subscription ID must be supplied")
        );

        if (isNotEmpty(flexToken))
        {
            authorization.addParam(TOKEN_SOURCE_TRANSIENT_TOKEN, flexToken);
        }
        else if (isNotEmpty(subscriptionID))
        {
            authorization.addParam(RECURRING_SUBSCRIPTION_INFO_SUBSCRIPTION_ID, subscriptionID);
        }
        else if (cardInfo != null && cardInfo.getCardType() != null)
        {
            authorization.addParam(CARD_TYPE, CardType.valueOf(PaymentParamUtils.toString(cardInfo.getCardType())))
                    .addParam(CARD_ACCOUNT_NUMBER, cardInfo.getCardNumber())
                    .addParam(CARD_EXPIRATION_MONTH, PaymentParamUtils.getMonth(cardInfo.getExpirationMonth()))
                    .addParam(CARD_EXPIRATION_YEAR, PaymentParamUtils.toString(cardInfo.getExpirationYear()))
                    .addParam(CARD_START_MONTH, PaymentParamUtils.getMonth(cardInfo.getIssueMonth()))
                    .addParam(CARD_START_YEAR, PaymentParamUtils.toString(cardInfo.getIssueYear()));
        }
    }

    protected void setPayerAuthenticationFields(final PaymentServiceRequest request,
            final PaymentTransaction authorization)
    {
        final String paRes = request.getParam("paRes");
        final String transactionId = request.getParam("payerAuthValidateServiceAuthenticationTransactionID");
        final Boolean enableAuthValidateService = request.getParam("payerAuthValidateServiceRun");

        if (isNotEmpty(paRes))
        {
            authorization.addParam(PAYER_AUTH_VALIDATE_SERVICE_RUN, true)
                    .addParam(PAYER_AUTH_VALIDATE_SERVICE_SIGNED_PARES, paRes);
        }

        if (isNotEmpty(transactionId))
        {
            authorization.addParam(PAYER_AUTH_VALIDATE_SERVICE_AUTHENTICATION_TRANSACTION_ID, transactionId);
        }

        if (enableAuthValidateService != null)
        {
            authorization.addParam(PAYER_AUTH_VALIDATE_SERVICE_RUN, enableAuthValidateService);
        }
    }
}
