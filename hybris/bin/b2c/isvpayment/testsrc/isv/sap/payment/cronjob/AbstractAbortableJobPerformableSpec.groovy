package isv.sap.payment.cronjob

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.cronjob.model.CronJobModel
import de.hybris.platform.servicelayer.cronjob.PerformResult
import org.junit.Test
import spock.lang.Specification

@UnitTest
class AbstractAbortableJobPerformableSpec extends Specification
{
    @SuppressWarnings('BracesForClass')
    def performable = new AbstractAbortableJobPerformable<CronJobModel>() {
        @Override
        PerformResult perform(final CronJobModel cronJobModel)
        {
            throw new UnsupportedOperationException('should not be invoked here')
        }
    }

    @Test
    def 'performable operation should be abortable'()
    {
        when:
        def isAbortable = performable.isAbortable()

        then:
        isAbortable
    }
}
