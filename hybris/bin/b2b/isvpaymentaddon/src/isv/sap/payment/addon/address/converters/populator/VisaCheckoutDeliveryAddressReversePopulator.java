package isv.sap.payment.addon.address.converters.populator;

import java.util.Map;

import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import isv.sap.payment.constants.IsvPaymentConstants;

public class VisaCheckoutDeliveryAddressReversePopulator
    extends AbstractVisaCheckoutAddressReversePopulator<Map<String, String>, AddressModel>
{
    @Override
    public void populate(final Map<String, String> transactionProperties, final AddressModel addressModel) throws ConversionException
    {
        addressModel.setTown(transactionProperties.get("billToCity"));
        setCountryToAddress(addressModel, transactionProperties.get("billToCountry"));
        setRegionToAddress(addressModel, transactionProperties.get("billToState"));
        setFirstLastNameToAddress(addressModel, transactionProperties.get("billToName"));
        addressModel.setPhone1(transactionProperties.get("billToPhoneNumber"));
        addressModel.setPostalcode(transactionProperties.get("billToPostalCode"));
        addressModel.setStreetname(transactionProperties.get("billToStreet1"));
        addressModel.setStreetname(transactionProperties.get("billToStreet2"));
    }
}
