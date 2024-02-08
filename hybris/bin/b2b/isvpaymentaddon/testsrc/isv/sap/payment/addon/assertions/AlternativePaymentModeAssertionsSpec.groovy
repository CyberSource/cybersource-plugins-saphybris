package isv.sap.payment.addon.assertions

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.payment.PaymentModeModel
import org.junit.Test
import spock.lang.Specification

import isv.sap.payment.enums.AlternativePaymentMethod
import isv.sap.payment.enums.PaymentType
import isv.sap.payment.model.IsvPaymentModeModel

@UnitTest
class AlternativePaymentModeAssertionsSpec extends Specification
{
    @Test
    def 'Should assert that payment type is valid'()
    {
        given:
        def paymentMode = Mock(IsvPaymentModeModel)
        paymentMode.paymentType >> PaymentType.ALTERNATIVE_PAYMENT

        when:
        AlternativePaymentModeAssertions.assertValidPaymentType(paymentMode)

        then:
        noExceptionThrown()
    }

    @Test
    def 'Should throw exception when payment type is not valid'()
    {
        given:
        def paymentMode = Mock(IsvPaymentModeModel)
        paymentMode.paymentType >> PaymentType.CREDIT_CARD

        when:
        AlternativePaymentModeAssertions.assertValidPaymentType(paymentMode)

        then:
        thrown(IllegalStateException)
    }

    @Test
    def 'Should assert that payment mode class type is valid'()
    {
        given:
        def paymentMode = Mock(IsvPaymentModeModel)

        when:
        AlternativePaymentModeAssertions.assertValidPaymentModeClazz(paymentMode)

        then:
        noExceptionThrown()
    }

    @Test
    def 'Should throw exception when payment mode class type is not valid'()
    {
        given:
        def paymentMode = Mock(PaymentModeModel)

        when:
        AlternativePaymentModeAssertions.assertValidPaymentModeClazz(paymentMode)

        then:
        thrown(IllegalStateException)
    }

    @Test
    def 'Should assert that alternative payment type is not null'()
    {
        when:
        AlternativePaymentModeAssertions.assertAlternativePaymentSubTypeIsSet(AlternativePaymentMethod.PPL)

        then:
        noExceptionThrown()
    }

    @Test
    def 'Should throw exception when alternative payment type is null'()
    {
        when:
        AlternativePaymentModeAssertions.assertAlternativePaymentSubTypeIsSet(null)

        then:
        thrown(IllegalArgumentException)
    }
}
