package isv.sap.payment.option.populator;

import de.hybris.platform.converters.Populator;

import isv.cjl.payment.data.AlternativePaymentOptionData;
import isv.sap.payment.model.IsvAlternativePaymentOptionModel;

public class PaymentOptionReversePopulator
        implements Populator<AlternativePaymentOptionData, IsvAlternativePaymentOptionModel>
{
    @Override
    public void populate(final AlternativePaymentOptionData source, final IsvAlternativePaymentOptionModel target)
    {
        target.setId(source.getId());
        target.setName(source.getName());
    }
}
