package isv.sap.payment.addon.address.converters.populator;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import isv.cjl.payment.data.enrollment.AddressData;

public class EnrollmentAddressPopulator implements Populator<AddressModel, AddressData>
{
    @Override
    public void populate(final AddressModel source, final AddressData target) throws ConversionException
    {
        target.setFirstName(source.getFirstname());
        target.setMiddleName(source.getMiddlename());
        target.setLastName(source.getLastname());
        target.setAddress1(source.getLine1());
        target.setAddress2(source.getLine2());
        target.setCity(source.getTown());
        target.setState(source.getRegion() != null ? source.getRegion().getName() : "");
        target.setPostalCode(source.getPostalcode());
        target.setCountryCode(source.getCountry() != null ? source.getCountry().getIsocode() : "");
        target.setPhone1(source.getPhone1());
        target.setPhone2(source.getPhone2());
    }
}
