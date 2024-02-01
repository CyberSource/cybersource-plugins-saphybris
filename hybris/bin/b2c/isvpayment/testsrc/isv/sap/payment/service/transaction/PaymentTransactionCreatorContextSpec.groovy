package isv.sap.payment.service.transaction

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.AbstractOrderModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.service.executor.request.PaymentServiceRequest

@UnitTest
class PaymentTransactionCreatorContextSpec extends Specification
{
    def transientPaymentTransactionCreator = Mock([useObjenesis: false], PaymentTransactionCreator)

    def persistentPaymentTransactionCreator = Mock([useObjenesis: false], PaymentTransactionCreator)

    def coreRequest = PaymentServiceRequest.create()

    def context = new PaymentTransactionCreatorContext()

    def setup()
    {
        context.transientPaymentTransactionCreator = transientPaymentTransactionCreator
        context.persistentPaymentTransactionCreator = persistentPaymentTransactionCreator
    }

    @Test
    def 'should dispatch to persistent creator from call to core library'()
    {
        def order = Mock([useObjenesis: false], AbstractOrderModel)

        given:
        coreRequest.addParam('order', order)

        when:
        def creator = context.getPaymentTransactionCreator(coreRequest)

        then:
        creator == persistentPaymentTransactionCreator
    }

    @Test
    def 'should dispatch to transient creator from call to core library'()
    {
        when:
        def creator = context.getPaymentTransactionCreator(coreRequest)

        then:
        creator == transientPaymentTransactionCreator
    }
}
