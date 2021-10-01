package isv.sap.payment.addon.controllers.pages.checkout.payment.alternative;

import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractPageController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping(path = "/checkout/payment/ap")
public class AlipayStubController extends AbstractPageController
{
    @RequestMapping(value = "/alipay", method = GET)
    public String simulateAlipayService()
    {
        return "addon:/isvpaymentaddon/pages/checkout/alipay/alipay_simulator";
    }
}
