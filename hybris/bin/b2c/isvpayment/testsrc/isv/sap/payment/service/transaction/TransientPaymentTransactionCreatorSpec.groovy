package isv.sap.payment.service.transaction

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.c2l.CurrencyModel
import de.hybris.platform.enumeration.EnumerationService
import de.hybris.platform.servicelayer.dto.converter.Converter
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.enums.PaymentTransactionType
import isv.cjl.payment.service.executor.PaymentServiceResult
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.sap.payment.model.IsvPaymentTransactionEntryModel

@UnitTest
class TransientPaymentTransactionCreatorSpec extends Specification
{
    def transactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

    def currency = Mock([useObjenesis: false], CurrencyModel)

    def coreRequest = PaymentServiceRequest.create().service(PaymentTransactionType.AUTHORIZATION)

    def paymentServiceResult = new PaymentServiceResult().create()

    def enumerationService = Mock([useObjenesis: false], EnumerationService)

    def paymentResultToTransactionEntryConverter = Mock(Converter)

    def creator = new TransientPaymentTransactionCreator()

    def setup()
    {
        creator.enumerationService = enumerationService
        creator.paymentResultToTransactionEntryConverter = paymentResultToTransactionEntryConverter

        enumerationService.getEnumerationValue(de.hybris.platform.payment.enums.PaymentTransactionType, 'AUTHORIZATION') >> de.hybris.platform.payment.enums.PaymentTransactionType.AUTHORIZATION
    }

    @Test
    def 'creator should create payment transaction entry from call to core library'()
    {
        given:
        paymentResultToTransactionEntryConverter.convert(paymentServiceResult) >> transactionEntry

        when:
        def result = creator.createTransactionEntry(coreRequest, paymentServiceResult)

        then:
        result == transactionEntry
    }
}
