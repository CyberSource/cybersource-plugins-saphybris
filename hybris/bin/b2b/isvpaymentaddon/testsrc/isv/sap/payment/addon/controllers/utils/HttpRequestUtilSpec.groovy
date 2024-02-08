package isv.sap.payment.addon.controllers.utils

import javax.servlet.http.HttpServletRequest

import de.hybris.bootstrap.annotations.UnitTest
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.exception.PaymentException
import isv.sap.payment.addon.utils.HttpRequestUtil

@UnitTest
class HttpRequestUtilSpec extends Specification
{
    @Test
    def 'should convert request parameters into a map'()
    {
        given:
        def request = Mock([useObjenesis: false], HttpServletRequest)

        and:
        request.parameterMap >> [param1: ['value1'] as String[], param2: ['value2'] as String[]]

        when:
        def params = HttpRequestUtil.getParametersMap(request)

        then:
        params.param1 == 'value1'
        params.param2 == 'value2'
    }

    @Test
    def 'should throw error on large parameters map'()
    {
        given:
        def request = Mock([useObjenesis: false], HttpServletRequest)
        def parameterMap = Mock([useObjenesis: false], Map)

        and:
        request.parameterMap >> parameterMap
        parameterMap.size() >> 600

        when:
        HttpRequestUtil.getParametersMap(request)

        then:
        thrown PaymentException
    }
}
