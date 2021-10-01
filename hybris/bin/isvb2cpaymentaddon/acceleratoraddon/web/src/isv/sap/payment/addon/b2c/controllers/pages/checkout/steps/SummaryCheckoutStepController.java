package isv.sap.payment.addon.b2c.controllers.pages.checkout.steps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Resource;

import de.hybris.platform.acceleratorservices.checkout.pci.impl.ConfiguredCheckoutPciStrategy;
import de.hybris.platform.acceleratorservices.enums.CheckoutPciOptionEnum;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.PreValidateCheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.checkout.steps.AbstractCheckoutStepController;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.acceleratorstorefrontcommons.forms.PlaceOrderForm;
import de.hybris.platform.acceleratorstorefrontcommons.forms.SopPaymentDetailsForm;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.order.data.CardTypeData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.ProductOption;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import isv.cjl.payment.enums.MerchantProfileType;
import isv.cjl.payment.enums.PaymentType;
import isv.cjl.payment.service.MerchantService;
import isv.sap.payment.addon.VisaCheckoutPaymentDetailsData;
import isv.sap.payment.addon.facade.CreditCardPaymentFacade;
import isv.sap.payment.addon.facade.PaymentModeFacade;
import isv.sap.payment.addon.facade.VisaCheckoutPaymentDetailsFacade;
import isv.sap.payment.fraud.FraudFacade;

import static de.hybris.platform.acceleratorservices.enums.CheckoutPciOptionEnum.FLEX;
import static isv.cjl.payment.enums.PaymentType.GOOGLE_PAY;
import static isv.sap.payment.enums.AlternativePaymentMethod.GGP;
import static isv.sap.payment.enums.AlternativePaymentMethod.KLI;
import static isv.sap.payment.enums.PaymentType.ALTERNATIVE_PAYMENT;
import static java.lang.String.valueOf;

@RequestMapping(value = "/checkout/multi/summary")
public class SummaryCheckoutStepController extends AbstractCheckoutStepController
{
    @SuppressWarnings("all")
    protected static final Map<String, String> ISV_SOP_CARD_TYPES = new HashMap<String, String>()
    {{
        put("visa", "001");
        put("master", "002");
        put("amex", "003");
        put("diners", "005");
        put("maestro", "024");
    }};

    private static final String RESPONSE_ACTION = "responseAction";

    private static final String SUMMARY = "summary";

    @Resource
    private ConfiguredCheckoutPciStrategy checkoutPciStrategy;

    @Resource
    private PaymentModeFacade paymentModeFacade;

    @Resource(name = "isv.sap.payment.merchantService")
    private MerchantService merchantService;

    @Resource(name = "isv.sap.payment.fraudFacade")
    private FraudFacade fraudFacade;

    @Resource
    private VisaCheckoutPaymentDetailsFacade visaCheckoutPaymentDetailsFacade;

    @Resource
    private CreditCardPaymentFacade creditCardPaymentFacade;

    @Value("${isv.payment.visa.checkout.image.url}")
    private String visaCheckoutImageUrl;

    @Value("${isv.payment.visa.checkout.sdk.url}")
    private String visaCheckoutSDKUrl;

    @Value("${klarna.sdk.url}")
    private String klarnaSDKUrl;

    @Value("${isv.payment.flex.microform.sdk.url}")
    private String flexSDKUrl;

    @Value("${isv.payment.customer.3ds.songbird.url}")
    private String songbirdUrl;

    @Value("${isv.payment.customer.googlepay.merchant.id}")
    private String googlePayMerchantId;

    @Value("${isv.payment.customer.googlepay.environment}")
    private String googlePayEnvironment;

    @Value("${isv.payment.alternativePayment.checkStatus.WQR.CHECK_STATUS.frequency:5000}")
    private long weChatCheckStatusInterval;

    @RequestMapping(value = "/view/payment/error", method = RequestMethod.GET)
    @RequireHardLogIn
    @PreValidateCheckoutStep(checkoutStep = SUMMARY)
    public String enterStepWithPaymentError(final Model model, final RedirectAttributes redirectModel)
            throws CMSItemNotFoundException, // NOSONAR
            CommerceCartModificationException
    {
        GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER,
                "checkout.place.order.payment.error");
        return "redirect:/checkout/multi/summary/view";
    }

    @RequestMapping(value = "/view", method = RequestMethod.GET)
    @RequireHardLogIn
    @Override
    @PreValidateCheckoutStep(checkoutStep = SUMMARY)
    public String enterStep(final Model model, final RedirectAttributes redirectAttributes)
            throws CMSItemNotFoundException, // NOSONAR
            CommerceCartModificationException
    {
        final CartData cartData = getCheckoutFacade().getCheckoutCart();
        if (cartData.getEntries() != null && !cartData.getEntries().isEmpty())
        {
            for (final OrderEntryData entry : cartData.getEntries())
            {
                final String productCode = entry.getProduct().getCode();
                final ProductData product = getProductFacade().getProductForCodeAndOptions(productCode,
                        Arrays.asList(ProductOption.BASIC, ProductOption.PRICE));
                entry.setProduct(product);
            }
        }

        model.addAttribute("deviceFingerPrint", fraudFacade.getDeviceFingerPrint());
        model.addAttribute("cartData", cartData);
        model.addAttribute("allItems", cartData.getEntries());
        model.addAttribute("deliveryAddress", cartData.getDeliveryAddress());
        model.addAttribute("deliveryMode", cartData.getDeliveryMode());
        model.addAttribute("paymentInfo", cartData.getPaymentInfo());
        model.addAttribute("songbirdUrl", songbirdUrl);

        final boolean is3dsEnabled = merchantService.is3dsEnabled();
        model.addAttribute("is3dsEnabled", is3dsEnabled);
        if (is3dsEnabled && FLEX.equals(checkoutPciStrategy.getSubscriptionPciOption()))
        {
            model.addAttribute("jwt", creditCardPaymentFacade.createEnrollmentJwt());
        }

        prepareVisaCheckoutData(model, visaCheckoutImageUrl, visaCheckoutSDKUrl);

        prepareKlarnaCheckoutData(model);
        prepareApplePayCheckoutData(model);
        prepareGooglePayCheckoutData(model);
        prepareWeChatPayCheckoutData(model);

        prepareFlexMicroformData(model);

        final boolean requestSecurityCode = CheckoutPciOptionEnum.DEFAULT.equals(getCheckoutFlowFacade()
                .getSubscriptionPciOption());
        model.addAttribute("requestSecurityCode", Boolean.valueOf(requestSecurityCode));

        model.addAttribute(new PlaceOrderForm());

        storeCmsPageInModel(model, getContentPageForLabelOrId(MULTI_CHECKOUT_SUMMARY_CMS_PAGE_LABEL));
        setUpMetaDataForContentPage(model, getContentPageForLabelOrId(MULTI_CHECKOUT_SUMMARY_CMS_PAGE_LABEL));
        model.addAttribute(WebConstants.BREADCRUMBS_KEY,
                getResourceBreadcrumbBuilder().getBreadcrumbs("checkout.multi.summary.breadcrumb"));

        model.addAttribute("metaRobots", "noindex,nofollow");

        setCheckoutStepLinksForModel(model, getCheckoutStep());

        model.addAttribute("paymentPciType", checkoutPciStrategy.getSubscriptionPciOption());
        model.addAttribute("paymentModes", paymentModeFacade.getPaymentModes());

        setupCardPayment(model);

        return "addon:/isvpaymentaddon/pages/checkout/multi/checkoutSummaryPage";
    }

    private void prepareApplePayCheckoutData(final Model model)
    {
        model.addAttribute("countryCode", paymentModeFacade.getPaymentCountryCode());
    }

    private void prepareWeChatPayCheckoutData(final Model model)
    {
        model.addAttribute("weChatCheckStatusInterval", weChatCheckStatusInterval);
    }

    protected void prepareGooglePayCheckoutData(final Model model)
    {
        final boolean googlePayEnabled = paymentModeFacade.isPaymentModeSupported(ALTERNATIVE_PAYMENT, GGP);
        model.addAttribute("googlePayEnabled", googlePayEnabled);
        if (googlePayEnabled)
        {
            model.addAttribute("googlePayEnvironment", googlePayEnvironment);
            model.addAttribute("googlePayGatewayMerchantId", merchantService.getCurrentMerchant(GOOGLE_PAY).getId());
            model.addAttribute("googlePayMerchantId", googlePayMerchantId);
            model.addAttribute("googlePayMerchantName", getBaseSiteService().getCurrentBaseSite().getName());
        }
    }

    protected void prepareKlarnaCheckoutData(final Model model)
    {
        final boolean klarnaEnabled = paymentModeFacade.isPaymentModeSupported(ALTERNATIVE_PAYMENT, KLI);
        if (klarnaEnabled)
        {
            model.addAttribute("klarnaEnabled", true);
            model.addAttribute("klarnaSDKUrl", klarnaSDKUrl);
        }
    }

    protected void prepareVisaCheckoutData(final Model model, final String vcImageUrl,
            final String vcSDKUrl)
    {
        final boolean visaCheckoutEnabled = paymentModeFacade.
                isPaymentModeSupported(isv.sap.payment.enums.PaymentType.VISA_CHECKOUT, null);
        if (visaCheckoutEnabled)
        {
            model.addAttribute("visaCheckoutEnabled", true);
            model.addAttribute("visaCheckoutImageUrl", vcImageUrl);
            model.addAttribute("visaCheckoutAPIKey", merchantService.
                    getMerchantProfileForPaymentType(PaymentType.VISA_CHECKOUT,
                            MerchantProfileType.VCO).getAccessKey());
            model.addAttribute("visaCheckoutSDKUrl", vcSDKUrl);

            final Optional<VisaCheckoutPaymentDetailsData> vcPaymentDetails = visaCheckoutPaymentDetailsFacade
                    .getVCPaymentDetails();
            if (vcPaymentDetails.isPresent())
            {
                model.addAttribute("visaCheckoutPaymentDetails", vcPaymentDetails.get());
            }
        }
    }

    protected void prepareFlexMicroformData(final Model model)
    {
        if (checkoutPciStrategy.getSubscriptionPciOption().equals(FLEX))
        {
            model.addAttribute("flexSdkUrl", flexSDKUrl);
        }
    }

    @RequestMapping(value = "/payment/canceled", method = RequestMethod.POST)
    @RequireHardLogIn
    public String paymentCanceled(final Model model)
    {
        model.addAttribute(RESPONSE_ACTION, "/checkout/multi/summary/view");
        return "addon:/isvpaymentaddon/pages/checkout/payment/sa/response";
    }

    private void setupCardPayment(final Model model)
    {
        model.addAttribute("sopPaymentDetailsForm", new SopPaymentDetailsForm());
        model.addAttribute("months", getMonths());
        model.addAttribute("startYears", getStartYears());
        model.addAttribute("expiryYears", getExpiryYears());
        model.addAttribute("sopCardTypes", getSopCardTypes());
    }

    private List<SelectOption> getMonths()
    {
        final List<SelectOption> months = new ArrayList<>();

        for (int i = 1; i <= 12; i++)
        {
            months.add(new SelectOption(valueOf(i), valueOf(i < 10 ? "0" + i : StringUtils.EMPTY + i)));
        }
        return months;
    }

    private List<SelectOption> getStartYears()
    {
        final List<SelectOption> startYears = new ArrayList<>();
        final Calendar calender = new GregorianCalendar();

        for (int i = calender.get(Calendar.YEAR); i > calender.get(Calendar.YEAR) - 6; i--)
        {
            startYears.add(new SelectOption(String.valueOf(i), String.valueOf(i)));
        }
        return startYears;
    }

    private Collection<CardTypeData> getSopCardTypes()
    {
        final Collection<CardTypeData> sopCardTypes = new ArrayList<>();

        final List<CardTypeData> supportedCardTypes = getCheckoutFacade().getSupportedCardTypes();
        for (final CardTypeData supportedCardType : supportedCardTypes)
        {
            if (ISV_SOP_CARD_TYPES.containsKey(supportedCardType.getCode()))
            {
                sopCardTypes.add(createCardTypeData(ISV_SOP_CARD_TYPES.get(supportedCardType.getCode()),
                        supportedCardType.getName()));
            }
        }
        return sopCardTypes;
    }

    private List<SelectOption> getExpiryYears()
    {
        final List<SelectOption> expiryYears = new ArrayList<>();
        final Calendar calender = new GregorianCalendar();
        for (int i = calender.get(Calendar.YEAR); i < calender.get(Calendar.YEAR) + 11; i++)
        {
            expiryYears.add(new SelectOption(String.valueOf(i), String.valueOf(i)));
        }
        return expiryYears;
    }

    private CardTypeData createCardTypeData(final String code, final String name)
    {
        final CardTypeData cardTypeData = new CardTypeData();
        cardTypeData.setCode(code);
        cardTypeData.setName(name);
        return cardTypeData;
    }

    @RequestMapping(value = "/back", method = RequestMethod.GET)
    @RequireHardLogIn
    @Override
    public String back(final RedirectAttributes redirectAttributes)
    {
        return getCheckoutStep().previousStep();
    }

    @RequestMapping(value = "/next", method = RequestMethod.GET)
    @RequireHardLogIn
    @Override
    public String next(final RedirectAttributes redirectAttributes)
    {
        return getCheckoutStep().nextStep();
    }

    protected CheckoutStep getCheckoutStep()
    {
        return getCheckoutStep(SUMMARY);
    }
}
