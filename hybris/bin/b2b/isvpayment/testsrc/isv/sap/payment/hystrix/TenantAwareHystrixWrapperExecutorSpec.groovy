package isv.sap.payment.hystrix

import com.google.common.base.Supplier
import de.hybris.bootstrap.annotations.UnitTest
import org.junit.Test
import spock.lang.Specification

@UnitTest
class TenantAwareHystrixWrapperExecutorSpec extends Specification
{
    def action = Mock([useObjenesis: false], Supplier)

    def executor = new TenantAwareHystrixWrapperExecutor('xxx')

    @Test
    def 'executor should run provided action'()
    {
        when:
        executor.execute('authorizeCommand', action)

        then:
        1 * action.get()
        executor.groupName == 'xxx'
    }
}
