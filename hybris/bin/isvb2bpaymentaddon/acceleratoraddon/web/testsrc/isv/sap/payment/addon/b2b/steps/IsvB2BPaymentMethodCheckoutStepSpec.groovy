package isv.sap.payment.addon.b2b.steps

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BPaymentTypeData
import de.hybris.platform.commercefacades.order.CheckoutFacade
import de.hybris.platform.commercefacades.order.data.CartData
import org.junit.Test
import spock.lang.Specification

@UnitTest
class IsvB2BPaymentMethodCheckoutStepSpec extends Specification
{
    def checkoutFacade = Mock([useObjenesis: false], CheckoutFacade)

    def cartData = Mock([useObjenesis: false], CartData)

    def paymentTypeData = Mock([useObjenesis: false], B2BPaymentTypeData)

    def b2BPaymentMethodCheckoutStep = new IsvB2BPaymentMethodCheckoutStep()

    void setup()
    {
        b2BPaymentMethodCheckoutStep.checkoutFacade = checkoutFacade
    }

    @Test
    def 'verify if checkout is enabled by payment type'()
    {
        given:
        checkoutFacade.checkoutCart >> cartData
        cartData.paymentType >> paymentTypeData
        paymentTypeData.code >> code

        when:
        def result = b2BPaymentMethodCheckoutStep.isEnabled()
        then:
        result == enabled

        where:
        code      | enabled
        'CARD'    | true
        'ACCOUNT' | false
    }

    @Test
    def 'is not enabled due to missing cart'()
    {
        when:
        def result = b2BPaymentMethodCheckoutStep.isEnabled()
        then:
        result == false
    }

    @Test
    def 'is not enabled due to missing payment type on cart'()
    {
        given:
        checkoutFacade.checkoutCart >> cartData

        when:
        def result = b2BPaymentMethodCheckoutStep.isEnabled()

        then:
        result == false
    }
}
