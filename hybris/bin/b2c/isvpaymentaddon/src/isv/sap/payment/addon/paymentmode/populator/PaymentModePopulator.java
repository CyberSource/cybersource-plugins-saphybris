package isv.sap.payment.addon.paymentmode.populator;

import de.hybris.platform.acceleratorservices.payment.data.PaymentModeData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import isv.sap.payment.model.IsvPaymentModeModel;

public class PaymentModePopulator implements Populator<IsvPaymentModeModel, PaymentModeData>
{
    @Override
    public void populate(final IsvPaymentModeModel source, final PaymentModeData target) throws ConversionException
    {
        target.setCode(source.getCode());
        target.setName(source.getName());
        target.setActive(source.getActive());
        target.setDescription(source.getDescription());
        target.setPaymentType(source.getPaymentType().getCode());
        if (source.getPaymentSubType() != null)
        {
            target.setPaymentSubType(source.getPaymentSubType().getCode());
        }
        target.setPaymentInfoType(source.getPaymentInfoType().getCode());
    }
}
