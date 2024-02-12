package isv.sap.payment.addon.b2b.controllers.pages.checkout.steps;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Resource;

import de.hybris.platform.acceleratorservices.checkout.pci.impl.ConfiguredCheckoutPciStrategy;
import de.hybris.platform.acceleratorservices.enums.CheckoutPciOptionEnum;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.PreValidateCheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.PreValidateQuoteCheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.checkout.steps.AbstractCheckoutStepController;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.acceleratorstorefrontcommons.forms.SopPaymentDetailsForm;
import de.hybris.platform.b2bacceleratoraddon.forms.PlaceOrderForm;
import de.hybris.platform.b2bacceleratorfacades.api.cart.CheckoutFacade;
import de.hybris.platform.b2bacceleratorfacades.checkout.data.PlaceOrderData;
import de.hybris.platform.b2bacceleratorfacades.exception.EntityValidationException;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BReplenishmentRecurrenceEnum;
import de.hybris.platform.b2bacceleratorfacades.order.data.ScheduledCartData;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.order.data.CardTypeData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.ProductOption;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.cronjob.enums.DayOfWeek;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import isv.sap.payment.fraud.FraudFacade;

import static de.hybris.platform.b2bacceleratorservices.enums.CheckoutPaymentType.ACCOUNT;

@Controller
@RequestMapping(value = "/checkout/multi/summary")
public class SummaryCheckoutStepController extends AbstractCheckoutStepController
{
    private static final Logger LOG = LoggerFactory.getLogger(SummaryCheckoutStepController.class);

    private static final String SUMMARY = "summary";

    private static final String REDIRECT_URL_REPLENISHMENT_CONFIRMATION = REDIRECT_PREFIX
            + "/checkout/replenishment/confirmation/";

    private static final String TEXT_STORE_DATEFORMAT_KEY = "text.store.dateformat";

    private static final String DEFAULT_DATEFORMAT = "MM/dd/yyyy";

    private static final String RESPONSE_ACTION = "responseAction";

    private static final String REPLENISHMENT_RECURRENCE_PROP = "storefront.replenishment.recurrence.default";

    private static final String REPLENISHMENT_NDAYS_PROP = "storefront.replenishment.nDays.default";

    private static final String REPLENISHMENT_NDAYS_OF_WEEK_PROP = "storefront.replenishment.nDaysOfWeek.default";

    private static final String REPLENISHMENT_FORM_NDAYS_RANGE_PROP = "storefront.replenishment.form.nDays.range";

    private static final String REPLENISHMENT_FORM_NTHDAY_OF_MONTH_RANGE_PROP = "storefront.replenishment.form.nthDayOfMonth.range";

    private static final String REPLENISHMENT_FORM_NTHWEEK_RANGE_PROP = "storefront.replenishment.form.nthWeek.range";

    @Resource
    private ConfiguredCheckoutPciStrategy checkoutPciStrategy;

    @Resource(name = "isv.sap.payment.fraudFacade")
    private FraudFacade fraudFacade;

    @Resource(name = "isvCardTypes")
    private Map<String, String> isvSopCardTypes;

    @Resource
    private ConfigurationService configurationService;

    @RequestMapping(value = "/view/payment/error", method = RequestMethod.GET)
    @RequireHardLogIn
    @PreValidateCheckoutStep(checkoutStep = SUMMARY)
    public String enterStepWithPaymentError(final Model model, final RedirectAttributes redirectModel)
    {
        GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER,
                "checkout.place.order.payment.error");
        return "redirect:/checkout/multi/summary/view";
    }

    @RequestMapping(value = "/view", method = RequestMethod.GET)
    @RequireHardLogIn
    @Override
    @PreValidateQuoteCheckoutStep
    @PreValidateCheckoutStep(checkoutStep = SUMMARY)
    public String enterStep(final Model model, final RedirectAttributes redirectAttributes)
            throws CMSItemNotFoundException, CommerceCartModificationException
    {
        final CartData cartData = getCheckoutFacade().getCheckoutCart();
        if (cartData.getEntries() != null && !cartData.getEntries().isEmpty())
        {
            for (final OrderEntryData entry : cartData.getEntries())
            {
                final String productCode = entry.getProduct().getCode();
                final ProductData product = getProductFacade().getProductForCodeAndOptions(productCode, Arrays.asList(
                        ProductOption.BASIC, ProductOption.PRICE, ProductOption.VARIANT_MATRIX_BASE,
                        ProductOption.PRICE_RANGE));
                entry.setProduct(product);
            }
        }

        model.addAttribute("cartData", cartData);
        model.addAttribute("allItems", cartData.getEntries());
        model.addAttribute("deliveryAddress", cartData.getDeliveryAddress());
        model.addAttribute("deliveryMode", cartData.getDeliveryMode());
        model.addAttribute("paymentInfo", cartData.getPaymentInfo());
        final String[] nDaysRange = configurationService.getConfiguration()
            .getString(REPLENISHMENT_FORM_NDAYS_RANGE_PROP, "1,30").split(",");
        final String[] nthDayOfMonthRange = configurationService.getConfiguration()
            .getString(REPLENISHMENT_FORM_NTHDAY_OF_MONTH_RANGE_PROP, "1,31").split(",");
        final String[] nthWeek = configurationService.getConfiguration()
            .getString(REPLENISHMENT_FORM_NTHWEEK_RANGE_PROP, "1,12").split(",");
        model.addAttribute("nDays", getNumberRange(Integer.parseInt(nDaysRange[0]), Integer.parseInt(nDaysRange[1])));
        model.addAttribute("nthDayOfMonth", getNumberRange(Integer.parseInt(nthDayOfMonthRange[0]),
            Integer.parseInt(nthDayOfMonthRange[1])));
        model.addAttribute("nthWeek", getNumberRange(Integer.parseInt(nthWeek[0]), Integer.parseInt(nthWeek[1])));
        model.addAttribute("daysOfWeek", getB2BCheckoutFacade().getDaysOfWeekForReplenishmentCheckoutSummary());
        model.addAttribute("deviceFingerPrint", fraudFacade.getDeviceFingerPrint());

        // Only request the security code if the SubscriptionPciOption is set to Default.
        final boolean requestSecurityCode = CheckoutPciOptionEnum.DEFAULT
                .equals(getCheckoutFlowFacade().getSubscriptionPciOption());
        model.addAttribute("requestSecurityCode", requestSecurityCode);

        if (!model.containsAttribute("placeOrderForm"))
        {
            final PlaceOrderForm placeOrderForm = new PlaceOrderForm();
            final String defaultRecurrence = configurationService.getConfiguration().getString(REPLENISHMENT_RECURRENCE_PROP, "MONTHLY");
            final String defaultNdays = configurationService.getConfiguration().getString(REPLENISHMENT_NDAYS_PROP, "14");
            final String defaultNdaysWeek = configurationService.getConfiguration().getString(REPLENISHMENT_NDAYS_OF_WEEK_PROP, "MONDAY");
            placeOrderForm.setReplenishmentRecurrence(B2BReplenishmentRecurrenceEnum.valueOf(defaultRecurrence));
            placeOrderForm.setnDays(defaultNdays);
            final List<DayOfWeek> daysOfWeek = List.of(DayOfWeek.valueOf(defaultNdaysWeek));
            placeOrderForm.setnDaysOfWeek(daysOfWeek);
            model.addAttribute("placeOrderForm", placeOrderForm);
        }

        storeCmsPageInModel(model, getContentPageForLabelOrId(MULTI_CHECKOUT_SUMMARY_CMS_PAGE_LABEL));
        setUpMetaDataForContentPage(model, getContentPageForLabelOrId(MULTI_CHECKOUT_SUMMARY_CMS_PAGE_LABEL));
        model.addAttribute(WebConstants.BREADCRUMBS_KEY,
                getResourceBreadcrumbBuilder().getBreadcrumbs("checkout.multi.summary.breadcrumb"));
        model.addAttribute("metaRobots", "noindex,nofollow");
        setCheckoutStepLinksForModel(model, getCheckoutStep());

        model.addAttribute("paymentPciType", checkoutPciStrategy.getSubscriptionPciOption());

        setupCardPayment(model);

        return "addon:/isvb2bpaymentaddon/pages/checkout/multi/checkoutSummaryPage";
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
            months.add(new SelectOption(String.valueOf(i), String.format("%02d", i)));
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
        final Collection<CardTypeData> sopCardTypes = new ArrayList<CardTypeData>();

        final List<CardTypeData> supportedCardTypes = getCheckoutFacade().getSupportedCardTypes();
        for (final CardTypeData supportedCardType : supportedCardTypes)
        {
            // Add credit cards for all supported cards that have mappings for isv SOP
            if (isvSopCardTypes.containsKey(supportedCardType.getCode()))
            {
                sopCardTypes.add(createCardTypeData(isvSopCardTypes.get(supportedCardType.getCode()),
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

    protected List<String> getNumberRange(final int startNumber, final int endNumber)
    {
        final List<String> numbers = new ArrayList<>();
        for (int number = startNumber; number <= endNumber; number++)
        {
            numbers.add(String.valueOf(number));
        }
        return numbers;
    }

    @RequestMapping(value = "/placeOrder")
    @PreValidateQuoteCheckoutStep
    @RequireHardLogIn
    public String placeOrder(@ModelAttribute("placeOrderForm") final PlaceOrderForm placeOrderForm, final Model model,
            final RedirectAttributes redirectModel)
            throws CMSItemNotFoundException, CommerceCartModificationException
    {
        final String paymentType = getCartFacade().getSessionCart().getPaymentType().getCode();

        if (!ACCOUNT.getCode().equals(paymentType))
        {
            LOG.error("Unsupported payment type.");
            return "redirect:/checkout/multi/summary/view/payment/error";
        }

        if (validateOrderForm(placeOrderForm, model))
        {
            return enterStep(model, redirectModel);
        }

        final PlaceOrderData placeOrderData = new PlaceOrderData();

        placeOrderData.setNDays(placeOrderForm.getnDays());
        placeOrderData.setNDaysOfWeek(placeOrderForm.getnDaysOfWeek());
        placeOrderData.setNthDayOfMonth(placeOrderForm.getNthDayOfMonth());
        placeOrderData.setNWeeks(placeOrderForm.getnWeeks());
        placeOrderData.setReplenishmentOrder(placeOrderForm.isReplenishmentOrder());
        placeOrderData.setReplenishmentRecurrence(placeOrderForm.getReplenishmentRecurrence());
        placeOrderData.setReplenishmentStartDate(placeOrderForm.getReplenishmentStartDate());

        placeOrderData.setSecurityCode(placeOrderForm.getSecurityCode());
        placeOrderData.setTermsCheck(placeOrderForm.isTermsCheck());

        try
        {
            final AbstractOrderData orderData = getB2BCheckoutFacade().placeOrder(placeOrderData);
            return redirectToOrderConfirmationPage(placeOrderData, orderData);
        }
        catch (final EntityValidationException e)
        {
            LOG.error("Failed to place Order", e);
            GlobalMessages.addErrorMessage(model, e.getLocalizedMessage());

            placeOrderForm.setTermsCheck(false);
            model.addAttribute(placeOrderForm);

            return enterStep(model, redirectModel);
        }
        catch (final Exception e)
        {
            LOG.error("Failed to place Order", e);
            GlobalMessages.addErrorMessage(model, "checkout.placeOrder.failed");

            return enterStep(model, redirectModel);
        }
    }

    /**
     * Validates the order form before to filter out invalid order states
     *
     * @param placeOrderForm
     *           The spring form of the order being submitted
     * @param model
     *           A spring Model
     * @return True if the order form is invalid and false if everything is valid.
     */
    protected boolean validateOrderForm(final PlaceOrderForm placeOrderForm, final Model model)
    {
        final String securityCode = placeOrderForm.getSecurityCode();
        boolean invalid = false;

        if (getCheckoutFlowFacade().hasNoDeliveryAddress())
        {
            GlobalMessages.addErrorMessage(model, "checkout.deliveryAddress.notSelected");
            invalid = true;
        }

        if (getCheckoutFlowFacade().hasNoDeliveryMode())
        {
            GlobalMessages.addErrorMessage(model, "checkout.deliveryMethod.notSelected");
            invalid = true;
        }

        if (getCheckoutFlowFacade().hasNoPaymentInfo())
        {
            GlobalMessages.addErrorMessage(model, "checkout.paymentMethod.notSelected");
            invalid = true;
        }
        else
        {
            if (CheckoutPciOptionEnum.DEFAULT.equals(getCheckoutFlowFacade().getSubscriptionPciOption())
                    && StringUtils.isBlank(securityCode))
            {
                GlobalMessages.addErrorMessage(model, "checkout.paymentMethod.noSecurityCode");
                invalid = true;
            }
        }

        if (!placeOrderForm.isTermsCheck())
        {
            GlobalMessages.addErrorMessage(model, "checkout.error.terms.not.accepted");
            invalid = true;
            return invalid;
        }
        final CartData cartData = getCheckoutFacade().getCheckoutCart();

        if (!getCheckoutFacade().containsTaxValues())
        {
            LOG.error(
                    "Cart {} does not have any tax values, which means the tax calculation was not properly done, placement of order can't continue",
                    cartData.getCode());
            GlobalMessages.addErrorMessage(model, "checkout.error.tax.missing");
            invalid = true;
        }

        if (!cartData.isCalculated())
        {
            LOG.error("Cart {} has a calculated flag of FALSE, placement of order can't continue", cartData.getCode());
            GlobalMessages.addErrorMessage(model, "checkout.error.cart.notcalculated");
            invalid = true;
        }

        return invalid;
    }

    protected CheckoutFacade getB2BCheckoutFacade()
    {
        return (CheckoutFacade) this.getCheckoutFacade();
    }

    protected String redirectToOrderConfirmationPage(final PlaceOrderData placeOrderData,
            final AbstractOrderData orderData)
    {
        if (Boolean.TRUE.equals(placeOrderData.getReplenishmentOrder()) && (orderData instanceof ScheduledCartData))
        {
            return REDIRECT_URL_REPLENISHMENT_CONFIRMATION + ((ScheduledCartData) orderData).getJobCode();
        }
        return REDIRECT_URL_ORDER_CONFIRMATION + orderData.getCode();
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

    @InitBinder
    protected void initBinder(final ServletRequestDataBinder binder)
    {
        final Locale currentLocale = getI18nService().getCurrentLocale();
        final String formatString = getMessageSource().getMessage(TEXT_STORE_DATEFORMAT_KEY, null, DEFAULT_DATEFORMAT,
                currentLocale);
        final DateFormat dateFormat = new SimpleDateFormat(formatString, currentLocale);
        final CustomDateEditor editor = new CustomDateEditor(dateFormat, true);
        binder.registerCustomEditor(Date.class, editor);
    }
}
