package isv.sap.payment.option.populator

import de.hybris.bootstrap.annotations.UnitTest
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.data.AlternativePaymentOptionData
import isv.sap.payment.model.IsvAlternativePaymentOptionModel

@UnitTest
class PaymentOptionReversePopulatorSpec extends Specification
{
    def source = new AlternativePaymentOptionData(id: 'id', name: 'name')

    @Test
    def 'should populate alternative option model instance'()
    {
        given:
        def target = new IsvAlternativePaymentOptionModel()

        when:
        new PaymentOptionReversePopulator().populate(source, target)

        then:
        target.id == source.id
        target.name == source.name
    }
}
