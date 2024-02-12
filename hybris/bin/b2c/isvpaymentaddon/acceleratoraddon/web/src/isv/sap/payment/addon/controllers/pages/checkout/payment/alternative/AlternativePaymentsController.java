package isv.sap.payment.addon.controllers.pages.checkout.payment.alternative;

import java.util.Map;
import java.util.Optional;
import javax.annotation.Resource;

import com.google.common.collect.Maps;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractCheckoutController;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import isv.sap.payment.addon.constants.IsvPaymentAddonConstants;
import isv.sap.payment.addon.enums.CheckStatusResponse;
import isv.sap.payment.addon.facade.AlternativePaymentFacade;
import isv.sap.payment.addon.facade.AlternativePaymentStatusFacade;
import isv.sap.payment.commercefacades.order.PaymentCheckoutFacade;

import static de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages.addErrorMessage;
import static java.util.Optional.empty;
import static org.apache.commons.lang.StringUtils.containsIgnoreCase;
import static org.springframework.http.ResponseEntity.ok;

@Controller
@RequestMapping(path = "/checkout/payment/ap")
public class AlternativePaymentsController extends AbstractCheckoutController
{
    public static final String PAYMENT_FAILED = "/checkout/multi/summary/view/payment/error";

    protected static final String PLACE_ORDER_PAYMENT_ERROR = "checkout.place.order.payment.error";

    private static final String ALIPAY_MERCHANT_URL_HOST = "isv.payment.alternativepayment.alipay.merchanturl.host";

    private static final Logger LOG = LoggerFactory.getLogger(AlternativePaymentsController.class);

    @Resource
    private CartService cartService;

    @Resource(name = "isv.sap.payment.paymentCheckoutFacade")
    private PaymentCheckoutFacade paymentCheckoutFacade;

    @Resource
    private AlternativePaymentFacade alternativePaymentFacade;

    @Resource
    private ConfigurationService configurationService;

    @Resource
    private AlternativePaymentStatusFacade alternativePaymentStatusFacade;

    @RequestMapping(value = "/pay", method = RequestMethod.POST)
    public String pay(@RequestParam(name = "paymentModeCode") final String paymentModeCode,
            @RequestParam(name = "paymentOptionId", required = false) final String optionId,
            @RequestParam(name = "klarnaAuthToken", required = false) final String klarnaAuthToken,
            final Model model)
    {
        return REDIRECT_PREFIX + resolveRedirectUrl(paymentModeCode, optionId, klarnaAuthToken)
                .map(this::resolveMerchantUrl)
                .orElseGet(() -> {
                    addErrorMessage(model, PLACE_ORDER_PAYMENT_ERROR);
                    return PAYMENT_FAILED;
                });
    }

    @RequestMapping(value = "/payNoRedirect", method = RequestMethod.POST)
    public ResponseEntity<String> payNoRedirect(@RequestParam(name = "paymentModeCode") final String paymentModeCode,
            @RequestParam(name = "paymentOptionId", required = false) final String optionId,
            @RequestParam(name = "klarnaAuthToken", required = false) final String klarnaAuthToken,
            final Model model)
    {
        return resolveRedirectUrl(paymentModeCode, optionId, klarnaAuthToken)
                .map(this::resolveMerchantUrl)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    addErrorMessage(model, PLACE_ORDER_PAYMENT_ERROR);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(PAYMENT_FAILED);
                });
    }

    @RequestMapping(value = "/checkstatus", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<CheckStatusResponse> isOrderPlaced()
    {
        final CartModel cartModel = cartService.getSessionCart();

        return ok(alternativePaymentStatusFacade.resolveCheckStatusResponse(cartModel));
    }

    @RequestMapping("/return")
    public String handleReturn(@RequestParam(name = "type") final String paymentType)
    {
        final CartModel cart = cartService.getSessionCart();

        return placeOrder(cart, paymentType)
                .map(orderData -> {
                    final StringBuilder redirectToConfirmationPage = new StringBuilder(redirectToOrderConfirmation(orderData));
                    // mark it as being used for alternative payments

                    redirectToConfirmationPage.append("?ap=")
                        .append(paymentType);
                    return redirectToConfirmationPage.toString();
                })
                .orElse(REDIRECT_PREFIX + PAYMENT_FAILED);
    }


    private Optional<String> resolveRedirectUrl(final String paymentModeCode, final String optionId, final String klarnaAuthToken)
    {
        try
        {
            final Map<String, Object> optionalParameters = Maps.newHashMap();
            optionalParameters.put(IsvPaymentAddonConstants.AlternativePayments.PAYMENT_OPTION_ID, optionId);
            optionalParameters.put(IsvPaymentAddonConstants.AlternativePayments.KLARNA_AUTH_TOKEN, klarnaAuthToken);

            return alternativePaymentFacade.makeSaleRequestForAlternativePayment(
                    cartService.getSessionCart(),
                    paymentModeCode, optionalParameters
            );
        }
        catch (final Exception ex)
        {
            LOG.error("Exception during alternative payment pay", ex);
            return empty();
        }
    }

    private Optional<AbstractOrderData> placeOrder(final CartModel cart, final String paymentType)
    {
        try
        {
            if (alternativePaymentFacade.validateAlternativePaymentResponse(cart, paymentType))
            {
                return Optional.of(paymentCheckoutFacade.performPlaceOrder(cart));
            }
        }
        catch (final Exception ex)
        {
            LOG.error("Exception during processing return for alternative payment", ex);
            return empty();
        }

        return empty();
    }

    protected String redirectToOrderConfirmation(final AbstractOrderData orderData)
    {

        return REDIRECT_URL_ORDER_CONFIRMATION
                + (getCheckoutCustomerStrategy().isAnonymousCheckout() ? orderData.getGuid() : orderData.getCode());
    }

    private String resolveMerchantUrl(final String url)
    {
        if (containsIgnoreCase(url, "alipay"))
        {
            final String paramsUrl = url.substring(url.indexOf('?'));
            final String merchantHost = configurationService.getConfiguration().getString(ALIPAY_MERCHANT_URL_HOST);
            return merchantHost + paramsUrl;
        }
        return url;
    }

    public void setCartService(final CartService cartService)
    {
        this.cartService = cartService;
    }

    public void setPaymentCheckoutFacade(final PaymentCheckoutFacade paymentCheckoutFacade)
    {
        this.paymentCheckoutFacade = paymentCheckoutFacade;
    }

    public void setAlternativePaymentFacade(final AlternativePaymentFacade alternativePaymentFacade)
    {
        this.alternativePaymentFacade = alternativePaymentFacade;
    }

    public void setAlternativePaymentStatusFacade(final AlternativePaymentStatusFacade alternativePaymentStatusFacade)
    {
        this.alternativePaymentStatusFacade = alternativePaymentStatusFacade;
    }
}
