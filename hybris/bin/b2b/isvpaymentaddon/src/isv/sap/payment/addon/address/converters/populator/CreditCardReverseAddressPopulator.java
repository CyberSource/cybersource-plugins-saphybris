package isv.sap.payment.addon.address.converters.populator;

import javax.annotation.Resource;

import de.hybris.platform.acceleratorservices.payment.data.CustomerInfoData;
import de.hybris.platform.commerceservices.customer.CustomerEmailResolutionService;
import de.hybris.platform.commerceservices.strategies.CheckoutCustomerStrategy;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.lang.StringUtils;

import static org.apache.commons.lang.StringUtils.isNotBlank;

public class CreditCardReverseAddressPopulator implements Populator<CustomerInfoData, AddressModel>
{
    @Resource
    private UserService userService;

    @Resource
    private CommonI18NService commonI18NService;

    @Resource
    private CustomerEmailResolutionService customerEmailResolutionService;

    @Resource
    private CheckoutCustomerStrategy checkoutCustomerStrategy;

    @Override
    public void populate(final CustomerInfoData customerInfoData, final AddressModel addressModel)
    {
        addressModel.setFirstname(customerInfoData.getBillToFirstName());
        addressModel.setLastname(customerInfoData.getBillToLastName());
        //line1 and line2 are dynamic attributes, using streetname and streetnumber instead since addressModel
        // can be instantiated via converter
        addressModel.setStreetname(customerInfoData.getBillToStreet1());
        addressModel.setStreetnumber(customerInfoData.getBillToStreet2());
        addressModel.setTown(customerInfoData.getBillToCity());
        addressModel.setPostalcode(customerInfoData.getBillToPostalCode());

        if (isNotBlank(customerInfoData.getBillToTitleCode()))
        {
            addressModel.setTitle(userService.getTitleForCode(customerInfoData.getBillToTitleCode()));
        }

        setCountryToAddress(addressModel, customerInfoData.getBillToCountry());
        setRegionToAddress(addressModel, customerInfoData.getBillToState());
        setEmail(addressModel);
    }

    private void setCountryToAddress(final AddressModel address, final String billToCountry)
    {
        final CountryModel country = commonI18NService.getCountry(billToCountry);
        address.setCountry(country);
    }

    private void setRegionToAddress(final AddressModel address, final String state)
    {
        if (StringUtils.isNotEmpty(state))
        {
            address.setRegion(
                commonI18NService.getRegion(address.getCountry(), address.getCountry().getIsocode() + "-" + state));
        }
    }

    private void setEmail(final AddressModel addressModel)
    {
        final CustomerModel customerModel = checkoutCustomerStrategy.getCurrentUserForCheckout();
        final String email = customerEmailResolutionService.getEmailForCustomer(customerModel);
        addressModel.setEmail(email);
    }
}
