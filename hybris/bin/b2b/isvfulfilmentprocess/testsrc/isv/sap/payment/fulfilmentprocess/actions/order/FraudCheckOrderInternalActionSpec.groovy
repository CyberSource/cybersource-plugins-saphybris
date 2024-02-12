package isv.sap.payment.fulfilmentprocess.actions.order

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.enums.OrderStatus
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.fraud.FraudService
import de.hybris.platform.fraud.impl.FraudServiceResponse
import de.hybris.platform.fraud.impl.FraudSymptom
import de.hybris.platform.fraud.model.FraudReportModel
import de.hybris.platform.fraud.model.FraudSymptomScoringModel
import de.hybris.platform.orderhistory.model.OrderHistoryEntryModel
import de.hybris.platform.orderprocessing.model.OrderProcessModel
import de.hybris.platform.servicelayer.model.ModelService
import de.hybris.platform.servicelayer.time.TimeService
import org.junit.Test
import spock.lang.Specification

@UnitTest
class FraudCheckOrderInternalActionSpec extends Specification
{
    def fraudService = Mock(FraudService)

    def providerName = 'testProvider'

    def modelService = Mock(ModelService)

    def timeService = Mock(TimeService)

    def action = new FraudCheckOrderInternalAction(
            fraudService: fraudService,
            providerName: providerName,
            modelService: modelService,
            timeService: timeService
    )

    @Test
    def 'Should mark order as not fraudulent'()
    {
        given:
        def process = Mock(OrderProcessModel)
        def order = Mock(OrderModel)
        process.order >> order

        and:
        def fraudServiceResponse = createFraudServiceResponse(450D)

        and:
        modelService.create(FraudReportModel) >> Mock(FraudReportModel)
        modelService.create(OrderHistoryEntryModel) >> Mock(OrderHistoryEntryModel)
        modelService.create(FraudSymptomScoringModel) >> Mock(FraudSymptomScoringModel)
        timeService.currentTime >> new Date()

        when:
        def transition = action.executeAction(process)

        then:
        1 * fraudService.recognizeOrderSymptoms(providerName, order) >> fraudServiceResponse
        1 * order.setFraudulent(Boolean.FALSE)
        1 * order.setPotentiallyFraudulent(Boolean.FALSE)
        1 * order.setStatus(OrderStatus.FRAUD_CHECKED)
        1 * modelService.save(order)

        and:
        transition == AbstractFraudCheckAction.Transition.OK
    }

    @Test
    def 'Should mark order as potentially fraudulent'()
    {
        given:
        def process = Mock(OrderProcessModel)
        def order = Mock(OrderModel)
        process.order >> order

        and:
        def fraudServiceResponse = createFraudServiceResponse(510D)

        and:
        modelService.create(FraudReportModel) >> Mock(FraudReportModel)
        modelService.create(OrderHistoryEntryModel) >> Mock(OrderHistoryEntryModel)
        modelService.create(FraudSymptomScoringModel) >> Mock(FraudSymptomScoringModel)
        timeService.currentTime >> new Date()

        when:
        def transition = action.executeAction(process)

        then:
        1 * fraudService.recognizeOrderSymptoms(providerName, order) >> fraudServiceResponse
        1 * order.setFraudulent(Boolean.FALSE)
        1 * order.setPotentiallyFraudulent(Boolean.TRUE)
        1 * order.setStatus(OrderStatus.FRAUD_CHECKED)
        1 * modelService.save(order)

        and:
        transition == AbstractFraudCheckAction.Transition.POTENTIAL
    }

    @Test
    def 'Should mark order as fraudulent'()
    {
        given:
        def process = Mock(OrderProcessModel)
        def order = Mock(OrderModel)
        process.order >> order

        and:
        def fraudServiceResponse = createFraudServiceResponse(580D)

        and:
        modelService.create(FraudReportModel) >> Mock(FraudReportModel)
        modelService.create(OrderHistoryEntryModel) >> Mock(OrderHistoryEntryModel)
        modelService.create(FraudSymptomScoringModel) >> Mock(FraudSymptomScoringModel)
        timeService.currentTime >> new Date()

        when:
        def transition = action.executeAction(process)

        then:
        1 * fraudService.recognizeOrderSymptoms(providerName, order) >> fraudServiceResponse
        1 * order.setFraudulent(Boolean.TRUE)
        1 * order.setPotentiallyFraudulent(Boolean.FALSE)
        1 * order.setStatus(OrderStatus.FRAUD_CHECKED)
        1 * modelService.save(order)

        and:
        transition == AbstractFraudCheckAction.Transition.FRAUD
    }

    def createFraudServiceResponse(double score)
    {
        def fraudServiceResponse = new FraudServiceResponse(providerName)
        def fraudSymptom = new FraudSymptom('Test explanation', score, 'Test symptom')
        fraudServiceResponse.symptoms = [fraudSymptom]
        fraudServiceResponse
    }
}
