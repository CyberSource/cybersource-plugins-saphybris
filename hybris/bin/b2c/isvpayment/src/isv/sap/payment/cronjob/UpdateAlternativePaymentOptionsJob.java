package isv.sap.payment.cronjob;

import java.util.Collection;
import java.util.List;
import javax.annotation.Resource;

import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import isv.cjl.payment.data.AlternativePaymentOptionData;
import isv.cjl.payment.service.alternativepayment.AlternativePaymentOptionsService;
import isv.sap.payment.model.IsvAlternativePaymentOptionModel;
import isv.sap.payment.model.IsvAlternativePaymentOptionsCronJobModel;
import isv.sap.payment.option.service.PaymentOptionService;

import static de.hybris.platform.cronjob.enums.CronJobResult.SUCCESS;
import static de.hybris.platform.cronjob.enums.CronJobStatus.FINISHED;
import static java.util.stream.Collectors.toList;

/**
 * A job performable implementation that encapsulates alternative payment options update on
 * a scheduled basis.
 */
public class UpdateAlternativePaymentOptionsJob
        extends AbstractAbortableJobPerformable<IsvAlternativePaymentOptionsCronJobModel>
{
    @Resource(name = "isv.sap.payment.paymentOptionService")
    private PaymentOptionService paymentOptionService;

    @Resource(name = "isv.sap.payment.paymentOptionReverseConverter")
    private Converter<AlternativePaymentOptionData, IsvAlternativePaymentOptionModel> paymentOptionReverseConverter;

    @Resource(name = "isv.cjl.payment.service.alternativepayment.alternativePaymentOptionsService")
    private AlternativePaymentOptionsService alternativePaymentOptionsService;

    /**
     * Updates alternative payment options through a call to payment options service instance.
     *
     * @param cronJob cron job model that holds configuration for current execution
     * @return an instance of {@link PerformResult} that encapsulates status and result.
     */
    @Override
    public PerformResult perform(final IsvAlternativePaymentOptionsCronJobModel cronJob)
    {
        final Collection<AlternativePaymentOptionData> options = alternativePaymentOptionsService
                .fetchAlternativePaymentOptions(
                        cronJob.getMerchant().getId());

        final List<IsvAlternativePaymentOptionModel> optionModels = options.stream()
                .map(data -> paymentOptionReverseConverter.convert(data))
                .collect(toList());

        paymentOptionService.updatePaymentOptions(optionModels);

        return new PerformResult(SUCCESS, FINISHED);
    }

    public void setPaymentOptionService(final PaymentOptionService paymentOptionService)
    {
        this.paymentOptionService = paymentOptionService;
    }

    public void setPaymentOptionReverseConverter(
            final Converter<AlternativePaymentOptionData, IsvAlternativePaymentOptionModel> paymentOptionReverseConverter)
    {
        this.paymentOptionReverseConverter = paymentOptionReverseConverter;
    }
}
