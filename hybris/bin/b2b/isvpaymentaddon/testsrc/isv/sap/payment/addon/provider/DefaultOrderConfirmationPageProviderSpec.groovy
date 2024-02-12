package isv.sap.payment.addon.provider

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.commercefacades.order.data.AbstractOrderData
import de.hybris.platform.commerceservices.strategies.CheckoutCustomerStrategy
import org.junit.Test
import spock.lang.Specification

@UnitTest
class DefaultOrderConfirmationPageProviderSpec extends Specification
{
    def checkoutCustomerStrategy = Mock([useObjenesis: false], CheckoutCustomerStrategy)

    def provider = new DefaultOrderConfirmationPageProvider(checkoutCustomerStrategy: checkoutCustomerStrategy)

    def orderData = new AbstractOrderData(guid: 'guid', code: 'code')

    @Test
    def 'should return confirmation uri for anonymous user'()
    {
        given:
        checkoutCustomerStrategy.isAnonymousCheckout() >> true

        when:
        def confirmationPage = provider.getOrderConfirmationPage(orderData)

        then:
        confirmationPage == "/checkout/orderConfirmation/${orderData.guid}"
    }

    @Test
    def 'should return confirmation uri for registered user'()
    {
        given:
        checkoutCustomerStrategy.isAnonymousCheckout() >> false

        when:
        def confirmationPage = provider.getOrderConfirmationPage(orderData)

        then:
        confirmationPage == "/checkout/orderConfirmation/${orderData.code}"
    }
}
