package isv.sap.payment.addon.controllers.pages.checkout.payment.sa;

import java.util.*;
import javax.annotation.Resource;

import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import isv.cjl.payment.model.MerchantProfile;
import isv.cjl.payment.sa.SecureAcceptanceRequestBuilder;
import isv.cjl.payment.security.service.SAService;
import isv.cjl.payment.utils.TimeUtils;
import isv.sap.payment.addon.provider.SecureAcceptanceUrlProvider;

import static isv.cjl.payment.enums.MerchantProfileType.HOP;
import static isv.sap.payment.constants.IsvPaymentConstants.SARequestFields.*;
import static java.util.Objects.isNull;
import static org.apache.commons.lang.StringUtils.EMPTY;

@Controller
@RequestMapping(value = "/checkout/payment/sa")
public class HopController extends SecureAcceptanceController
{
    @Resource
    private SecureAcceptanceUrlProvider hopSecureAcceptanceUrlProvider;

    @Resource(name = "isv.cjl.payment.security.service.SAService")
    private SAService sAService;

    @RequestMapping(value = "/hop", method = RequestMethod.POST)
    public String sendPaymentRequest(final Model model)
    {
        model.addAttribute("cartGuid", getCartService().getSessionCart().getGuid());
        model.addAttribute("postUrl", hopSecureAcceptanceUrlProvider.getURL());
        model.addAttribute("formFields", buildRequest());

        return "addon:/isvpaymentaddon/pages/checkout/payment/sa/hopRequest";
    }

    private Map<String, String> buildRequest()
    {
        final MerchantProfile profile = getMerchantService().getCurrentMerchantProfile(HOP);

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
                .addSignedField(BILL_TO_FORENAME, billingAddress.getFirstname())
                .addSignedField(BILL_TO_SURNAME, billingAddress.getLastname())
                .addSignedField(BILL_TO_EMAIL, billingAddress.getEmail())
                .addSignedField(BILL_TO_ADDRESS_COUNTRY, billingAddress.getCountry().getIsocode())
                .addSignedField(BILL_TO_ADDRESS_STATE, state)
                .addSignedField(BILL_TO_ADDRESS_CITY, billingAddress.getTown())
                .addSignedField(BILL_TO_ADDRESS_LINE1, billingAddress.getLine1())
                .addSignedField(BILL_TO_ADDRESS_LINE2, billingAddress.getLine2())
                .addSignedField(BILL_TO_ADDRESS_POSTAL_CODE, billingAddress.getPostalcode())
                .addSignedField(OVERRIDE_CUSTOM_CANCEL_PAGE, getCancelUrl())
                .addSignedField(OVERRIDE_CUSTOM_RECEIPT_PAGE, getReceiptUrl())
                .addSignedField(OVERRIDE_CUSTOM_MERCHANT_POST_PAGE, getMerchantPostUrl())
                .addSignedField(DEVICE_FINGERPRINT_ID, cart.getFingerPrintSessionID())
                .addUnsignedField(PARTNER_SOLUTION_ID, getPaymentSystemInfo().getPartnerSolutionID())
                .addUnsignedField(DEVELOPER_ID, getPaymentSystemInfo().getDeveloperID())
                .addUnsignedField(MERCHANT_DEFINED_DATA_99, profile.getMerchant().getId())
                .addUnsignedField(MERCHANT_DEFINED_DATA_100, HOP.getCode());



        return saRequestBuilder.build(profile.getSecretKey(), sAService);
    }

    protected String getCancelUrl()
    {
        return getSiteBaseUrlResolutionService().getWebsiteUrlForSite(getSiteService().getCurrentBaseSite(), true,
                "/checkout/multi/summary/payment/canceled");
    }
}
