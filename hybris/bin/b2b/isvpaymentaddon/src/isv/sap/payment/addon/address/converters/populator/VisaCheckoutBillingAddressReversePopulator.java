package isv.sap.payment.addon.address.converters.populator;

import java.util.Map;

import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import isv.sap.payment.constants.IsvPaymentConstants;

public class VisaCheckoutBillingAddressReversePopulator
    extends AbstractVisaCheckoutAddressReversePopulator<Map<String, String>, AddressModel>
{
    @Override
    public void populate(final Map<String, String> transactionProperties, final AddressModel addressModel) throws ConversionException
    {
        addressModel.setTown(transactionProperties.get(IsvPaymentConstants.VCRequestFields.BILL_TO_CITY));
        setCountryToAddress(addressModel, transactionProperties.get(IsvPaymentConstants.VCRequestFields.BILL_TO_COUNTRY));
        setRegionToAddress(addressModel, transactionProperties.get(IsvPaymentConstants.VCRequestFields.BILL_TO_STATE));
        setFirstLastNameToAddress(addressModel, transactionProperties.get(IsvPaymentConstants.VCRequestFields.BILL_TO_NAME));
        addressModel.setPhone1(transactionProperties.get(IsvPaymentConstants.VCRequestFields.BILL_TO_PHONE_NUMBER));
        addressModel.setPostalcode(transactionProperties.get(IsvPaymentConstants.VCRequestFields.BILL_TO_POSTAL_CODE));
        //line1 and line2 are dynamic attributes, using streetname and streetnumber instead since addressModel
        // can be instantiated via converter
        addressModel.setStreetname(transactionProperties.get(IsvPaymentConstants.VCRequestFields.BILL_TO_STREET1));
        addressModel.setStreetnumber(transactionProperties.get(IsvPaymentConstants.VCRequestFields.BILL_TO_STREET2));
    }
}
