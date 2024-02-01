package isv.sap.payment.fulfilmentprocess.strategy.impl

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.OrderModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.service.executor.PaymentServiceExecutor
import isv.cjl.payment.service.executor.PaymentServiceResult
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.sap.payment.model.IsvPaymentTransactionEntryModel
import isv.sap.payment.model.IsvPaymentTransactionModel

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION

@UnitTest
class AbstractPaymentOperationStrategySpec extends Specification
{
    def request = new PaymentServiceRequest()

    def executor = Mock([useObjenesis: false], PaymentServiceExecutor)

    def transaction = new IsvPaymentTransactionEntryModel()

    def order = new OrderModel()

    def auth = new IsvPaymentTransactionModel()

    @SuppressWarnings('BracesForClass')
    def strategy = new AbstractPaymentOperationStrategy() {
        @Override
        PaymentType getPaymentType()
        {
            throw new UnsupportedOperationException('should not be invoked here.')
        }

        @Override
        protected PaymentServiceRequest request(final OrderModel vcOrder, final IsvPaymentTransactionModel vcAuth)
        {
            vcOrder == order && vcAuth == auth ? request : null
        }
    }

    def 'setup'()
    {
        executor.execute(request) >> PaymentServiceResult.create().addData(TRANSACTION, transaction)
        strategy.paymentServiceExecutor = executor
    }

    @Test
    def 'should return transaction as result of take payment request'()
    {
        expect:
        strategy.execute(order, auth) == transaction
    }

    @Test
    def 'should return null value for alternative payment method'()
    {
        expect:
        strategy.paymentMethod == null
    }
}
