package isv.sap.payment.addon.strategy

import de.hybris.bootstrap.annotations.UnitTest
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.enums.AlternativePaymentMethod

@UnitTest
class AlternativePaymentSaleRequesterSpec extends Specification
{
    @Test
    def 'getSaleRequester: Should find supported saleRequester'()
    {
        given:
        AlternativePaymentSaleRequester requester1 = Mock([useObjenesis: false])
        AlternativePaymentSaleRequester requester2 = Mock([useObjenesis: false])
        AlternativePaymentSaleRequester requester3 = Mock([useObjenesis: false])
        requester3.supports(AlternativePaymentMethod.SOF) >> true
        def requesters = [requester1, requester2, requester3]

        when:
        def result = AlternativePaymentSaleRequester.getSaleRequester(requesters, AlternativePaymentMethod.SOF)

        then:
        requester3.is(result)
    }

    @Test
    def 'getSaleRequester: Should throw exception if requester cant be found'()
    {
        given:
        AlternativePaymentSaleRequester requester1 = Mock([useObjenesis: false])
        AlternativePaymentSaleRequester requester2 = Mock([useObjenesis: false])
        AlternativePaymentSaleRequester requester3 = Mock([useObjenesis: false])
        def requesters = [requester1, requester2, requester3]

        when:
        AlternativePaymentSaleRequester.getSaleRequester(requesters, AlternativePaymentMethod.SOF)

        then:
        def ex = thrown(IllegalStateException)
        ex.message == 'There is no AlternativePaymentSaleRequester for type: SOF'
    }
}
