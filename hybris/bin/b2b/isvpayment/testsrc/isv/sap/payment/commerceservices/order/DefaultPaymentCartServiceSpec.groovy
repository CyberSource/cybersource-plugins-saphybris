package isv.sap.payment.commerceservices.order

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.PK
import de.hybris.platform.core.model.order.CartModel
import de.hybris.platform.servicelayer.model.ModelService
import de.hybris.platform.servicelayer.tx.MockTransactionManager
import org.junit.Test
import org.springframework.transaction.support.TransactionCallbackWithoutResult
import org.springframework.transaction.support.TransactionTemplate
import spock.lang.Specification

import isv.sap.payment.commerceservices.order.dao.PaymentCartDao

@UnitTest
class DefaultPaymentCartServiceSpec extends Specification
{
    def cart = Mock([useObjenesis: false], CartModel)

    def paymentCartDao = Mock([useObjenesis: false], PaymentCartDao)

    def modelService = Mock([useObjenesis: false], ModelService)

    def transactionTemplate = Spy(TransactionTemplate, constructorArgs: [new MockTransactionManager()])

    def runnable = Mock([useObjenesis: false], Runnable)

    def service = new DefaultPaymentCartService()

    def setup()
    {
        service.paymentCartDao = paymentCartDao
        service.modelService = modelService
        service.transactionTemplate = transactionTemplate

        cart.pk >> PK.BIG_PK
    }

    @Test
    def 'should return cart model by given code'()
    {
        given:
        paymentCartDao.getCartForGuid('12345') >> cart

        when:
        def resultCart = service.getCartForGuid('12345')

        then:
        resultCart == cart
    }

    @Test
    def 'should return null if cannot get cart because of an exception'()
    {
        given:
        paymentCartDao.getCartForGuid('12345') >> { throw new RuntimeException('test exception') }

        when:
        def resultCart = service.getCartForGuid('12345')

        then:
        resultCart == null
    }

    @Test
    def 'should throw exception if cart code not provided'()
    {
        when:
        service.getCartForGuid(null)

        then:
        thrown IllegalArgumentException
    }

    @Test
    def 'should obtain lock on cart and execute given runnable'()
    {
        when:
        service.executeWithCartLock(cart, runnable)

        then:
        1 * transactionTemplate.execute(_ as TransactionCallbackWithoutResult)
        1 * modelService.lock(cart.pk)
        1 * runnable.run()
    }

    @Test
    def 'should execute given runnable without locking the cart if not supported'()
    {
        when:
        service.executeWithCartLock(cart, runnable)

        then:
        1 * transactionTemplate.execute(_ as TransactionCallbackWithoutResult)
        1 * modelService.lock(cart.pk) >> { throw new UnsupportedOperationException() }
        1 * runnable.run()
    }

    @Test
    def 'should not execute given runnable if an error occurs when getting the lock on cart'()
    {
        when:
        service.executeWithCartLock(cart, runnable)

        then:
        1 * transactionTemplate.execute(_ as TransactionCallbackWithoutResult)
        1 * modelService.lock(cart.pk) >> { throw new RuntimeException() }
        0 * runnable.run()
    }
}
