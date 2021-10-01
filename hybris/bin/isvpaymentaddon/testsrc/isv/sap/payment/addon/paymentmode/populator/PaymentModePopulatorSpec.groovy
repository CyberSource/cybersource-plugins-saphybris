package isv.sap.payment.addon.paymentmode.populator

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.acceleratorservices.payment.data.PaymentModeData
import de.hybris.platform.core.model.type.ComposedTypeModel
import org.junit.Test
import spock.lang.Specification

import isv.sap.payment.enums.AlternativePaymentMethod
import isv.sap.payment.model.IsvPaymentModeModel

import static isv.sap.payment.enums.PaymentType.CREDIT_CARD

@UnitTest
class PaymentModePopulatorSpec extends Specification
{
    def source = Mock([useObjenesis: false], IsvPaymentModeModel)

    def setup()
    {
        source.code >> 'code'
        source.name >> 'name'
        source.active >> true
        source.description >> 'description'
        source.paymentType >> CREDIT_CARD
        source.paymentInfoType >> new ComposedTypeModel()
    }

    @Test
    def 'should populate payment mode data'()
    {
        given:
        def target = new PaymentModeData()
        source.paymentSubType >> paymentSubType

        when:
        new PaymentModePopulator().populate(source, target)

        then:
        target.code == source.code
        target.name == source.name
        target.active == source.active
        target.description == source.description
        target.paymentType == 'CREDIT_CARD'
        target.paymentInfoType == source.paymentInfoType.code
        target.paymentSubType == paymentSubTypeCode

        where:
        paymentSubType                   || paymentSubTypeCode
        AlternativePaymentMethod.KLI || 'KLI'
        null                             || null
    }
}
