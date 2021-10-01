package isv.sap.payment.option.facade

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.servicelayer.dto.converter.Converter
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.data.AlternativePaymentOptionData
import isv.sap.payment.model.IsvAlternativePaymentOptionModel
import isv.sap.payment.option.service.PaymentOptionService

import static java.util.Arrays.asList

@UnitTest
class DefaultPaymentOptionFacadeSpec extends Specification
{
    def paymentOptionService = Mock([useObjenesis: false], PaymentOptionService)

    def paymentOptionConverter = Mock([useObjenesis: false], Converter)

    def facade = new DefaultPaymentOptionFacade()

    def optionData = new AlternativePaymentOptionData()

    void setup()
    {
        facade.paymentOptionService = paymentOptionService
        facade.paymentOptionConverter = paymentOptionConverter

        def optionModel = new IsvAlternativePaymentOptionModel()

        paymentOptionService.paymentOptions >> asList(optionModel)
        paymentOptionConverter.convert(optionModel) >> optionData
    }

    @Test
    def 'should return payment options data '()
    {
        when:
        def options = facade.paymentOptions

        then:
        options.size() == 1
        options.first() == optionData
    }
}
