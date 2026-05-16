package isv.sap.payment.addon.controllers.pages.checkout.payment.alternative;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;

import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractCheckoutController;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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

    // Allowlist of legitimate Apple Pay merchant validation domains
    private static final Set<String> ALLOWED_APPLE_PAY_HOSTS = new HashSet<>(Arrays.asList(
            "apple-pay-gateway.apple.com",
            "cn-apple-pay-gateway.apple.com",
            "apple-pay-gateway-cert.apple.com",
            "cn-apple-pay-gateway-cert.apple.com"
    ));

    @Resource
    private ApplePayPaymentFacade applePayPaymentFacade;

    @Resource
    private CartService cartService;

    @Resource(name = "isv.sap.payment.paymentCheckoutFacade")
    private PaymentCheckoutFacade paymentCheckoutFacade;

    @RequireHardLogIn
    @RequestMapping(value = "/validate", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validateMerchant(final String validationUrl)
    {
        // Validate that validationUrl is provided
        if (validationUrl == null || validationUrl.trim().isEmpty())
        {
            LOG.warn("Apple Pay validation attempted with empty validationUrl");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Parse and validate the URL to prevent SSRF attacks
        try
        {
            final URL url = new URL(validationUrl);

            // Enforce HTTPS protocol
            if (!"https".equalsIgnoreCase(url.getProtocol()))
            {
                LOG.warn("Apple Pay validation attempted with non-HTTPS URL: {}");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            // Validate against Apple Pay domain allowlist to prevent SSRF attacks
            final String host = url.getHost().toLowerCase();
            if (!ALLOWED_APPLE_PAY_HOSTS.contains(host))
            {
                LOG.warn("Apple Pay validation attempted with unauthorized host: {}");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Additional security check: ensure no credentials in URL
            if (url.getUserInfo() != null)
            {
                LOG.warn("Apple Pay validation attempted with credentials in URL");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            return ResponseEntity.ok(applePayPaymentFacade.createApplePaySession(validationUrl));
        }
        catch (final MalformedURLException e)
        {
            LOG.warn("Apple Pay validation attempted with malformed URL: {}", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        catch (final Exception e)
        {
            LOG.error("Error during Apple Pay merchant validation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
