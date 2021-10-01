package isv.sap.payment.addon.b2b.provider

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.b2bacceleratorfacades.order.data.ScheduledCartData
import org.junit.Test
import spock.lang.Specification

@UnitTest
class B2bOrderConfirmationPageProviderSpec extends Specification
{
    @Test
    def 'should provide scheduled order confirmation page'()
    {
        given:
        def scheduledCartData = new ScheduledCartData(jobCode: 'jobCode')

        when:
        def confirmationPage = new B2bOrderConfirmationPageProvider().getOrderConfirmationPage(scheduledCartData)

        then:
        confirmationPage == "/checkout/replenishment/confirmation/${scheduledCartData.jobCode}"
    }
}
