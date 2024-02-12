package isv.sap.payment.option.facade;

import java.util.List;
import javax.annotation.Resource;

import de.hybris.platform.servicelayer.dto.converter.Converter;

import isv.cjl.payment.data.AlternativePaymentOptionData;
import isv.sap.payment.model.IsvAlternativePaymentOptionModel;
import isv.sap.payment.option.service.PaymentOptionService;

import static java.util.stream.Collectors.toList;

/**
 * Encapsulates the default implementation of {@link PaymentOptionFacade} interface.
 * <p>
 * This implementation is based on locally-stored alternative payment options.
 */
public class DefaultPaymentOptionFacade implements PaymentOptionFacade
{
    @Resource(name = "isv.sap.payment.paymentOptionService")
    private PaymentOptionService paymentOptionService;

    @Resource(name = "isv.sap.payment.paymentOptionConverter")
    private Converter<IsvAlternativePaymentOptionModel, AlternativePaymentOptionData> paymentOptionConverter;

    @Override
    public List<AlternativePaymentOptionData> getPaymentOptions()
    {
        return paymentOptionService.getPaymentOptions().stream()
                .map(model -> paymentOptionConverter.convert(model))
                .collect(toList());
    }

    public void setPaymentOptionService(final PaymentOptionService paymentOptionService)
    {
        this.paymentOptionService = paymentOptionService;
    }

    public void setPaymentOptionConverter(
            final Converter<IsvAlternativePaymentOptionModel, AlternativePaymentOptionData> paymentOptionConverter)
    {
        this.paymentOptionConverter = paymentOptionConverter;
    }
}
