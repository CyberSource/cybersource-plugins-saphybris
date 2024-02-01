package isv.sap.payment.option.populator;

import de.hybris.platform.converters.Populator;

import isv.cjl.payment.data.AlternativePaymentOptionData;
import isv.sap.payment.model.IsvAlternativePaymentOptionModel;

public class PaymentOptionPopulator
        implements Populator<IsvAlternativePaymentOptionModel, AlternativePaymentOptionData>
{
    @Override
    public void populate(final IsvAlternativePaymentOptionModel source, final AlternativePaymentOptionData target)
    {
        target.setId(source.getId());
        target.setName(source.getName());
    }
}
