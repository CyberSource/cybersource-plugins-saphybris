package isv.sap.payment.data

import de.hybris.bootstrap.annotations.UnitTest
import org.junit.Test
import spock.lang.Specification

@UnitTest
class PaymentSystemInfoSpec extends Specification
{
    @Test
    def 'should provide correct clientApplication'()
    {
        given:
        def paymentSystemInfo = new PaymentSystemInfo()

        expect:
        paymentSystemInfo.clientApplication == 'SOAP Toolkit API'
    }
}
