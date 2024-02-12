package isv.sap.payment.addon.controllers.pages.checkout.payment.sa;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Resource;

import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import isv.cjl.payment.model.MerchantProfile;
import isv.cjl.payment.sa.SecureAcceptanceRequestBuilder;
import isv.cjl.payment.security.service.SAService;
import isv.cjl.payment.utils.TimeUtils;

import static isv.cjl.payment.enums.MerchantProfileType.SOP;
import static isv.sap.payment.constants.IsvPaymentConstants.SARequestFields.*;
import static java.util.Objects.isNull;
import static org.apache.commons.lang.StringUtils.EMPTY;

@Controller
@RequestMapping(value = "/checkout/payment/sa")
public class SopController extends SecureAcceptanceController
{
    public static final String POST_URL = "isv.payment.secure.acceptance.sop.post.url";

    public static final String SOP_REQUEST = "addon:/isvpaymentaddon/pages/checkout/payment/sa/sopRequest";
    private static final String CARD_TYPE_SELECTION_INDICATOR_VALUE = "1";

    @Resource(name = "isv.cjl.payment.security.service.SAService")
    private SAService sAService;

    @RequestMapping(value = "sop", method = RequestMethod.GET)
    public String getSopForm(final ModelMap model)
    {
        model.addAttribute("cartGuid", getCartService().getSessionCart().getGuid());
        model.addAttribute("postUrl", getConfigurationService().getConfiguration().getString(POST_URL));
        model.addAttribute("formFields", buildRequest());

        return SOP_REQUEST;
    }

    private Map<String, String> buildRequest()
    {
        final MerchantProfile profile = getMerchantService().getCurrentMerchantProfile(SOP);
        final CartModel cart = getCartService().getSessionCart();
        final AddressModel billingAddress = cart.getPaymentInfo().getBillingAddress();
        final String state = isNull(billingAddress.getRegion()) ? EMPTY : billingAddress.getRegion().getIsocodeShort();

        SecureAcceptanceRequestBuilder saRequestBuilder = new SecureAcceptanceRequestBuilder();
        saRequestBuilder.addSignedField(ACCESS_KEY, profile.getAccessKey())
                .addSignedField(PROFILE_ID, profile.getProfileId())
                .addSignedField(TRANSACTION_UUID, UUID.randomUUID().toString())
                .addSignedField(SIGNED_DATE_TIME, TimeUtils.toUTCDateTime(new Date()))
                .addSignedField(LOCALE, getI18NService().getCurrentLocale().toString())
                .addSignedField(TRANSACTION_TYPE, getTransactionType())
                .addSignedField(REFERENCE_NUMBER, cart.getGuid())
                .addSignedField(AMOUNT, cart.getTotalPrice().toString())
                .addSignedField(CURRENCY, cart.getCurrency().getIsocode())
                .addSignedField(PAYMENT_METHOD, "card")
                .addSignedField(BILL_TO_FORENAME, billingAddress.getFirstname())
                .addSignedField(BILL_TO_SURNAME, billingAddress.getLastname())
                .addSignedField(BILL_TO_EMAIL, billingAddress.getEmail())
                .addSignedField(BILL_TO_ADDRESS_LINE1, billingAddress.getLine1())
                .addSignedField(BILL_TO_ADDRESS_CITY, billingAddress.getTown())
                .addSignedField(BILL_TO_ADDRESS_COUNTRY, billingAddress.getCountry().getIsocode())
                .addSignedField(BILL_TO_ADDRESS_POSTAL_CODE, billingAddress.getPostalcode())
                .addSignedField(BILL_TO_ADDRESS_STATE, state)
                .addSignedField(OVERRIDE_CUSTOM_RECEIPT_PAGE, getReceiptUrl())
                .addSignedField(OVERRIDE_CUSTOM_MERCHANT_POST_PAGE, getMerchantPostUrl())
                .addSignedField(DEVICE_FINGERPRINT_ID, cart.getFingerPrintSessionID())
                .addUnsignedField(PARTNER_SOLUTION_ID, getPaymentSystemInfo().getPartnerSolutionID())
                .addUnsignedField(DEVELOPER_ID, getPaymentSystemInfo().getDeveloperID())
                .addUnsignedField(CARD_TYPE, EMPTY)
                .addUnsignedField(CARD_NUMBER, EMPTY)
                .addUnsignedField(CARD_EXPIRE_DATE, EMPTY)
                .addUnsignedField(CARD_CVN, EMPTY)
                .addUnsignedField(MERCHANT_DEFINED_DATA_99, profile.getMerchant().getId())
                .addUnsignedField(MERCHANT_DEFINED_DATA_100, SOP.getCode())
                .addUnsignedField(CARD_TYPE_SELECTION_INDICATOR, CARD_TYPE_SELECTION_INDICATOR_VALUE);




        return saRequestBuilder.build(profile.getSecretKey(), sAService);
    }
}
