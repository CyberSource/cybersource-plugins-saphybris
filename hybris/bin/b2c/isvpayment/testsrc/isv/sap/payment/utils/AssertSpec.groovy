package isv.sap.payment.utils

import java.util.function.Supplier

import de.hybris.bootstrap.annotations.UnitTest
import org.junit.Test
import spock.lang.Specification

@UnitTest
class AssertSpec extends Specification
{
    @Test
    @SuppressWarnings('BracesForClass')
    def 'assert should throw an exception for invalid expression'()
    {
        when:
        Assert.isTrue(false, new Supplier<RuntimeException>() {
            @Override
            RuntimeException get()
            {
                new RuntimeException('')
            }
        })

        then:
        thrown RuntimeException
    }
}
