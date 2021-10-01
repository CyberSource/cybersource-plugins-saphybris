package isv.sap.payment.cronjob

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.servicelayer.dto.converter.Converter
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.data.AlternativePaymentOptionData
import isv.cjl.payment.service.alternativepayment.AlternativePaymentOptionsService
import isv.sap.payment.model.IsvAlternativePaymentOptionModel
import isv.sap.payment.model.IsvAlternativePaymentOptionsCronJobModel
import isv.sap.payment.model.IsvMerchantModel
import isv.sap.payment.option.service.PaymentOptionService

import static de.hybris.platform.cronjob.enums.CronJobResult.SUCCESS
import static de.hybris.platform.cronjob.enums.CronJobStatus.FINISHED
import static java.util.Arrays.asList

@UnitTest
class UpdateAlternativePaymentOptionsJobSpec extends Specification
{
    def altPaymentOptionsService = Mock(AlternativePaymentOptionsService)

    def paymentOptionService = Mock(PaymentOptionService)

    def merchant = Mock(IsvMerchantModel)

    def paymentOptionReverseConverter = Mock(Converter)

    def cronJobModel = Mock(IsvAlternativePaymentOptionsCronJobModel)

    def optionModel = new IsvAlternativePaymentOptionModel()

    def optionData = new AlternativePaymentOptionData()

    def job = new UpdateAlternativePaymentOptionsJob()

    void setup()
    {
        job.paymentOptionService = paymentOptionService
        job.paymentOptionReverseConverter = paymentOptionReverseConverter
        job.alternativePaymentOptionsService = altPaymentOptionsService

        paymentOptionReverseConverter.convert(optionData) >> optionModel
    }

    @Test
    def 'should update alternative payment options'()
    {
        given:
        cronJobModel.merchant >> merchant
        merchant.id >> 'merchantID'
        altPaymentOptionsService.fetchAlternativePaymentOptions(merchant.id) >> [optionData]

        when:
        def jobResult = job.perform(cronJobModel)

        then:
        jobResult.status == FINISHED
        jobResult.result == SUCCESS

        1 * paymentOptionService.updatePaymentOptions(asList(optionModel))
    }
}
