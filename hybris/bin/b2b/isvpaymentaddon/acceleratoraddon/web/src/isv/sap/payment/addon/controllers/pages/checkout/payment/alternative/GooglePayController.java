package isv.sap.payment.addon.controllers.pages.checkout.payment.alternative;

import java.util.Map;
import javax.annotation.Resource;

import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractCheckoutController;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import isv.sap.payment.addon.facade.GooglePayPaymentFacade;
import isv.sap.payment.commercefacades.order.PaymentCheckoutFacade;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping(path = "/checkout/payment/ap/googlepay")
public class GooglePayController extends AbstractCheckoutController
{
    protected static final String PAYMENT_ERROR_URL = "/checkout/multi/summary/view/payment/error";

    private static final Logger LOG = LoggerFactory.getLogger(GooglePayController.class);

    @Resource
    private GooglePayPaymentFacade googlePayPaymentFacade;

    @Resource
    private CartService cartService;

    @Resource(name = "isv.sap.payment.paymentCheckoutFacade")
    private PaymentCheckoutFacade paymentCheckoutFacade;

    @RequestMapping(value = "/placeOrder", method = POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> placeOrder(@RequestBody final Map paymentData)
    {
        final CartModel sessionCart = cartService.getSessionCart();

        try
        {
            if (googlePayPaymentFacade.authorizeGooglePayPayment(paymentData, sessionCart))
            {
                final AbstractOrderData orderData = paymentCheckoutFacade.performPlaceOrder(sessionCart);

                return ResponseEntity.ok("/checkout/orderConfirmation/" + getOrderId(orderData));
            }
        }
        catch (final Exception e)
        {
            LOG.error("Error while processing Google Pay placeOrder", e);
        }

        return ResponseEntity.unprocessableEntity().body(PAYMENT_ERROR_URL);
    }

    private String getOrderId(final AbstractOrderData orderData)
    {
        return getCheckoutCustomerStrategy().isAnonymousCheckout() ? orderData.getGuid() : orderData.getCode();
    }

    public void setGooglePayPaymentFacade(final GooglePayPaymentFacade googlePayPaymentFacade)
    {
        this.googlePayPaymentFacade = googlePayPaymentFacade;
    }

    public void setCartService(final CartService cartService)
    {
        this.cartService = cartService;
    }

    public void setPaymentCheckoutFacade(final PaymentCheckoutFacade paymentCheckoutFacade)
    {
        this.paymentCheckoutFacade = paymentCheckoutFacade;
    }
}
