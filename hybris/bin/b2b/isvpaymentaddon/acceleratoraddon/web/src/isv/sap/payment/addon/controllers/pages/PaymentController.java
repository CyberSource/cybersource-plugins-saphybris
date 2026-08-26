package isv.sap.payment.addon.controllers.pages;

import java.util.Map;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

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

import isv.sap.payment.model.IsvPaymentTransactionModel;
import isv.sap.payment.addon.handler.ResponseHandler;
import isv.sap.payment.addon.provider.OrderConfirmationPageProvider;
import isv.sap.payment.commercefacades.order.PaymentCheckoutFacade;
import isv.sap.payment.commerceservices.order.PaymentCartService;
import isv.sap.payment.constants.IsvPaymentConstants;
import isv.sap.payment.utils.LogUtils;

import static isv.cjl.payment.enums.PaymentType.CREDIT_CARD;
import isv.cjl.payment.service.MerchantService;

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

    @Resource(name = "isv.sap.payment.hybrisMerchantService")
    private MerchantService merchantService;

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
                if (!validateMerchantId(paymentResponse, cart))
                {
                    LOG.error("Merchant ID validation failed for cart [{}]. Rejecting payment response.",
                            LogUtils.encode(orderNumber));
                    return "addon:/isvpaymentaddon/pages/checkout/payment/sa/response";
                }
                
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
            if (!validateMerchantId(paymentResponse, cart))
            {
                LOG.error("Merchant ID validation failed for cart [{}]. Rejecting payment response.",
                        LogUtils.encode(orderNumber));
                return;
            }
            
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

    /**
     * Validates that the merchant ID in the payment response matches the cart's configuration.
     * This prevents replay attacks where an attacker uses a different merchant's credentials
     * to forge valid signatures.
     *
     * @param paymentResponse the payment response containing merchant ID
     * @param cart the cart being processed
     * @return true if merchant ID is valid, false otherwise
     */
    private boolean validateMerchantId(final Map<String, String> paymentResponse, final CartModel cart)
    {
        final String responseMerchantId = paymentResponse.get(IsvPaymentConstants.SAResponseFields.MERCHANT_ID);
 
        if (StringUtils.isEmpty(responseMerchantId))
        {
            LOG.error("Payment response does not contain merchant ID");
            return false;
        }
 
        final String expectedMerchantId =   merchantService.getCurrentMerchant(CREDIT_CARD).getId();
 
        if (StringUtils.isEmpty(expectedMerchantId))
        {
            LOG.error("No ISV payment transaction with merchant ID found on cart");
            return false;
        }
 
        if (!responseMerchantId.equals(expectedMerchantId))
        {
            LOG.error("Merchant ID mismatch. Expected: {}, Received: {}. Rejecting to prevent merchant ID substitution attack.",
                    LogUtils.encode(expectedMerchantId), LogUtils.encode(responseMerchantId));
            return false;
        }
 
        return true;
    }

    private AbstractOrderData doHandlePlaceOrder(final Map<String, String> paymentResponse, final CartModel cart)
    {
        try
        {
            try
            {
                orderFacade.getOrderDetailsForGUID(cart.getGuid());
                LOG.error("Order already exists for cart GUID [{}]. Rejecting duplicate order placement attempt.",
                        LogUtils.encode(cart.getGuid()));
            }
            catch (final UnknownIdentifierException e)
            {
                LOG.error("Failed to place Order", e);
            }
            
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
