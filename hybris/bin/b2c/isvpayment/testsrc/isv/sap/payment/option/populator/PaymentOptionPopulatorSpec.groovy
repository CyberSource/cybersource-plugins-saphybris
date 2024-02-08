package isv.sap.payment.option.populator

import de.hybris.bootstrap.annotations.UnitTest
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.data.AlternativePaymentOptionData
import isv.sap.payment.model.IsvAlternativePaymentOptionModel

@UnitTest
class PaymentOptionPopulatorSpec extends Specification
{
    def source = new IsvAlternativePaymentOptionModel(id: 'id', name: 'name')

    @Test
    def 'should populate alternative option data instance'()
    {
        given:
        def target = new AlternativePaymentOptionData()

        when:
        new PaymentOptionPopulator().populate(source, target)

        then:
        target.id == source.id
        target.name == source.name
    }
}
