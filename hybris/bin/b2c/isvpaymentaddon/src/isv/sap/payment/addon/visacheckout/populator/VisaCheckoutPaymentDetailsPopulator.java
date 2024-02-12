package isv.sap.payment.addon.visacheckout.populator;

import java.util.Map;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import isv.sap.payment.addon.VisaCheckoutPaymentDetailsData;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;

import static isv.cjl.payment.constants.PaymentConstants.VCResponseFields.BILL_TO_NAME;
import static isv.cjl.payment.constants.PaymentConstants.VCResponseFields.CARD_ART_FILE;
import static isv.cjl.payment.constants.PaymentConstants.VCResponseFields.CARD_EXPIRATION_MONTH;
import static isv.cjl.payment.constants.PaymentConstants.VCResponseFields.CARD_EXPIRATION_YEAR;
import static isv.cjl.payment.constants.PaymentConstants.VCResponseFields.CARD_SUFFIX;
import static isv.cjl.payment.constants.PaymentConstants.VCResponseFields.CARD_TYPE;

public class VisaCheckoutPaymentDetailsPopulator
        implements Populator<IsvPaymentTransactionEntryModel, VisaCheckoutPaymentDetailsData>
{
    @Override
    public void populate(final IsvPaymentTransactionEntryModel getDataTransactionEntryModel,
            final VisaCheckoutPaymentDetailsData target) throws ConversionException
    {
        final Map<String, String> properties = getDataTransactionEntryModel.getProperties();

        target.setBillToName(properties.get(BILL_TO_NAME));
        target.setCardArt(properties.get(CARD_ART_FILE));
        target.setCardExpirationMonth(properties.get(CARD_EXPIRATION_MONTH));
        target.setCardExpirationYear(properties.get(CARD_EXPIRATION_YEAR));
        target.setCardType(properties.get(CARD_TYPE));
        target.setCardSuffix(properties.get(CARD_SUFFIX));
    }
}
