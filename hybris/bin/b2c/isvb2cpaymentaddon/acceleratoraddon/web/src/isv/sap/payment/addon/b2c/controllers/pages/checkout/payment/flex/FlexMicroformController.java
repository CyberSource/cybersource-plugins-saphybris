package isv.sap.payment.addon.b2c.controllers.pages.checkout.payment.flex;

import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import com.cybersource.flex.sdk.CaptureContext;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractCheckoutController;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.util.Config;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

import isv.cjl.payment.service.flex.FlexService;
import isv.sap.payment.addon.facade.CreditCardPaymentFacade;
import isv.sap.payment.addon.utils.AjaxResponse;
import isv.sap.payment.commercefacades.order.PaymentCheckoutFacade;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static isv.sap.payment.constants.IsvPaymentConstants.ReasonCode.ENROLLED_CODE;
import static isv.sap.payment.constants.IsvPaymentConstants.ReasonCode.NOT_ENROLLED_CODE;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Controller
@RequestMapping(path = "/checkout/payment/flex")
public class FlexMicroformController extends AbstractCheckoutController
{
    private static final Logger LOG = LoggerFactory.getLogger(FlexMicroformController.class);

    private static final String URL_ORDER_CONFIRMATION = "/checkout/orderConfirmation/";

    private static final String URL_PAYMENT_FAILED = "/checkout/multi/summary/view/payment/error";

    private static final String FLEX_CAPTURE_CONTEXT_ATTRIBUTE = "captureContext";

    @Resource
    private CartService cartService;

    @Resource
    private CreditCardPaymentFacade creditCardPaymentFacade;

    @Resource(name = "isv.sap.payment.flexService")
    private FlexService flexService;

    @Resource(name = "isv.sap.payment.paymentCheckoutFacade")
    private PaymentCheckoutFacade paymentCheckoutFacade;

    @GetMapping(value = "/newJwk", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String newJwk(final HttpSession session, final UriComponentsBuilder uriComponentsBuilder)
    {
        final String targetOrigin = uriComponentsBuilder
                .replacePath(null).replaceQuery(null).userInfo(null).fragment(null)
                .build()
                .toUriString();

        final CaptureContext captureContext = flexService.createKey(targetOrigin);

        session.setAttribute(FLEX_CAPTURE_CONTEXT_ATTRIBUTE, captureContext.toString());

        return captureContext.toString();
    }

    @PostMapping(value = "/verifyToken", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity verifyToken(@RequestBody final String flexToken, final HttpSession session)
    {
        final String captureContext = (String) session.getAttribute(FLEX_CAPTURE_CONTEXT_ATTRIBUTE);

        checkNotNull(captureContext);

        return flexService.verifyAndGet(captureContext, flexToken)
                .map(transientToken -> ResponseEntity.ok(transientToken.getId()))
                .orElse(ResponseEntity.status(UNPROCESSABLE_ENTITY).build());
    }

    @PostMapping(path = "/pay")
    public String pay(@RequestParam(name = "card_flexToken") final String flexToken)
    {
        checkArgument(StringUtils.isNotBlank(flexToken), "flexToken is missing");


        boolean is3dsEnabled = false;
        String serviceName = Config.getString("isv.payment.payerAuthentication.3ds.enabled", StringUtils.EMPTY);

        if(serviceName.equals("true"))
        {
            is3dsEnabled = true;
        }
        if (!is3dsEnabled)
        {
            return REDIRECT_PREFIX + payAndPlaceOrder(flexToken, null, null);
        }
        else
        {
            LOG.error("3DS must be disabled");
        }

        return REDIRECT_PREFIX + URL_PAYMENT_FAILED;
    }

    @ResponseBody
    @PostMapping(path = "/attemptPaymentWithoutValidation", produces = MediaType.APPLICATION_JSON_VALUE)
    public AjaxResponse payWithoutValidation(
            @RequestParam final String referenceId,
            @RequestParam final String transientToken
    )
    {
        checkArgument(StringUtils.isNotBlank(referenceId), "referenceId is missing");
        checkArgument(StringUtils.isNotBlank(transientToken), "transientToken is missing");

        try
        {
            final IsvPaymentTransactionEntryModel enrollmentTransaction = creditCardPaymentFacade
                    .enrollCreditCard(referenceId, transientToken);
            final Map<String, String> properties = enrollmentTransaction.getProperties();

            final String responseCode = properties.get("payerAuthEnrollReplyReasonCode");
            if (NOT_ENROLLED_CODE.equals(responseCode))
            {
                final String redirectUrl = payAndPlaceOrder(transientToken, null, enrollmentTransaction);

                return AjaxResponse.success()
                        .put("responseCode", NOT_ENROLLED_CODE)
                        .put("redirectUrl", redirectUrl);
            }
            else if (ENROLLED_CODE.equals(responseCode))
            {
                return AjaxResponse.success()
                        .put("responseCode", ENROLLED_CODE)
                        .put("acsUrl", properties.get("payerAuthEnrollReplyAcsURL"))
                        .put("payload", properties.get("payerAuthEnrollReplyPaReq"))
                        .put("transactionId", properties.get("payerAuthEnrollReplyAuthenticationTransactionID"));
            }
            else
            {
                LOG.warn("Cart [{}]: Received invalid enrollment code [{}]. Payment should not proceed",
                        cartService.getSessionCart().getCode(), responseCode);
            }
        }
        catch (final Exception ex)
        {
            LOG.error("Cart [{}]: Exception when trying to enroll/authorize credit card",
                    cartService.getSessionCart().getCode(), ex);
        }

        return AjaxResponse.fail()
                .put("redirectUrl", URL_PAYMENT_FAILED);
    }

    @ResponseBody
    @PostMapping(path = "/payWithValidation", produces = MediaType.APPLICATION_JSON_VALUE)
    public AjaxResponse pay(@RequestParam(name = "transientToken") final String transientToken,
            @RequestParam(name = "authJwt") final String authJwt)
    {
        checkArgument(StringUtils.isNotBlank(transientToken), "transientToken is missing");
        checkArgument(StringUtils.isNotBlank(authJwt), "authJwt is missing");

        try
        {
            final String redirectUrl = payAndPlaceOrder(transientToken, authJwt, null);

            return AjaxResponse.success().put("redirectUrl", redirectUrl);
        }
        catch (final Exception ex)
        {
            LOG.error("Cart [{}]: Exception when trying to validate/authorize", cartService.getSessionCart().getCode(),
                    ex);
            return AjaxResponse.fail().put("redirectUrl", URL_PAYMENT_FAILED);
        }
    }

    private String payAndPlaceOrder(final String transientToken, final String authJwt,
            final IsvPaymentTransactionEntryModel enrollmentTransaction)
    {
        final CartModel cart = cartService.getSessionCart();

        boolean authorizationSucceeded;
        if (authJwt != null)
        {
            authorizationSucceeded = creditCardPaymentFacade
                    .authorizeFlexCreditCardPayment(cart, transientToken, authJwt);
        }
        else if (enrollmentTransaction != null)
        {
            authorizationSucceeded = creditCardPaymentFacade
                    .authorizeFlexCreditCardPayment(cart, transientToken, enrollmentTransaction);
        }
        else
        {
            authorizationSucceeded = creditCardPaymentFacade.authorizeFlexCreditCardPayment(cart, transientToken);
        }

        if (authorizationSucceeded)
        {
            try
            {
                final AbstractOrderData orderData = paymentCheckoutFacade.performPlaceOrder(cart);
                return URL_ORDER_CONFIRMATION + getOrderId(orderData);
            }
            catch (InvalidCartException e)
            {
                LOG.error("Cart [{}]: Place order failed", cart.getCode(), e);
            }
        }

        return URL_PAYMENT_FAILED;
    }

    private String getOrderId(final AbstractOrderData orderData)
    {
        return getCheckoutCustomerStrategy().isAnonymousCheckout() ? orderData.getGuid() : orderData.getCode();
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(final Exception exception)
    {
        LOG.error(exception.getMessage(), exception);

        return REDIRECT_PREFIX + URL_PAYMENT_FAILED;
    }

    public void setCartService(final CartService cartService)
    {
        this.cartService = cartService;
    }

    public void setCreditCardPaymentFacade(final CreditCardPaymentFacade creditCardPaymentFacade)
    {
        this.creditCardPaymentFacade = creditCardPaymentFacade;
    }

    public void setFlexService(final FlexService flexService)
    {
        this.flexService = flexService;
    }

    public void setPaymentCheckoutFacade(final PaymentCheckoutFacade paymentCheckoutFacade)
    {
        this.paymentCheckoutFacade = paymentCheckoutFacade;
    }
}
