package isv.sap.payment.addon.controllers.pages.checkout.payment.paypal;

import javax.annotation.Resource;

import com.google.common.base.Preconditions;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractCheckoutController;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import isv.sap.payment.addon.facade.PayPalPaymentFacade;
import isv.sap.payment.commercefacades.order.PaymentCheckoutFacade;

@Controller
@RequestMapping(path = "/checkout/payment/paypal")
public class PayPalController extends AbstractCheckoutController
{
    protected static final String PAYMENT_ERROR_URL = "/checkout/multi/summary/view/payment/error";

    private static final Logger LOG = LoggerFactory.getLogger(PayPalController.class);

    @Resource
    private CartService cartService;

    @Resource(name = "isv.sap.payment.paymentCheckoutFacade")
    private PaymentCheckoutFacade paymentCheckoutFacade;

    @Resource
    private PayPalPaymentFacade payPalPaymentFacade;

    @RequestMapping(path = "/startFlow", method = RequestMethod.GET)
    public String startFlow(final Model model)
    {
        try
        {
            final String redirectUrl = payPalPaymentFacade
                    .executePayPalExpressCheckoutCreateSessionRequest(cartService.getSessionCart());

            return REDIRECT_PREFIX + redirectUrl;
        }
        catch (Exception error)
        {
            LOG.error(error.getMessage(), error);
            GlobalMessages.addErrorMessage(model, "checkout.place.order.payment.error");
            return REDIRECT_PREFIX + PAYMENT_ERROR_URL;
        }
    }

    @RequestMapping(path = "/handleResponse", method = RequestMethod.GET)
    public String handleResponse(@RequestParam(name = "token") final String token,
            @RequestParam(name = "PayerID") final String payerId)
    {
        try
        {
            Preconditions.checkArgument(StringUtils.isNotBlank(token), "Paypal token can't be blank");
            Preconditions.checkArgument(StringUtils.isNotBlank(payerId), "Paypal PayerID can't be blank");

            final CartModel sessionCart = cartService.getSessionCart();

            if (payPalPaymentFacade.authorizePayPalPayment(sessionCart, token))
            {
                final AbstractOrderData orderData = paymentCheckoutFacade.performPlaceOrder(sessionCart);
                return REDIRECT_PREFIX + "/checkout/orderConfirmation/" + getOrderId(orderData);
            }
        }
        catch (final Exception ex)
        {
            LOG.error("Exception happened during processing paypal response", ex);
        }
        return REDIRECT_PREFIX + PAYMENT_ERROR_URL;
    }

    private String getOrderId(final AbstractOrderData orderData)
    {
        return getCheckoutCustomerStrategy().isAnonymousCheckout() ? orderData.getGuid() : orderData.getCode();
    }

    public void setCartService(final CartService cartService)
    {
        this.cartService = cartService;
    }

    public void setPaymentCheckoutFacade(final PaymentCheckoutFacade paymentCheckoutFacade)
    {
        this.paymentCheckoutFacade = paymentCheckoutFacade;
    }

    public void setPayPalPaymentFacade(final PayPalPaymentFacade payPalPaymentFacade)
    {
        this.payPalPaymentFacade = payPalPaymentFacade;
    }
}
