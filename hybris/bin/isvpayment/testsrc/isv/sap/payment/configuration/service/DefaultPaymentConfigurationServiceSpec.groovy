package isv.sap.payment.configuration.service

import de.hybris.bootstrap.annotations.UnitTest
import org.junit.Test
import spock.lang.Specification

import isv.sap.payment.configuration.resolver.PaymentConfigurationResolver
import isv.sap.payment.model.IsvMerchantPaymentConfigurationModel

import static isv.sap.payment.enums.IsvConfigurationType.MERCHANT_CONFIG

@UnitTest
class DefaultPaymentConfigurationServiceSpec extends Specification
{
    def service = new DefaultPaymentConfigurationService()

    def type = MERCHANT_CONFIG
    def params = [:]

    @Test
    def 'should provide configuration'()
    {
        given:
        def resolver = Mock(PaymentConfigurationResolver)
        def expected = new IsvMerchantPaymentConfigurationModel()

        and:
        resolver.resolve(params) >> expected
        service.resolverMap = [(type): resolver]

        when:
        def actual = service.getConfiguration(type, params)

        then:
        actual == expected
    }

    @Test
    def 'should throw exception when no resolver found'()
    {
        when:
        service.getConfiguration(type, params)

        then:
        thrown(IllegalArgumentException)
    }
}
