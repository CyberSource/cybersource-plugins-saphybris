package isv.sap.payment.addon.b2b.provider

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.b2bacceleratorfacades.order.data.ScheduledCartData
import de.hybris.platform.commercefacades.order.data.OrderData
import de.hybris.platform.commerceservices.strategies.CheckoutCustomerStrategy
import org.junit.Test
import spock.lang.Specification

@UnitTest
class B2bOrderConfirmationPageProviderSpec extends Specification
{
    def checkoutCustomerStrategy = Mock(CheckoutCustomerStrategy)

    def confirmationPageProvider = new B2bOrderConfirmationPageProvider(
            checkoutCustomerStrategy: checkoutCustomerStrategy
    )

    @Test
    def 'should provide scheduled order confirmation page'()
    {
        given:
        def scheduledCartData = new ScheduledCartData(jobCode: 'jobCode')

        when:
        def confirmationPage = confirmationPageProvider.getOrderConfirmationPage(scheduledCartData)

        then:
        confirmationPage == "/checkout/replenishment/confirmation/${scheduledCartData.jobCode}"
    }

    @Test
    def 'should provide order confirmation page'()
    {
        given:
        def orderData = new OrderData(code: 'order12345')
        checkoutCustomerStrategy.isAnonymousCheckout() >> false

        when:
        def confirmationPage = confirmationPageProvider.getOrderConfirmationPage(orderData)

        then:
        confirmationPage == "/checkout/orderConfirmation/${orderData.code}"
    }
}
