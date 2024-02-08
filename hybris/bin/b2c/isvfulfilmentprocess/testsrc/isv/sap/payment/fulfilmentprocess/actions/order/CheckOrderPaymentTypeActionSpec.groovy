package isv.sap.payment.fulfilmentprocess.actions.order

import com.google.common.collect.ImmutableMap
import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.orderprocessing.model.OrderProcessModel
import de.hybris.platform.payment.model.PaymentTransactionEntryModel
import de.hybris.platform.payment.model.PaymentTransactionModel
import de.hybris.platform.servicelayer.model.ModelService
import org.junit.Test
import spock.lang.Specification

import isv.sap.payment.enums.PaymentType

import static isv.sap.payment.fulfilmentprocess.actions.order.CheckOrderPaymentTypeAction.Transition.ALTERNATIVE
import static isv.sap.payment.fulfilmentprocess.actions.order.CheckOrderPaymentTypeAction.Transition.APPLE_PAY
import static isv.sap.payment.fulfilmentprocess.actions.order.CheckOrderPaymentTypeAction.Transition.CREDIT_CARD
import static isv.sap.payment.fulfilmentprocess.actions.order.CheckOrderPaymentTypeAction.Transition.GOOGLE_PAY
import static isv.sap.payment.fulfilmentprocess.actions.order.CheckOrderPaymentTypeAction.Transition.VISA_CHECKOUT

@UnitTest
class CheckOrderPaymentTypeActionSpec extends Specification
{
    def order = Mock(OrderModel)

    def paymentTransaction = Mock(PaymentTransactionModel)

    PaymentTransactionEntryModel paymentTransactionEntry = new PaymentTransactionEntryModel()

    def businessProcess = Mock(OrderProcessModel)

    def modelService = Mock(ModelService)

    def checkOrderPaymentTypeAction = new CheckOrderPaymentTypeAction()

    void setup()
    {
        businessProcess.order >> order

        checkOrderPaymentTypeAction.setModelService(modelService)
        checkOrderPaymentTypeAction.setPaymentTypeMappings(
                ImmutableMap.<PaymentType, CheckOrderPaymentTypeAction.Transition> builder()
                        .put(PaymentType.CREDIT_CARD, CREDIT_CARD)
                        .put(PaymentType.ALTERNATIVE_PAYMENT, ALTERNATIVE)
                        .put(PaymentType.PAY_PAL, ALTERNATIVE)
                        .put(PaymentType.VISA_CHECKOUT, VISA_CHECKOUT)
                        .put(PaymentType.APPLE_PAY, APPLE_PAY)
                        .put(PaymentType.GOOGLE_PAY, GOOGLE_PAY)
                        .build())
    }

    @Test
    def 'action should validate transition value'()
    {
        given:
        order.paymentTransactions >> [paymentTransaction]
        paymentTransaction.entries >> [paymentTransactionEntry]
        paymentTransactionEntry.paymentTransaction = paymentTransaction
        paymentTransaction.paymentProvider >> paymentProvider

        when:
        def result = checkOrderPaymentTypeAction.execute(businessProcess)

        then:
        result == expectedTransition

        where:
        paymentProvider                     | expectedTransition
        PaymentType.ALTERNATIVE_PAYMENT | 'ALTERNATIVE'
        PaymentType.CREDIT_CARD         | 'CREDIT_CARD'
        PaymentType.PAY_PAL             | 'ALTERNATIVE'
        PaymentType.VISA_CHECKOUT       | 'VISA_CHECKOUT'
        PaymentType.APPLE_PAY           | 'APPLE_PAY'
        PaymentType.GOOGLE_PAY          | 'GOOGLE_PAY'
    }

    @Test
    def 'action should not validate on missing transition value'()
    {
        given:
        order.paymentTransactions >> []

        when:
        def result = checkOrderPaymentTypeAction.execute(businessProcess)

        then:
        result == 'NOK'
    }
}
