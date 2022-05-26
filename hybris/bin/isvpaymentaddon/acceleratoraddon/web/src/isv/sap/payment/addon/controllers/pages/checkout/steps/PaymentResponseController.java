package isv.sap.payment.addon.controllers.pages.checkout.steps;

import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import de.hybris.platform.acceleratorfacades.payment.data.PaymentSubscriptionResultData;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.acceleratorstorefrontcommons.forms.SopPaymentDetailsForm;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.user.data.AddressData;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import isv.sap.payment.addon.controllers.validation.SopPaymentDetailsValidator;
import isv.sap.payment.addon.facade.CreditCardPaymentFacade;

import static de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages.ERROR_MESSAGES_HOLDER;

@Controller
@RequestMapping(value = "/checkout/multi/isv")
public class PaymentResponseController extends PaymentMethodCheckoutStepController
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentResponseController.class);

    @Resource(name = "sopPaymentDetailsFormValidator")
    private SopPaymentDetailsValidator sopPaymentDetailsValidator;

    @RequestMapping(value = "/response", method = RequestMethod.POST)
    @RequireHardLogIn
    public String doHandleIsvResponse(final HttpServletRequest request,
            @Valid final SopPaymentDetailsForm isvPaymentDetailsForm,
            final RedirectAttributes redirectAttributes, final BindingResult bindingResult)
            throws CMSItemNotFoundException
    {
        sopPaymentDetailsValidator.validate(isvPaymentDetailsForm, bindingResult);
        if (bindingResult.hasErrors())
        {
            GlobalMessages
                    .addFlashMessage(redirectAttributes, ERROR_MESSAGES_HOLDER, "address.error.formentry.invalid");
            redirectAttributes.addFlashAttribute("isvPaymentDetailsForm", isvPaymentDetailsForm);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.isvPaymentDetailsForm",
                    bindingResult);

            return getCheckoutStep().currentStep();
        }

        final Map<String, String> resultMap = getRequestParameterMap(request);

        final boolean savePaymentInfo = isvPaymentDetailsForm.isSavePaymentInfo()
                || getCheckoutCustomerStrategy().isAnonymousCheckout();

        final PaymentSubscriptionResultData paymentSubscriptionResultData = getCreditCardPaymentFacade()
                .completeCreatePayment(resultMap, savePaymentInfo);

        if (paymentSubscriptionResultData == null || !paymentSubscriptionResultData.isSuccess())
        {
            LOGGER.error("Failed to create payment subscription. Please check the log files for more information");

            return REDIRECT_URL_ERROR;
        }

        return getCheckoutStep().nextStep();
    }

    @RequestMapping(value = "/billingaddressform", method = RequestMethod.GET)
    public String getCountryAddressForm(@RequestParam("countryIsoCode") final String countryIsoCode,
            @RequestParam("useDeliveryAddress") final boolean useDeliveryAddress, final Model model)
    {
        model.addAttribute("supportedCountries", getCountries());
        model.addAttribute("regions", getI18NFacade().getRegionsForCountryIso(countryIsoCode));
        model.addAttribute("country", countryIsoCode);

        final SopPaymentDetailsForm isvPaymentDetailsForm = new SopPaymentDetailsForm();
        model.addAttribute("sopPaymentDetailsForm", isvPaymentDetailsForm);
        if (useDeliveryAddress)
        {
            final AddressData deliveryAddress = getCheckoutFacade().getCheckoutCart().getDeliveryAddress();

            if (deliveryAddress.getRegion() != null && !StringUtils.isEmpty(deliveryAddress.getRegion().getIsocode()))
            {
                isvPaymentDetailsForm.setBillTo_state(deliveryAddress.getRegion().getIsocodeShort());
            }

            isvPaymentDetailsForm.setBillTo_titleCode(deliveryAddress.getTitleCode());
            isvPaymentDetailsForm.setBillTo_firstName(deliveryAddress.getFirstName());
            isvPaymentDetailsForm.setBillTo_lastName(deliveryAddress.getLastName());
            isvPaymentDetailsForm.setBillTo_street1(deliveryAddress.getLine1());
            isvPaymentDetailsForm.setBillTo_street2(deliveryAddress.getLine2());
            isvPaymentDetailsForm.setBillTo_city(deliveryAddress.getTown());
            isvPaymentDetailsForm.setBillTo_postalCode(deliveryAddress.getPostalCode());
            isvPaymentDetailsForm.setBillTo_country(deliveryAddress.getCountry().getIsocode());
            isvPaymentDetailsForm.setBillTo_phoneNumber(deliveryAddress.getPhone());
        }
        return "fragments/checkout/billingAddressForm";
    }

    private CreditCardPaymentFacade getCreditCardPaymentFacade()
    {
        return (CreditCardPaymentFacade) getPaymentFacade();
    }
}
