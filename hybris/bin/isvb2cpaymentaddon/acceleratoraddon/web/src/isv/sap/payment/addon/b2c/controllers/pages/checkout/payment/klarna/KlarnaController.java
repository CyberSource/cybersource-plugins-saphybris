package isv.sap.payment.addon.b2c.controllers.pages.checkout.payment.klarna;

import javax.annotation.Resource;

import de.hybris.platform.order.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import isv.sap.payment.addon.constants.IsvPaymentAddonConstants;
import isv.sap.payment.addon.facade.KlarnaPaymentFacade;
import isv.sap.payment.addon.utils.AjaxResponse;

/**
 * Concrete controller for Klarna payment service.
 */
@Controller
@RequestMapping(path = "/checkout/payment/klarna")
public class KlarnaController
{
    private static final Logger LOG = LoggerFactory.getLogger(KlarnaController.class);

    @Resource
    private KlarnaPaymentFacade klarnaPaymentFacade;

    @Resource
    private CartService cartService;

    @ResponseBody
    @RequestMapping(path = "/createSession", method = RequestMethod.POST)
    public AjaxResponse createSession()
    {
        try
        {
            final String sessionId = klarnaPaymentFacade.createKlarnaSession(cartService.getSessionCart());

            return AjaxResponse.success()
                    .put(IsvPaymentAddonConstants.AlternativePayments.KLARNA_SESSION_ID, sessionId);
        }
        catch (final Exception ex)
        {
            LOG.error("Exception when trying to create Klarna session", ex);
            return AjaxResponse.fail();
        }
    }

    @ResponseBody
    @RequestMapping(path = "/updateSession", method = RequestMethod.PUT)
    public AjaxResponse updateSession()
    {
        try
        {
            final String sessionId = klarnaPaymentFacade.updateKlarnaSession(cartService.getSessionCart());

            return AjaxResponse.success()
                    .put(IsvPaymentAddonConstants.AlternativePayments.KLARNA_SESSION_ID, sessionId);
        }
        catch (final Exception ex)
        {
            LOG.error("Exception when trying to create Klarna session", ex);
            return AjaxResponse.fail();
        }
    }

    public void setKlarnaPaymentFacade(final KlarnaPaymentFacade klarnaPaymentFacade)
    {
        this.klarnaPaymentFacade = klarnaPaymentFacade;
    }

    public void setCartService(final CartService cartService)
    {
        this.cartService = cartService;
    }
}
