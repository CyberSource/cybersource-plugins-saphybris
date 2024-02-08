package isv.sap.payment.addon.controllers.pages.checkout.payment.alternative

import de.hybris.bootstrap.annotations.UnitTest
import org.junit.Test
import spock.lang.Specification

@UnitTest
class AlipayStubControllerSpec extends Specification
{
    @Test
    def 'should simulate AliPay service'()
    {
        when:
        def actual = new AlipayStubController().simulateAlipayService()

        then:
        actual == 'addon:/isvpaymentaddon/pages/checkout/alipay/alipay_simulator'
    }
}
