package isv.sap.payment.service.executor.request.converter.fraud

import de.hybris.bootstrap.annotations.UnitTest
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.configuration.transaction.PaymentTransaction
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.cjl.payment.service.request.RequestFactory

import static isv.cjl.payment.constants.PaymentServiceConstants.Fraud.ACCOUNT_TAKEOVER_PROTECTION
import static isv.cjl.payment.enums.DmeServiceEventType.LOGIN

@UnitTest
class AccountTakeoverProtectionRequestConverterSpec extends Specification
{
    def converter = new AccountTakeoverProtectionRequestConverter()

    def source = PaymentServiceRequest.create()

    def requestFactory = Mock([useObjenesis: false], RequestFactory)

    def paymentTransaction = new PaymentTransaction()

    def 'setup'()
    {
        converter.requestFactory = requestFactory
        requestFactory.request(ACCOUNT_TAKEOVER_PROTECTION) >> paymentTransaction

        source.addParam('merchantId', 'merchantId')
        source.addParam('merchantReferenceCode', 'merchantReferenceCode')
        source.addParam('deviceFingerprintID', 'deviceFingerprintID')
        source.addParam('dmeServiceEventType', LOGIN.name)
    }

    @Test
    def 'should convert payment service request to isv request'()
    {
        when:
        def target = converter.convert(source)

        then:
        target.requestFields['merchantId'] == 'merchantId'
        target.requestFields['merchantReferenceCode'] == 'merchantReferenceCode'
        target.requestFields['deviceFingerprintID'] == 'deviceFingerprintID'
        target.requestFields['dmeServiceEventType'] == LOGIN.name
        target.requestFields['dmeServiceRun'] == true
    }
}
