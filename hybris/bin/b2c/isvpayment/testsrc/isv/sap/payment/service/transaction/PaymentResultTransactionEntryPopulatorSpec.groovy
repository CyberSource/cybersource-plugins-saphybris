package isv.sap.payment.service.transaction

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.c2l.CurrencyModel
import de.hybris.platform.servicelayer.i18n.CommonI18NService
import de.hybris.platform.servicelayer.time.TimeService
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.service.executor.PaymentServiceResult
import isv.sap.payment.model.IsvPaymentTransactionEntryModel

@UnitTest
class PaymentResultTransactionEntryPopulatorSpec extends Specification
{

    def timeService = Mock([useObjenesis: false], TimeService)
    def commonI18NService = Mock([useObjenesis: false], CommonI18NService)

    def populator = new PaymentResultTransactionEntryPopulator()

    def setup()
    {
        populator.timeService = timeService
        populator.commonI18NService = commonI18NService
    }

    @Test
    def 'should populate IsvPaymentTransactionEntryModel'()
    {
        given:
        def destination = new IsvPaymentTransactionEntryModel()

        def source = PaymentServiceResult.create()
        source.addData('requestID', 'reqID')
        source.addData('requestToken', 'reqToken')
        source.addData('decision', 'decision')
        source.addData('reasonCode', 'reasonCode')
        source.addData('purchaseTotalsCurrency', 'GBP')
        source.addData('amount', '10')
        source.addData('paySubscriptionCreateReplySubscriptionID', 'subscriptionID')
        timeService.currentTime >> new GregorianCalendar(2014, 2, 11).time
        def currency = Mock([useObjenesis: false], CurrencyModel)
        commonI18NService.getCurrency('GBP') >> currency
        source.addData('someProperty', 'test')

        when:
        populator.populate(source, destination)

        then:
        destination.requestId == 'reqID'
        destination.requestToken == 'reqToken'
        destination.transactionStatus == 'decision'
        destination.time == new GregorianCalendar(2014, 2, 11).time
        destination.transactionStatusDetails == 'reasonCode'
        destination.currency == currency
        destination.amount == BigDecimal.TEN
        destination.subscriptionID == 'subscriptionID'
        with(destination.properties) {
            requestID == 'reqID'
            requestToken == 'reqToken'
            decision == 'decision'
            reasonCode == 'reasonCode'
            purchaseTotalsCurrency == 'GBP'
            amount == '10'
            paySubscriptionCreateReplySubscriptionID == 'subscriptionID'
            someProperty == 'test'
        }
    }

    @Test
    def 'should populate IsvPaymentTransactionEntryModel having nulls'()
    {
        given:
        def destination = new IsvPaymentTransactionEntryModel()

        def source = PaymentServiceResult.create()
        source.addData('requestID', 'reqID')
        source.addData('requestToken', 'reqToken')
        source.addData('decision', 'decision')
        source.addData('reasonCode', 'reasonCode')
        source.addData('purchaseTotalsCurrency', null)
        source.addData('amount', null)
        source.addData('paySubscriptionCreateReplySubscriptionID', 'subscriptionID')
        timeService.currentTime >> new GregorianCalendar(2014, 2, 11).time
        source.addData('someProperty', null)

        when:
        populator.populate(source, destination)

        then:
        destination.requestId == 'reqID'
        destination.requestToken == 'reqToken'
        destination.transactionStatus == 'decision'
        destination.time == new GregorianCalendar(2014, 2, 11).time
        destination.transactionStatusDetails == 'reasonCode'
        destination.currency == null
        destination.amount == null
        destination.subscriptionID == 'subscriptionID'
        with(destination.properties) {
            requestID == 'reqID'
            requestToken == 'reqToken'
            decision == 'decision'
            reasonCode == 'reasonCode'
            paySubscriptionCreateReplySubscriptionID == 'subscriptionID'
        }
        !destination.properties.containsKey('someProperty')
    }
}
