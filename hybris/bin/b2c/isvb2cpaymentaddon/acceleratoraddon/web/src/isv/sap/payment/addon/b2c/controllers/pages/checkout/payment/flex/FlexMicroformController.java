package isv.sap.payment.addon.b2c.controllers.pages.checkout.payment.flex;

import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
// import org.apache.commons.lang3.ObjectUtils;
 
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractCheckoutController;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.servicelayer.model.ModelService;
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
import static isv.sap.payment.constants.IsvPaymentConstants.ReasonCode.REVIEW_CODE;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import org.springframework.ui.Model;
import org.apache.commons.text.StringEscapeUtils;
import isv.sap.payment.addon.utils.AjaxResponse;
import isv.sap.payment.commerceservices.order.PaymentCartService;

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
    public AjaxResponse newJwk(final HttpSession session, final UriComponentsBuilder uriComponentsBuilder)
    {
        final String targetOrigin = uriComponentsBuilder
                .replacePath(null).replaceQuery(null).userInfo(null).fragment(null)
                .build()
                .toUriString();
        final Map<String, String> captureContext = flexService.createKey(targetOrigin, creditCardPaymentFacade.getMerchantIdForCaptureContext());

        session.setAttribute(FLEX_CAPTURE_CONTEXT_ATTRIBUTE, captureContext.get("captureContext"));
        
        return AjaxResponse.success()
                .put("captureContext", StringEscapeUtils.escapeHtml4(captureContext.get("captureContext")))
                .put("clientLibrary", StringEscapeUtils.escapeHtml4(captureContext.get("clientLibrary")))
                .put("clientLibraryIntegrity", StringEscapeUtils.escapeHtml4(captureContext.get("clientLibraryIntegrity")));

    }

    @PostMapping(value = "/verifyToken", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity verifyToken(@RequestBody final String flexToken, final HttpSession session)
    {
        final String captureContext = (String) session.getAttribute(FLEX_CAPTURE_CONTEXT_ATTRIBUTE);
        //OLH: For Reflected XSS fix. I don't think this method is vulnerable to XSS since the flexToken its not a user's input to the response but part of the response from Cybersource.
        // To be extra safe, we can sanitize it before passing to the flexService.verifyAndGet() method below. The method does not seem to perform any validation or sanitization.
        String sanitizedFlexToken = StringEscapeUtils.escapeHtml4(flexToken);

        checkNotNull(captureContext);

        
        return flexService.verifyAndGet(sanitizedFlexToken)//OLH: Use sanitize value
                .map(transientToken -> ResponseEntity.ok(transientToken))
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
    @PostMapping(path = "/attemptPaymentSetUp", produces = MediaType.APPLICATION_JSON_VALUE)
    public AjaxResponse setUp(
            @RequestParam final String transientToken
    )
    {
        checkArgument(StringUtils.isNotBlank(transientToken), "transientToken is missing");
        String sanitizedTransientToken = StringEscapeUtils.escapeHtml4(transientToken);

        try
        {
            final IsvPaymentTransactionEntryModel setUpTransaction = creditCardPaymentFacade
                    .setUpCreditCard(sanitizedTransientToken);
            final Map<String, String> properties = setUpTransaction.getProperties();
            if("ACCEPT".equals(properties.get("decision")))
            {
                 return AjaxResponse.success()
                    .put("decision",properties.get("decision") )
                    .put("requestID", properties.get("requestID"))
                    .put("deviceDataCollectionURL",properties.get("deviceDataCollectionURL"))
                    .put("accessToken",properties.get("accessToken"))
                    .put("referenceID",properties.get("referenceID"));
            }
            else
            {
                LOG.warn("Cart [{}]: Received invalid setup code [{}]. Payment should not proceed",
                        cartService.getSessionCart().getCode(), properties.get("decision"));
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
    @PostMapping(path = "/attemptPaymentWithoutValidation", produces = MediaType.APPLICATION_JSON_VALUE)
    public AjaxResponse payWithoutValidation(
            @RequestParam final String referenceId,
            @RequestParam final String transientToken,
            @RequestParam final String browserCookieAccepted,
            @RequestParam final String browserScreenHeight,
            @RequestParam final String browserScreenWidth,
            @RequestParam final String serviceReturnUrl,
            final UriComponentsBuilder uriComponentsBuilder)
    {
        final String targetOrigin = uriComponentsBuilder
                .replacePath(null).replaceQuery(null).userInfo(null).fragment(null)
                .build()
                .toUriString();
        checkArgument(StringUtils.isNotBlank(referenceId), "referenceId is missing");
        checkArgument(StringUtils.isNotBlank(transientToken), "transientToken is missing");
        checkArgument(StringUtils.isNotBlank(browserCookieAccepted), "User browser cookie accepted is missing");
        checkArgument(StringUtils.isNotBlank(browserScreenHeight), "User browser screen height is missing");
        checkArgument(StringUtils.isNotBlank(browserScreenWidth), "User browser screen width is missing");
        String sanitizedTransientToken = StringEscapeUtils.escapeHtml4(transientToken);
        String sanitizedReferenceId = StringEscapeUtils.escapeHtml4(referenceId);
        String sanitizedBrowserCookieAccepted = StringEscapeUtils.escapeHtml4(browserCookieAccepted);
        String sanitizedBrowserScreenHeight = StringEscapeUtils.escapeHtml4(browserScreenHeight);
        String sanitizedBrowserScreenWidth = StringEscapeUtils.escapeHtml4(browserScreenWidth);
        String sanitizedServiceReturnUrl = StringEscapeUtils.escapeHtml4(serviceReturnUrl);
        try
        {
            final IsvPaymentTransactionEntryModel enrollmentTransaction = creditCardPaymentFacade
                    .enrollCreditCard(sanitizedReferenceId, sanitizedTransientToken, sanitizedBrowserCookieAccepted,sanitizedBrowserScreenHeight,sanitizedBrowserScreenWidth,targetOrigin+sanitizedServiceReturnUrl+"/checkout/payment/flex/payerAuthHelper");
            final Map<String, String> properties = enrollmentTransaction.getProperties();

            final String responseCode = properties.get("reasonCode");
            if (NOT_ENROLLED_CODE.equals(responseCode)|| REVIEW_CODE.equals(responseCode))
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
                        .put("stepUpUrl", properties.get("payerAuthEnrollReplyStepUpUrl"))
                        .put("accessToken", properties.get("payerAuthEnrollReplyAccessToken"))
                        .put("paReq", properties.get("payerAuthEnrollReplyPaReq"));
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

    @PostMapping(path = "/payerAuthHelper")
    public String payerAuthHelper(@RequestParam(name = "TransactionId") final String transactionId, final Model model)
    {
        String sanitizedTransactionId = StringEscapeUtils.escapeHtml4(transactionId);
        model.addAttribute("transactionId", sanitizedTransactionId);
        return "addon:/isvpaymentaddon/pages/checkout/multi/payment/payerAuthHelper";    
    }

    @ResponseBody
    @PostMapping(path = "/payWithValidation", produces = MediaType.APPLICATION_JSON_VALUE)
    public AjaxResponse pay(@RequestParam(name = "transientToken") final String transientToken,
            @RequestParam(name = "transactionId") final String transactionId)
    {
        checkArgument(StringUtils.isNotBlank(transientToken), "transientToken is missing");
        checkArgument(StringUtils.isNotBlank(transactionId), "transactionId is missing");
        String sanitizedTransientToken = StringEscapeUtils.escapeHtml4(transientToken);
        String sanitizedTransactionId = StringEscapeUtils.escapeHtml4(transactionId);
        try
        {
            final String redirectUrl = payAndPlaceOrder(sanitizedTransientToken, sanitizedTransactionId, null);

            return AjaxResponse.success().put("redirectUrl", redirectUrl);
        }
        catch (final Exception ex)
        {
            LOG.error("Cart [{}]: Exception when trying to validate/authorize", cartService.getSessionCart().getCode(),
                    ex);
            return AjaxResponse.fail().put("redirectUrl", URL_PAYMENT_FAILED);
        }
    }
 
    @Resource(name = "isv.sap.payment.paymentCartService")
    private PaymentCartService paymentCartService;

    @Resource
    private ModelService modelService;

    private String payAndPlaceOrder(final String transientToken, final String transactionId,
            final IsvPaymentTransactionEntryModel enrollmentTransaction)
    {
        final CartModel cart = cartService.getSessionCart();
        final String[] response = {URL_PAYMENT_FAILED};

        // Capture cart state before authorization
        final Double authorizedTotal = cart.getTotalPrice();
        final int authorizedItemCount = cart.getEntries().size();

        boolean authorizationSucceeded;
        if (transactionId != null)
        {
            authorizationSucceeded = creditCardPaymentFacade
                    .authorizeFlexCreditCardPayment(cart, transientToken, transactionId);
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
                paymentCartService.executeWithCartLock(cart, () -> {
                    try
                    {
                        // Refresh cart to get current database state after lock acquisition
                        modelService.refresh(cart);

                        // Re-validate cart state against authorized amounts inside critical section
                        final Double currentTotal = cart.getTotalPrice();
                        final int currentItemCount = cart.getEntries().size();

                        if (!authorizedTotal.equals(currentTotal) || authorizedItemCount != currentItemCount)
                        {
                            LOG.error("Cart modification detected inside lock. " +
                                    "Authorized total: {}, Current total: {}. " +
                                    "Authorized items: {}, Current items: {}. " +
                                    "Rejecting order to prevent race condition exploit.",
                                    authorizedTotal, currentTotal, authorizedItemCount, currentItemCount);
                            return;
                        }

                        final AbstractOrderData orderData = paymentCheckoutFacade.performPlaceOrder(cart);
                        if(orderData != null)
                        {
                            response[0] = URL_ORDER_CONFIRMATION + getOrderId(orderData);
                        }
                    }
                    catch (final Exception e)
                    {
                        LOG.error("Error while placing order with Card payment", e);
                    }
                });
            }
            catch (final Exception e)
            {
                LOG.error("Cart [{}]: Place order failed", cart.getCode(), e);
            }
        }

        return response[0];
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
