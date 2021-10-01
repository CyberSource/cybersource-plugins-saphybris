package isv.sap.payment.addon.b2b.controllers.pages.checkout.steps;

import javax.annotation.Resource;

import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import isv.sap.payment.addon.b2b.ReplenishmentInfoData;
import isv.sap.payment.addon.b2b.facade.ReplenishmentCheckoutFacade;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping(value = "/checkout/replenishment")
public class ReplenishmentCheckoutController
{
    @Resource
    private ReplenishmentCheckoutFacade replenishmentCheckoutFacade;

    @RequireHardLogIn
    @RequestMapping(method = POST, headers = {"content-type=application/json"})
    public ResponseEntity addReplenishment(@RequestBody final ReplenishmentInfoData replenishment)
    {
        replenishmentCheckoutFacade.add(replenishment);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequireHardLogIn
    @RequestMapping(method = DELETE)
    public ResponseEntity removeReplenishment()
    {
        replenishmentCheckoutFacade.removeReplenishment();
        return new ResponseEntity(HttpStatus.OK);
    }
}
