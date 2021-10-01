package isv.sap.payment.fulfilmentprocess.strategy.context

import de.hybris.bootstrap.annotations.UnitTest
import org.junit.Test
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import isv.cjl.payment.enums.AlternativePaymentMethod
import isv.sap.payment.fulfilmentprocess.strategy.impl.takepayment.ApplePayTakePaymentStrategy
import isv.sap.payment.fulfilmentprocess.strategy.impl.takepayment.CreditCardTakePaymentStrategy
import isv.sap.payment.fulfilmentprocess.strategy.impl.takepayment.GooglePayTakePaymentStrategy
import isv.sap.payment.fulfilmentprocess.strategy.impl.takepayment.KlarnaTakePaymentStrategy
import isv.sap.payment.fulfilmentprocess.strategy.impl.takepayment.PayPalTakePaymentStrategy
import isv.sap.payment.fulfilmentprocess.strategy.impl.takepayment.VisaCheckoutTakePaymentStrategy
import isv.sap.payment.model.IsvPaymentTransactionModel

import static isv.sap.payment.enums.AlternativePaymentMethod.KLI
import static isv.sap.payment.enums.AlternativePaymentMethod.PPL
import static isv.sap.payment.enums.PaymentType.ALTERNATIVE_PAYMENT
import static isv.sap.payment.enums.PaymentType.APPLE_PAY
import static isv.sap.payment.enums.PaymentType.CREDIT_CARD
import static isv.sap.payment.enums.PaymentType.GOOGLE_PAY
import static isv.sap.payment.enums.PaymentType.PAY_PAL
import static isv.sap.payment.enums.PaymentType.TAX_CALCULATION
import static isv.sap.payment.enums.PaymentType.VISA_CHECKOUT

@UnitTest
class PaymentOperationContextSpec extends Specification
{
    def context = new PaymentOperationContext()

    def transaction = Mock([useObjenesis: false], IsvPaymentTransactionModel)

    @Shared
    def klarnaStrategy = new KlarnaTakePaymentStrategy()

    @Shared
    def payPalStrategy = new PayPalTakePaymentStrategy()

    @Shared
    def vcoStrategy = new VisaCheckoutTakePaymentStrategy()

    @Shared
    def creditCardStrategy = new CreditCardTakePaymentStrategy()

    @Shared
    def applePayStrategy = new ApplePayTakePaymentStrategy()

    @Shared
    def googlePayStrategy = new GooglePayTakePaymentStrategy()

    def 'setup'()
    {
        context.strategies = [klarnaStrategy, payPalStrategy, vcoStrategy, creditCardStrategy, applePayStrategy, googlePayStrategy]
    }

    @Test
    def 'should return suitable take payment strategy'()
    {
        given:
        transaction.paymentProvider >> provider.toString()
        transaction.alternativePaymentMethod >> method

        when:
        def strategy = context.strategy(transaction)

        then:
        strategy == selected

        where:
        provider            | method | selected
        CREDIT_CARD         | null   | creditCardStrategy
        ALTERNATIVE_PAYMENT | KLI    | klarnaStrategy
        PAY_PAL             | PPL    | payPalStrategy
        VISA_CHECKOUT       | null   | vcoStrategy
        APPLE_PAY           | null   | applePayStrategy
        GOOGLE_PAY          | null   | googlePayStrategy
    }

    @Test
    def 'should throw exception when no strategy is found'()
    {
        given:
        transaction.paymentProvider >> TAX_CALCULATION.toString()

        when:
        context.strategy(transaction)

        then:
        thrown(IllegalArgumentException)
    }

    @Test
    @Unroll
    def 'Should transform CJL alternative payment method into hybris'()
    {
        when:
        def result = context.transformPaymentMethod(cjlAltPaymentMethod)

        then:
        result == hybrisPaymentMethod

        where:
        cjlAltPaymentMethod              || hybrisPaymentMethod
        null                             || null
        AlternativePaymentMethod.PPL || PPL
    }
}
