package isv.sap.payment.addon.address.converters.populator;

import javax.annotation.Resource;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import org.apache.commons.lang3.StringUtils;

import static org.apache.commons.lang.StringUtils.isNotBlank;

public abstract class AbstractVisaCheckoutAddressReversePopulator<SOURCE, TARGET extends AddressModel> implements Populator<SOURCE, TARGET>
{
    @Resource
    private CommonI18NService commonI18NService;

    protected void setFirstLastNameToAddress(final TARGET target, final String fullName)
    {
        if (isNotBlank(fullName))
        {
            final String[] splitFullName = fullName.split(" ", 2);
            target.setFirstname(splitFullName[0]);
            target.setLastname(splitFullName[1]);
        }
    }

    protected void setCountryToAddress(final TARGET target, final String billToCountry)
    {
        final CountryModel country = commonI18NService.getCountry(billToCountry);
        target.setCountry(country);
    }

    protected void setRegionToAddress(final TARGET target, final String state)
    {
        if (StringUtils.isNotEmpty(state))
        {
            target.setRegion(
                commonI18NService.getRegion(target.getCountry(), target.getCountry().getIsocode() + "-" + state));
        }
    }

    public void setCommonI18NService(final CommonI18NService commonI18NService)
    {
        this.commonI18NService = commonI18NService;
    }
}
