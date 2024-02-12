package isv.sap.payment.addon.controllers.pages;

import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractCheckoutController;
import de.hybris.platform.commercefacades.order.OrderFacade;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import isv.sap.payment.addon.handler.ResponseHandler;
import isv.sap.payment.addon.provider.OrderConfirmationPageProvider;
import isv.sap.payment.commercefacades.order.PaymentCheckoutFacade;
import isv.sap.payment.commerceservices.order.PaymentCartService;
import isv.sap.payment.constants.IsvPaymentConstants;
import isv.sap.payment.utils.LogUtils;

@Controller
@RequestMapping("/checkout/payment/sa")
public class PaymentController extends AbstractCheckoutController
{
    private static final Logger LOG = LoggerFactory.getLogger(PaymentController.class);

    private static final String RESPONSE_ACTION = "responseAction";

    @Resource(name = "isv.sap.payment.paymentCartService")
    private PaymentCartService paymentCartService;

    @Resource
    private ResponseHandler isvResponseHandler;

    @Resource(name = "isv.sap.payment.paymentCheckoutFacade")
    private PaymentCheckoutFacade paymentCheckoutFacade;

    @Resource
    private OrderFacade orderFacade;

    @Resource
    private OrderConfirmationPageProvider orderConfirmationPageProvider;

    @RequestMapping(value = "/receipt", method = RequestMethod.POST)
    public String handlerReceiptPost(final HttpServletRequest request, final Model model)
    {
        final Map<String, String> paymentResponse = isvResponseHandler.getValidParameters(request);
        model.addAttribute(RESPONSE_ACTION, "/checkout/multi/summary/view/payment/error");
        final String orderNumber = getReferenceNumber(paymentResponse);
        LOG.info("Creating order [{}] from ISV Receipt POST", LogUtils.encode(orderNumber));

        if (StringUtils.isNotEmpty(orderNumber))
        {
            final CartModel cart = paymentCartService.getCartForGuid(orderNumber);
            if (cart != null)
            {
                paymentCartService.executeWithCartLock(cart, () -> {
                    final AbstractOrderData orderData = doHandlePlaceOrder(paymentResponse, cart);
                    if (orderData != null)
                    {
                        model.addAttribute(RESPONSE_ACTION,
                                orderConfirmationPageProvider.getOrderConfirmationPage(orderData));
                    }
                });
            }
        }

        return "addon:/isvpaymentaddon/pages/checkout/payment/sa/response";
    }

    @RequestMapping(value = "/merchantpost", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void handlerMerchantPost(final HttpServletRequest request)
    {
        final Map<String, String> paymentResponse = isvResponseHandler.getValidParameters(request);
        final String orderNumber = getReferenceNumber(paymentResponse);
        LOG.info("Creating order [{}] from ISV Merchant POST", LogUtils.encode(orderNumber));

        if (StringUtils.isEmpty(orderNumber))
        {
            return;
        }

        final CartModel cart = paymentCartService.getCartForGuid(orderNumber);
        if (cart != null)
        {
            paymentCartService.executeWithCartLock(cart, () -> doHandlePlaceOrder(paymentResponse, cart));
        }
    }

    @RequestMapping(value = "/isorderplaced", method = RequestMethod.GET)
    @ResponseBody
    public String isOrderPlaced(@RequestParam final String cartGuid)
    {
        try
        {
            final OrderData orderData = orderFacade.getOrderDetailsForGUID(cartGuid);
            return getCheckoutCustomerStrategy().isAnonymousCheckout() ? orderData.getGuid() : orderData.getCode();
        }
        catch (final UnknownIdentifierException e)
        {
            return StringUtils.EMPTY;
        }
    }

    private String getReferenceNumber(final Map<String, String> paymentResponse)
    {
        final String referenceNumber = paymentResponse
                .get(IsvPaymentConstants.SAResponseFields.REFERENCE_NUMBER);
        if (referenceNumber == null)
        {
            LOG.error("Got an empty order reference number on isv payment response");
        }

        return referenceNumber;
    }

    private AbstractOrderData doHandlePlaceOrder(final Map<String, String> paymentResponse, final CartModel cart)
    {
        try
        {
            if (isvResponseHandler.isValidSignature(paymentResponse))
            {
                isvResponseHandler.processResponse(cart, paymentResponse);

                if (paymentCheckoutFacade.validOrder(cart) && isvResponseHandler
                        .isDecisionSuccessful(paymentResponse))
                {
                    return paymentCheckoutFacade.performPlaceOrder(cart);
                }
                else
                {
                    LOG.error("Failed to place Order, please check order data and payment transaction");
                }
            }
        }
        catch (final Exception e)
        {
            LOG.error("Failed to place Order", e);
        }

        return null;
    }
}
