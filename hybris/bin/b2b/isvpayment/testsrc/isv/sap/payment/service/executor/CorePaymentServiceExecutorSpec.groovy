package isv.sap.payment.service.executor

import de.hybris.bootstrap.annotations.UnitTest
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.service.executor.PaymentServiceResult
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.sap.payment.model.IsvPaymentTransactionEntryModel
import isv.sap.payment.service.transaction.PaymentTransactionCreator
import isv.sap.payment.service.transaction.PaymentTransactionCreatorContext

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION

@UnitTest
class CorePaymentServiceExecutorSpec extends Specification
{
    def transactionCreatorContext = Mock([useObjenesis: false], PaymentTransactionCreatorContext)

    def paymentServiceResult = PaymentServiceResult.create()

    def paymentServiceRequest = PaymentServiceRequest.create()

    def paymentTransactionCreator = Mock([useObjenesis: false], PaymentTransactionCreator)

    def paymentTransactionEntryModel = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

    def serviceExecutor = Spy(new CorePaymentServiceExecutor())

    def setup()
    {
        serviceExecutor.paymentTransactionCreatorContext >> transactionCreatorContext
        serviceExecutor.executeSuper(_ as PaymentServiceRequest) >> paymentServiceResult

        transactionCreatorContext.getPaymentTransactionCreator(paymentServiceRequest) >> paymentTransactionCreator
        paymentTransactionCreator.createTransactionEntry(paymentServiceRequest, paymentServiceResult) >> paymentTransactionEntryModel
    }

    @Test
    def 'should add transaction entry to payment service result'()
    {
        when:
        serviceExecutor.execute(paymentServiceRequest)

        then:
        paymentServiceResult.getData(TRANSACTION) == paymentTransactionEntryModel
    }
}
