package isv.sap.payment.addon.b2b.controllers.pages.checkout.steps;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Resource;

import de.hybris.platform.acceleratorstorefrontcommons.annotations.PreValidateCheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.PreValidateQuoteCheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.ThirdPartyConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.checkout.steps.AbstractCheckoutStepController;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.b2bacceleratoraddon.forms.PaymentTypeForm;
import de.hybris.platform.b2bacceleratoraddon.forms.validation.PaymentTypeFormValidator;
import de.hybris.platform.b2bacceleratorfacades.api.cart.CheckoutFacade;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BPaymentTypeData;
import de.hybris.platform.b2bacceleratorservices.enums.CheckoutPaymentType;
import de.hybris.platform.b2bcommercefacades.company.B2BCostCenterFacade;
import de.hybris.platform.b2bcommercefacades.company.data.B2BCostCenterData;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(value = "/checkout/multi/payment-type")
public class PaymentTypeCheckoutStepController extends AbstractCheckoutStepController
{
    private static final String CHOOSE_PAYMENT_TYPE_PAGE = "addon:/isvb2bpaymentaddon/pages/checkout/multi/choosePaymentTypePage";

    private static final String PAYMENT_TYPE = "payment-type";

    @Resource(name = "b2bCheckoutFacade")
    private CheckoutFacade b2bCheckoutFacade;

    @Resource(name = "costCenterFacade")
    private B2BCostCenterFacade costCenterFacade;

    @Resource(name = "paymentTypeFormValidator")
    private PaymentTypeFormValidator paymentTypeFormValidator;

    @ModelAttribute("paymentTypes")
    public Collection<B2BPaymentTypeData> getAllB2BPaymentTypes()
    {
        return b2bCheckoutFacade.getPaymentTypes();
    }

    @ModelAttribute("costCenters")
    public List<? extends B2BCostCenterData> getVisibleActiveCostCenters()
    {
        final List<? extends B2BCostCenterData> costCenterData = costCenterFacade.getActiveCostCenters();
        return costCenterData == null ? Collections.<B2BCostCenterData>emptyList() : costCenterData;
    }

    @Override
    @RequestMapping(value = "/choose", method = RequestMethod.GET)
    @RequireHardLogIn
    @PreValidateQuoteCheckoutStep
    @PreValidateCheckoutStep(checkoutStep = PAYMENT_TYPE)
    public String enterStep(final Model model, final RedirectAttributes redirectAttributes)
        throws CMSItemNotFoundException, CommerceCartModificationException
    {
        final CartData cartData = getCheckoutFacade().getCheckoutCart();
        model.addAttribute("cartData", cartData);
        model.addAttribute("paymentTypeForm", preparePaymentTypeForm(cartData));
        prepareDataForChooseStep(model);

        return CHOOSE_PAYMENT_TYPE_PAGE;
    }

    @RequestMapping(value = "/choose", method = RequestMethod.POST)
    @RequireHardLogIn
    public String choose(@ModelAttribute final PaymentTypeForm paymentTypeForm, final BindingResult bindingResult,
        final Model model) throws CMSItemNotFoundException, CommerceCartModificationException
    {
        paymentTypeFormValidator.validate(paymentTypeForm, bindingResult);

        if (bindingResult.hasErrors())
        {
            GlobalMessages.addErrorMessage(model, "checkout.error.paymenttype.formentry.invalid");
            model.addAttribute("paymentTypeForm", paymentTypeForm);
            prepareDataForChooseStep(model);

            return CHOOSE_PAYMENT_TYPE_PAGE;
        }

        updateCheckoutCart(paymentTypeForm);

        checkAndSelectDeliveryAddress(paymentTypeForm);

        return getCheckoutStep().nextStep();
    }

    protected void updateCheckoutCart(final PaymentTypeForm paymentTypeForm)
    {
        final CartData cartData = new CartData();

        final B2BPaymentTypeData paymentTypeData = new B2BPaymentTypeData();
        paymentTypeData.setCode(paymentTypeForm.getPaymentType());

        cartData.setPaymentType(paymentTypeData);

        if (CheckoutPaymentType.ACCOUNT.getCode().equals(cartData.getPaymentType().getCode()))
        {
            final B2BCostCenterData costCenter = new B2BCostCenterData();
            costCenter.setCode(paymentTypeForm.getCostCenterId());

            cartData.setCostCenter(costCenter);
        }

        cartData.setPurchaseOrderNumber(paymentTypeForm.getPurchaseOrderNumber());

        b2bCheckoutFacade.updateCheckoutCart(cartData);
    }

    @RequestMapping(value = "/next", method = RequestMethod.GET)
    @RequireHardLogIn
    @Override
    public String next(final RedirectAttributes redirectAttributes)
    {
        return getCheckoutStep().nextStep();
    }

    @RequestMapping(value = "/back", method = RequestMethod.GET)
    @RequireHardLogIn
    @Override
    public String back(final RedirectAttributes redirectAttributes)
    {
        return getCheckoutStep().previousStep();
    }

    protected PaymentTypeForm preparePaymentTypeForm(final CartData cartData)
    {
        final PaymentTypeForm paymentTypeForm = new PaymentTypeForm();

        if (cartData.getPaymentType() != null && StringUtils.isNotBlank(cartData.getPaymentType().getCode()))
        {
            paymentTypeForm.setPaymentType(cartData.getPaymentType().getCode());
        }
        else
        {
            paymentTypeForm.setPaymentType(CheckoutPaymentType.ACCOUNT.getCode());
        }

        if (cartData.getCostCenter() != null && StringUtils.isNotBlank(cartData.getCostCenter().getCode()))
        {
            paymentTypeForm.setCostCenterId(cartData.getCostCenter().getCode());
        }
        else if (!CollectionUtils.isEmpty(getVisibleActiveCostCenters()) && getVisibleActiveCostCenters().size() == 1)
        {
            paymentTypeForm.setCostCenterId(getVisibleActiveCostCenters().get(0).getCode());
        }

        paymentTypeForm.setPurchaseOrderNumber(cartData.getPurchaseOrderNumber());
        return paymentTypeForm;
    }

    protected void checkAndSelectDeliveryAddress(final PaymentTypeForm paymentTypeForm)
    {
        if (CheckoutPaymentType.ACCOUNT.getCode().equals(paymentTypeForm.getPaymentType()))
        {
            final List<? extends AddressData> deliveryAddresses = getCheckoutFacade()
                .getSupportedDeliveryAddresses(true);
            if (deliveryAddresses.size() == 1)
            {
                getCheckoutFacade().setDeliveryAddress(deliveryAddresses.get(0));
            }
        }
    }

    protected CheckoutStep getCheckoutStep()
    {
        return getCheckoutStep(PAYMENT_TYPE);
    }

    protected void prepareDataForChooseStep(final Model model) throws CMSItemNotFoundException
    {
        prepareDataForPage(model);
        storeCmsPageInModel(model, getContentPageForLabelOrId(MULTI_CHECKOUT_SUMMARY_CMS_PAGE_LABEL));
        setUpMetaDataForContentPage(model, getContentPageForLabelOrId(MULTI_CHECKOUT_SUMMARY_CMS_PAGE_LABEL));
        model.addAttribute(WebConstants.BREADCRUMBS_KEY,
            getResourceBreadcrumbBuilder().getBreadcrumbs("checkout.multi.paymentType.breadcrumb"));
        model.addAttribute(ThirdPartyConstants.SeoRobots.META_ROBOTS, ThirdPartyConstants.SeoRobots.NOINDEX_NOFOLLOW);
        setCheckoutStepLinksForModel(model, getCheckoutStep());
    }
}
