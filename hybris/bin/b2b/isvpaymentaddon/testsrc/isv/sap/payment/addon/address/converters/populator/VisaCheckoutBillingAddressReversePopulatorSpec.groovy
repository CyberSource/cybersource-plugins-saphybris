package isv.sap.payment.addon.address.converters.populator

import spock.lang.Specification
import org.junit.Test

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.c2l.CountryModel
import de.hybris.platform.core.model.c2l.RegionModel
import de.hybris.platform.core.model.user.AddressModel
import de.hybris.platform.servicelayer.i18n.CommonI18NService

@UnitTest
class VisaCheckoutBillingAddressReversePopulatorSpec extends Specification
{
    def commonI18NService = Mock(CommonI18NService)
    def populator = new VisaCheckoutBillingAddressReversePopulator(
            commonI18NService: commonI18NService
    )

    @Test
    def 'Should populate billing address model using the info from transaction properties'()
    {
        given:
        def vCTProps = createVCTransactionProperties()
        def billingAddressModel = Mock(AddressModel)
        def countryModel = Mock(CountryModel)
        countryModel.isocode >> 'US'
        billingAddressModel.country >> countryModel
        def regionModel = Mock(RegionModel)
        regionModel.isocode >> 'NY'

        when:
        populator.populate(vCTProps, billingAddressModel)

        then:
        1 * commonI18NService.getCountry('US') >> countryModel
        1 * commonI18NService.getRegion(countryModel, _ as String) >> regionModel

        and:
        1 * billingAddressModel.setTown('New York')
        1 * billingAddressModel.setCountry(countryModel)
        1 * billingAddressModel.setRegion(regionModel)
        1 * billingAddressModel.setFirstname('John')
        1 * billingAddressModel.setLastname('Doe')
        1 * billingAddressModel.setPhone1('987654321')
        1 * billingAddressModel.setPostalcode('4321')
        1 * billingAddressModel.setStreetname('bill to street line 1')
        1 * billingAddressModel.setStreetnumber('bill to street line 2')
    }

    def createVCTransactionProperties()
    {
        [
                billToCity       : 'New York',
                billToCountry    : 'US',
                billToState      : 'NY',
                billToPhoneNumber: '987654321',
                billToPostalCode : '4321',
                billToStreet1    : 'bill to street line 1',
                billToStreet2    : 'bill to street line 2',
                billToName       : 'John Doe'
        ]
    }
}
