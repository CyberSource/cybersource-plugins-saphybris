package isv.sap.payment.addon.address.converters.populator

import spock.lang.Specification
import org.junit.Test

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.acceleratorservices.payment.data.CustomerInfoData
import de.hybris.platform.commerceservices.customer.CustomerEmailResolutionService
import de.hybris.platform.commerceservices.strategies.CheckoutCustomerStrategy
import de.hybris.platform.core.model.c2l.CountryModel
import de.hybris.platform.core.model.c2l.RegionModel
import de.hybris.platform.core.model.user.AddressModel
import de.hybris.platform.core.model.user.CustomerModel
import de.hybris.platform.core.model.user.TitleModel
import de.hybris.platform.servicelayer.i18n.CommonI18NService
import de.hybris.platform.servicelayer.user.UserService

@UnitTest
class CreditCardReverseAddressPopulatorSpec extends Specification
{
    def userService = Mock(UserService)
    def commonI18NService = Mock(CommonI18NService)
    def customerEmailResolutionService = Mock(CustomerEmailResolutionService)
    def checkoutCustomerStrategy = Mock(CheckoutCustomerStrategy)

    def populator = new CreditCardReverseAddressPopulator(
            userService: userService,
            commonI18NService: commonI18NService,
            customerEmailResolutionService: customerEmailResolutionService,
            checkoutCustomerStrategy: checkoutCustomerStrategy
    )

    @Test
    def 'Should populate address model using the info from customer data'()
    {
        given:
        def customerInfoData = createCustomerInfoData()
        def addressModel = Mock(AddressModel)
        def customerModel = Mock(CustomerModel)
        checkoutCustomerStrategy.getCurrentUserForCheckout() >> customerModel

        and:
        def countryModel = Mock(CountryModel)
        countryModel.isocode >> 'US'
        def regionModel = Mock(RegionModel)
        regionModel.isocode >> 'CA'
        def titleModel = Mock(TitleModel)
        titleModel.code >> 'mr'
        addressModel.country >> countryModel

        when:
        populator.populate(customerInfoData, addressModel)

        then:
        1 * userService.getTitleForCode('mr') >> titleModel
        1 * commonI18NService.getCountry('US') >> countryModel
        1 * commonI18NService.getRegion(countryModel, _ as String) >> regionModel
        1 * customerEmailResolutionService.getEmailForCustomer(customerModel) >> 'john.doe@example.com'

        and:
        1 * addressModel.setFirstname('John')
        1 * addressModel.setLastname('Doe')
        1 * addressModel.setStreetname('Maiden Ln')
        1 * addressModel.setStreetnumber('27')
        1 * addressModel.setTown('San Francisco')
        1 * addressModel.setPostalcode('94108')
        1 * addressModel.setTitle(titleModel)
        1 * addressModel.setCountry(countryModel)
        1 * addressModel.setRegion(regionModel)
        1 * addressModel.setEmail('john.doe@example.com')
    }

    def createCustomerInfoData()
    {
        def customerInfoData = new CustomerInfoData()
        customerInfoData.billToFirstName = 'John'
        customerInfoData.billToLastName = 'Doe'
        customerInfoData.billToStreet1 = 'Maiden Ln'
        customerInfoData.billToStreet2 = '27'
        customerInfoData.billToCity = 'San Francisco'
        customerInfoData.billToPostalCode = '94108'
        customerInfoData.billToTitleCode = 'mr'
        customerInfoData.billToCountry = 'US'
        customerInfoData.billToState = 'CA'
        customerInfoData
    }
}
