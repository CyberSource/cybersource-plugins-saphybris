package isv.sap.payment.option.service

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.servicelayer.model.ModelService
import de.hybris.platform.servicelayer.search.FlexibleSearchService
import de.hybris.platform.servicelayer.search.impl.SearchResultImpl
import org.junit.Test
import spock.lang.Specification

import isv.sap.payment.model.IsvAlternativePaymentOptionModel

import static isv.sap.payment.option.service.DefaultPaymentOptionService.OPTIONS_QUERY
import static java.util.Arrays.asList
import static org.apache.commons.collections.ListUtils.EMPTY_LIST

@UnitTest
class DefaultPaymentOptionServiceSpec extends Specification
{
    def modelService = Mock([useObjenesis: false], ModelService)

    def flexibleSearchService = Mock([useObjenesis: false], FlexibleSearchService)

    def options = asList(new IsvAlternativePaymentOptionModel())

    def service = new DefaultPaymentOptionService()

    void setup()
    {
        service.modelService = modelService
        service.flexibleSearchService = flexibleSearchService
    }

    @Test
    def 'should return payment options'()
    {
        given:
        def result = new SearchResultImpl<IsvAlternativePaymentOptionModel>(options, 1, 1, 0)
        flexibleSearchService.search(OPTIONS_QUERY) >> result

        when:
        def actual = service.paymentOptions

        then:
        actual == options
    }

    @Test
    def 'should return empty list when payment options not found'()
    {
        given:
        def result = new SearchResultImpl<IsvAlternativePaymentOptionModel>(null, 1, 1, 0)
        flexibleSearchService.search(OPTIONS_QUERY) >> result

        when:
        def actual = service.paymentOptions

        then:
        actual == EMPTY_LIST
    }

    @Test
    def 'should update payment options'()
    {
        given:
        def newOptions = asList(new IsvAlternativePaymentOptionModel())
        def result = new SearchResultImpl<IsvAlternativePaymentOptionModel>(options, 1, 1, 0)
        flexibleSearchService.search(OPTIONS_QUERY) >> result

        when:
        service.updatePaymentOptions(newOptions)

        then:
        1 * modelService.removeAll(options)
        1 * modelService.saveAll(newOptions)
    }
}
