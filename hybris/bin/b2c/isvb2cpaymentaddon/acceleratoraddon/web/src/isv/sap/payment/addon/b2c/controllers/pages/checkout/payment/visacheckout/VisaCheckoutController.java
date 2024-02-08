package isv.sap.payment.addon.b2c.controllers.pages.checkout.payment.visacheckout;

import javax.annotation.Resource;

import com.google.common.base.Preconditions;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractCheckoutController;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import isv.sap.payment.addon.facade.VisaCheckoutPaymentFacade;
import isv.sap.payment.commercefacades.order.PaymentCheckoutFacade;

@Controller
@RequestMapping(path = "/checkout/payment/vc/success")
public class VisaCheckoutController extends AbstractCheckoutController
{
    protected static final String VISA_CHECKOUT_EXPRESS = "expressVisaCheckout";

    protected static final String VISA_CHECKOUT_CALL_ID = "callId";

    protected static final String PAYMENT_ERROR_URL = "/checkout/multi/summary/view/payment/error";

    protected static final String CHECKOUT_PAYMENT_URL = "/checkout/multi/summary/view";

    private static final Logger LOG = LoggerFactory.getLogger(VisaCheckoutController.class);

    @Resource(name = "isv.sap.payment.paymentCheckoutFacade")
    private PaymentCheckoutFacade paymentCheckoutFacade;

    @Resource
    private VisaCheckoutPaymentFacade visaCheckoutPaymentFacade;

    @Resource
    private CartService cartService;

    @RequireHardLogIn
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public String success(@RequestParam(name = VISA_CHECKOUT_CALL_ID) final String callId,
            @RequestParam(name = "expressCheckout") final boolean expressCheckout)
    {
        try
        {
            Preconditions.checkArgument(StringUtils.isNotBlank(callId), "Visa Checkout callId can't be blank");

            final CartModel sessionCart = cartService.getSessionCart();

            if (visaCheckoutPaymentFacade.authorizeVisaCheckoutPayment(sessionCart, callId, !expressCheckout))
            {
                final AbstractOrderData orderData = paymentCheckoutFacade.performPlaceOrder(sessionCart);

                return REDIRECT_URL_ORDER_CONFIRMATION + getOrderId(orderData);
            }
        }
        catch (final Exception ex)
        {
            LOG.error("Exception happened during processing visa checkout response", ex);
        }

        return REDIRECT_PREFIX + PAYMENT_ERROR_URL;
    }

    @RequireHardLogIn
    @RequestMapping(value = "/express", method = RequestMethod.POST)
    public String expressSuccess(@RequestParam(name = VISA_CHECKOUT_CALL_ID) final String callId,
            final RedirectAttributes redirectAttributes)
    {
        try
        {
            Preconditions.checkArgument(StringUtils.isNotBlank(callId), "Visa Checkout callId can't be blank");

            final CartModel sessionCart = cartService.getSessionCart();

            if (visaCheckoutPaymentFacade.updateCartAddressesWithVCGetData(sessionCart, callId))
            {
                redirectAttributes.addFlashAttribute(VISA_CHECKOUT_EXPRESS, true);
                redirectAttributes.addFlashAttribute(VISA_CHECKOUT_CALL_ID, callId);

                return REDIRECT_PREFIX + CHECKOUT_PAYMENT_URL;
            }
        }
        catch (final Exception ex)
        {
            LOG.error("Exception happened during processing visa checkout response", ex);
        }

        return REDIRECT_PREFIX + PAYMENT_ERROR_URL;
    }

    private String getOrderId(final AbstractOrderData orderData)
    {
        return getCheckoutCustomerStrategy().isAnonymousCheckout() ? orderData.getGuid() : orderData.getCode();
    }

    public void setPaymentCheckoutFacade(final PaymentCheckoutFacade paymentCheckoutFacade)
    {
        this.paymentCheckoutFacade = paymentCheckoutFacade;
    }

    public void setVisaCheckoutPaymentFacade(final VisaCheckoutPaymentFacade visaCheckoutPaymentFacade)
    {
        this.visaCheckoutPaymentFacade = visaCheckoutPaymentFacade;
    }

    public void setCartService(final CartService cartService)
    {
        this.cartService = cartService;
    }
}
