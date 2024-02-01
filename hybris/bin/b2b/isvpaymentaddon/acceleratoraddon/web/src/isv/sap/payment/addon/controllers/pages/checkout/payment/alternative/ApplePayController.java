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

import isv.sap.payment.addon.facade.ApplePayPaymentFacade;
import isv.sap.payment.commercefacades.order.PaymentCheckoutFacade;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping(path = "/checkout/payment/ap/applepay")
public class ApplePayController extends AbstractCheckoutController
{
    static final String PAYMENT_ERROR_URL = "/checkout/multi/summary/view/payment/error";

    private static final Logger LOG = LoggerFactory.getLogger(ApplePayController.class);

    @Resource
    private ApplePayPaymentFacade applePayPaymentFacade;

    @Resource
    private CartService cartService;

    @Resource(name = "isv.sap.payment.paymentCheckoutFacade")
    private PaymentCheckoutFacade paymentCheckoutFacade;

    @RequestMapping(value = "/validate", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validateMerchant(final String validationUrl)
    {
        return ResponseEntity.ok(applePayPaymentFacade.createApplePaySession(validationUrl));
    }

    @RequestMapping(value = "/placeOrder", method = POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> placeOrder(@RequestBody final Map paymentToken)
    {
        final CartModel sessionCart = cartService.getSessionCart();

        try
        {
            if (applePayPaymentFacade.authorizeApplePayPayment(paymentToken, sessionCart))
            {
                final AbstractOrderData orderData = paymentCheckoutFacade.performPlaceOrder(sessionCart);

                return ResponseEntity.ok("/checkout/orderConfirmation/" + getOrderId(orderData));
            }
        }
        catch (final Exception e)
        {
            LOG.error("Error while processing ApplePay placeOrder", e);
        }

        return ResponseEntity.unprocessableEntity().body(PAYMENT_ERROR_URL);
    }

    private String getOrderId(final AbstractOrderData orderData)
    {
        return getCheckoutCustomerStrategy().isAnonymousCheckout() ? orderData.getGuid() : orderData.getCode();
    }

    public void setApplePayPaymentFacade(final ApplePayPaymentFacade applePayPaymentFacade)
    {
        this.applePayPaymentFacade = applePayPaymentFacade;
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
