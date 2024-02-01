package isv.sap.payment.service.executor.request.converter.creditcard;

import java.math.BigDecimal;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.base.Optional;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.payment.dto.CardInfo;
import org.apache.commons.lang3.StringUtils;

import isv.cjl.payment.configuration.transaction.PaymentTransaction;
import isv.cjl.payment.constants.PaymentRequestParamConstants;
import isv.cjl.payment.enums.CardType;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.executor.request.populator.Populator;
import isv.cjl.payment.service.request.Request;
import isv.cjl.payment.utils.Assert;
import isv.cjl.payment.utils.PaymentParamUtils;

import static isv.cjl.module.util.ConfigurationConstants.DEFAULT_PROCESSING_LEVEL_POPULATOR;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentServiceConstants.CreditCard.REFUND_STANDALONE;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for payment refund standalone request.
 */
public class RefundStandaloneRequestConverter extends AbstractRequestConverter
{
    @Inject
    @Named(DEFAULT_PROCESSING_LEVEL_POPULATOR)
    private Populator<PaymentServiceRequest, PaymentTransaction> processingLevelPopulator;

    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(ORDER);

        final PaymentTransaction target = addCommonFields(source, order);

        final Optional<AddressModel> addressOptional = Optional.fromNullable(order.getPaymentAddress());
        if (addressOptional.isPresent())
        {
            addBillingAddress(addressOptional.get(), target);
        }

        addCardInfoFields(source, target);

        processingLevelPopulator.populate(source, target);

        return target.request();
    }

    protected PaymentTransaction addCommonFields(final PaymentServiceRequest source, final AbstractOrderModel order)
    {
        return requestFactory.request(REFUND_STANDALONE)
                .addParam(PaymentRequestParamConstants.MERCHANT_ID, source.getParam(MERCHANT_ID))
                .addParam(PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(PaymentRequestParamConstants.CC_CREDIT_SERVICE_RUN, true)
                .addParam(PaymentRequestParamConstants.PURCHASE_TOTALS_CURRENCY, order.getCurrency().getIsocode())
                .addParam(PaymentRequestParamConstants.PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT,
                        BigDecimal.valueOf(order.getTotalPrice()));
    }

    protected void addCardInfoFields(final PaymentServiceRequest request, final PaymentTransaction target)
    {
        final CardInfo cardInfo = request.getParam("card");
        final String subscriptionID = request.getParam("subscriptionID");

        Assert.isTrue(cardInfo != null || StringUtils.isNotEmpty(subscriptionID),
            () -> new RuntimeException("one out of two should be provided: card info or subscription id"));

        if (StringUtils.isNotEmpty(subscriptionID))
        {
            target.addParam("recurringSubscriptionInfoSubscriptionID", subscriptionID);
        }
        else if (cardInfo != null)
        {
            addCardDetails(cardInfo, target);
        }
    }

    protected void addCardDetails(final CardInfo cardInfo, final PaymentTransaction target)
    {
        final CardType cardType = CardType.valueOf(cardInfo.getCardType().toString());

        target.addParam(PaymentRequestParamConstants.CARD_TYPE, cardType)
                .addParam(PaymentRequestParamConstants.CARD_ACCOUNT_NUMBER, cardInfo.getCardNumber())
                .addParam(PaymentRequestParamConstants.CARD_EXPIRATION_MONTH,
                        PaymentParamUtils.getMonth(cardInfo.getExpirationMonth()))
                .addParam(PaymentRequestParamConstants.CARD_EXPIRATION_YEAR,
                        PaymentParamUtils.toString(cardInfo.getExpirationYear()))
                .addParam(PaymentRequestParamConstants.CARD_START_MONTH,
                        PaymentParamUtils.getMonth(cardInfo.getIssueMonth()))
                .addParam(PaymentRequestParamConstants.CARD_START_YEAR,
                        PaymentParamUtils.toString(cardInfo.getIssueYear()));
    }

    protected void addBillingAddress(final AddressModel billingAddress, final PaymentTransaction target)
    {
        target.addParam(PaymentRequestParamConstants.BILL_TO_FIRST_NAME, billingAddress.getFirstname())
                .addParam(PaymentRequestParamConstants.BILL_TO_LAST_NAME, billingAddress.getLastname())
                .addParam(PaymentRequestParamConstants.BILL_TO_EMAIL, billingAddress.getEmail())
                .addParam(PaymentRequestParamConstants.BILL_TO_COUNTRY, billingAddress.getCountry().getIsocode())
                .addParam(PaymentRequestParamConstants.BILL_TO_CITY, billingAddress.getTown())
                .addParam(PaymentRequestParamConstants.BILL_TO_POSTAL_CODE, billingAddress.getPostalcode())
                .addParam(PaymentRequestParamConstants.BILL_TO_STATE,
                        PaymentParamUtils.getValue(billingAddress.getRegion(), input -> input.getIsocodeShort()))
                .addParam(PaymentRequestParamConstants.BILL_TO_STREET1, billingAddress.getLine1())
                .addParam(PaymentRequestParamConstants.BILL_TO_STREET2, billingAddress.getLine2());
    }

    public void setProcessingLevelPopulator(
            final Populator<PaymentServiceRequest, PaymentTransaction> processingLevelPopulator)
    {
        this.processingLevelPopulator = processingLevelPopulator;
    }
}
