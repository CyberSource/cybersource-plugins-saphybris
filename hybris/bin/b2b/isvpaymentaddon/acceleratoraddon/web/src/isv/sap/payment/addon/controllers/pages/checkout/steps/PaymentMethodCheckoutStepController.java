package isv.sap.payment.addon.controllers.pages.checkout.steps;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import javax.annotation.Resource;

import de.hybris.platform.acceleratorservices.enums.CheckoutPciOptionEnum;
import de.hybris.platform.acceleratorservices.payment.data.PaymentData;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.PreValidateCheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.checkout.steps.AbstractCheckoutStepController;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.acceleratorstorefrontcommons.forms.AddressForm;
import de.hybris.platform.acceleratorstorefrontcommons.forms.PaymentDetailsForm;
import de.hybris.platform.acceleratorstorefrontcommons.forms.SopPaymentDetailsForm;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CountryData;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import isv.sap.payment.addon.facade.CreditCardPaymentFacade;
import isv.sap.payment.addon.facade.PaymentInfoFacade;

import static de.hybris.platform.acceleratorservices.enums.CheckoutPciOptionEnum.FLEX;
import static de.hybris.platform.acceleratorservices.enums.CheckoutPciOptionEnum.HOP;
import static de.hybris.platform.acceleratorservices.enums.CheckoutPciOptionEnum.SOP;
import static de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants.BREADCRUMBS_KEY;
import static de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages.CONF_MESSAGES_HOLDER;
import static java.util.Calendar.YEAR;

@RequestMapping(value = "/checkout/multi/payment-method")
public class PaymentMethodCheckoutStepController extends AbstractCheckoutStepController
{
    private static final String PAYMENT_METHOD = "payment-method";

    private static final String CART_DATA_ATTR = "cartData";

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentMethodCheckoutStepController.class);

    @Resource
    private PaymentInfoFacade paymentInfoFacade;

    @ModelAttribute("billingCountries")
    public Collection<CountryData> getBillingCountries()
    {
        return getCheckoutFacade().getBillingCountries();
    }

    @ModelAttribute("expiryYears")
    public List<SelectOption> getExpiryYears()
    {
        final List<SelectOption> expiryYears = new ArrayList<>();
        final Calendar calender = new GregorianCalendar();

        for (int i = calender.get(YEAR); i < calender.get(YEAR) + 11; i++)
        {
            expiryYears.add(new SelectOption(String.valueOf(i), String.valueOf(i)));
        }

        return expiryYears;
    }

    @Override
    @GetMapping(value = "/add")
    @RequireHardLogIn
    @PreValidateCheckoutStep(checkoutStep = PAYMENT_METHOD)
    public String enterStep(final Model model, final RedirectAttributes redirectAttributes)
            throws CMSItemNotFoundException
    {
        getCheckoutFacade().setDeliveryModeIfAvailable();
        setupAddPaymentPage(model);

        final CheckoutPciOptionEnum subscriptionPciOption = getCheckoutFlowFacade().getSubscriptionPciOption();
        setCheckoutStepLinksForModel(model, getCheckoutStep());

        if (SOP.equals(subscriptionPciOption) || HOP.equals(subscriptionPciOption) || FLEX
                .equals(subscriptionPciOption) || subscriptionPciOption.getCode()=="Default")
        {
            final SopPaymentDetailsForm setupIsvPaymentForm = getSopPaymentDetailsForm(model);
            try
            {
                setupIsvPaymentForm(setupIsvPaymentForm, model);
                return "addon:/isvpaymentaddon/pages/checkout/multi/isvPaymentMethodPage";
            }
            catch (final Exception e)
            {
                LOGGER.error("Failed to build beginCreateSubscription request", e);
                GlobalMessages.addErrorMessage(model, "checkout.multi.paymentMethod.addPaymentDetails.generalError");
                model.addAttribute("isvPaymentDetailsForm", setupIsvPaymentForm);
                return "pages/checkout/multi/addPaymentMethodPage";
            }
        }

        final PaymentDetailsForm paymentDetailsForm = new PaymentDetailsForm();
        final AddressForm addressForm = new AddressForm();
        paymentDetailsForm.setBillingAddress(addressForm);
        model.addAttribute(paymentDetailsForm);

        final CartData cartData = getCheckoutFacade().getCheckoutCart();
        model.addAttribute(CART_DATA_ATTR, cartData);

        return "pages/checkout/multi/addPaymentMethodPage";
    }

    private SopPaymentDetailsForm getSopPaymentDetailsForm(final Model model)
    {
        final Optional<AddressData> billingAddressOpt = paymentInfoFacade.fetchAddressFromPaymentInfo();
        if (model.containsAttribute("isvPaymentDetailsForm"))
        {
            return (SopPaymentDetailsForm) model.asMap().get("isvPaymentDetailsForm");
        }
        else if (billingAddressOpt.isPresent())
        {
            final AddressData billingAddress = billingAddressOpt.get();
            final SopPaymentDetailsForm sopPaymentDetailsForm = new SopPaymentDetailsForm();
            sopPaymentDetailsForm.setUseDeliveryAddress(false);
            sopPaymentDetailsForm.setBillTo_firstName(billingAddress.getFirstName());
            sopPaymentDetailsForm.setBillTo_lastName(billingAddress.getLastName());
            sopPaymentDetailsForm.setBillTo_street1(billingAddress.getLine1());
            sopPaymentDetailsForm.setBillTo_street2(billingAddress.getLine2());
            sopPaymentDetailsForm.setBillTo_city(billingAddress.getTown());
            sopPaymentDetailsForm.setBillTo_postalCode(billingAddress.getPostalCode());
            sopPaymentDetailsForm.setBillTo_titleCode(billingAddress.getTitleCode());
            sopPaymentDetailsForm.setBillTo_country(billingAddress.getCountry().getIsocode());
            sopPaymentDetailsForm.setBillTo_phoneNumber(billingAddress.getPhone());
            return sopPaymentDetailsForm;
        }
        else
        {
            final SopPaymentDetailsForm sopPaymentDetailsForm = new SopPaymentDetailsForm();
            sopPaymentDetailsForm.setUseDeliveryAddress(true);
            return sopPaymentDetailsForm;
        }
    }

    @PostMapping(value = "/remove")
    @RequireHardLogIn
    public String remove(@RequestParam(value = "paymentInfoId") final String paymentMethodId,
            final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException
    {
        getUserFacade().unlinkCCPaymentInfo(paymentMethodId);
        GlobalMessages.addFlashMessage(redirectAttributes, CONF_MESSAGES_HOLDER,
                "text.account.profile.paymentCart.removed");
        return getCheckoutStep().currentStep();
    }

    /**
     * This method gets called when the "Use These Payment Details" button is clicked. It sets the selected payment
     * method on the checkout facade and reloads the page highlighting the selected payment method.
     *
     * @param selectedPaymentMethodId
     *           - the id of the payment method to use.
     * @return - a URL to the page to load.
     */
    @GetMapping(value = "/choose")
    @RequireHardLogIn
    public String doSelectPaymentMethod(@RequestParam("selectedPaymentMethodId") final String selectedPaymentMethodId)
    {
        if (StringUtils.isNotBlank(selectedPaymentMethodId))
        {
            getCheckoutFacade().setPaymentDetails(selectedPaymentMethodId);
        }
        return getCheckoutStep().nextStep();
    }

    @GetMapping(value = "/back")
    @RequireHardLogIn
    @Override
    public String back(final RedirectAttributes redirectAttributes)
    {
        return getCheckoutStep().previousStep();
    }

    @GetMapping(value = "/next")
    @RequireHardLogIn
    @Override
    public String next(final RedirectAttributes redirectAttributes)
    {
        return getCheckoutStep().nextStep();
    }

    protected void setupAddPaymentPage(final Model model) throws CMSItemNotFoundException
    {
        model.addAttribute("metaRobots", "noindex,nofollow");
        model.addAttribute("hasNoPaymentInfo", getCheckoutFlowFacade().hasNoPaymentInfo());
        prepareDataForPage(model);
        model.addAttribute(BREADCRUMBS_KEY,
                getResourceBreadcrumbBuilder().getBreadcrumbs("checkout.multi.paymentMethod.breadcrumb"));
        final ContentPageModel contentPage = getContentPageForLabelOrId(MULTI_CHECKOUT_SUMMARY_CMS_PAGE_LABEL);
        storeCmsPageInModel(model, contentPage);
        setUpMetaDataForContentPage(model, contentPage);
        setCheckoutStepLinksForModel(model, getCheckoutStep());
    }

    protected void setupIsvPaymentForm(final SopPaymentDetailsForm paymentDetailsForm, final Model model)
    {
        try
        {
            final PaymentData paymentData = getCreditCardPaymentFacade().beginCreatePayment(
                    "/checkout/multi/isv/response");

            model.addAttribute("silentOrderPageData", paymentData);
            paymentDetailsForm.setParameters(paymentData.getParameters());
            model.addAttribute("paymentFormUrl", paymentData.getPostUrl());
        }
        catch (final IllegalArgumentException e)
        {
            model.addAttribute("paymentFormUrl", "");
            model.addAttribute("silentOrderPageData", null);
            LOGGER.warn("Failed to set up silent order post page", e);
            GlobalMessages.addErrorMessage(model, "checkout.multi.sop.globalError");
        }

        final CartData cartData = getCheckoutFacade().getCheckoutCart();
        model.addAttribute("silentOrderPostForm", new PaymentDetailsForm());
        model.addAttribute(CART_DATA_ATTR, cartData);
        model.addAttribute("deliveryAddress", cartData.getDeliveryAddress());
        model.addAttribute("isvPaymentDetailsForm", paymentDetailsForm);
        if (StringUtils.isNotBlank(paymentDetailsForm.getBillTo_country()))
        {
            model.addAttribute("regions",
                    getI18NFacade().getRegionsForCountryIso(paymentDetailsForm.getBillTo_country()));
            model.addAttribute("country", paymentDetailsForm.getBillTo_country());
        }
    }

    protected CheckoutStep getCheckoutStep()
    {
        return getCheckoutStep(PAYMENT_METHOD);
    }

    private CreditCardPaymentFacade getCreditCardPaymentFacade()
    {
        return (CreditCardPaymentFacade) getPaymentFacade();
    }
}
