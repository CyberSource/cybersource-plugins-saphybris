package isv.sap.payment.addon.address.converters.populator

import spock.lang.Specification
import org.junit.Test

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.c2l.CountryModel
import de.hybris.platform.core.model.c2l.RegionModel
import de.hybris.platform.core.model.user.AddressModel
import de.hybris.platform.servicelayer.i18n.CommonI18NService

@UnitTest
class VisaCheckoutDeliveryAddressReversePopulatorSpec extends Specification
{
    def commonI18NService = Mock(CommonI18NService)
    def populator = new VisaCheckoutDeliveryAddressReversePopulator(
            commonI18NService: commonI18NService
    )

    @Test
    def 'Should populate delivery address model using the info from transaction properties'()
    {
        given:
        def vCTProps = createVCTransactionProperties()
        def deliveryAddressModel = Mock(AddressModel)
        def countryModel = Mock(CountryModel)
        countryModel.isocode >> 'US'
        deliveryAddressModel.country >> countryModel
        def regionModel = Mock(RegionModel)
        regionModel.isocode >> 'NY'

        when:
        populator.populate(vCTProps, deliveryAddressModel)

        then:
        1 * commonI18NService.getCountry('US') >> countryModel
        1 * commonI18NService.getRegion(countryModel, _ as String) >> regionModel

        and:
        1 * deliveryAddressModel.setTown('New York')
        1 * deliveryAddressModel.setCountry(countryModel)
        1 * deliveryAddressModel.setRegion(regionModel)
        1 * deliveryAddressModel.setFirstname('John')
        1 * deliveryAddressModel.setLastname('Doe')
        1 * deliveryAddressModel.setPhone1('123456789')
        1 * deliveryAddressModel.setPostalcode('1234')
        1 * deliveryAddressModel.setStreetname('ship to street line 1')
        1 * deliveryAddressModel.setStreetnumber('ship to street line 2')
    }

    def createVCTransactionProperties()
    {
        [
                shipToCity       : 'New York',
                shipToCountry    : 'US',
                shipToState      : 'NY',
                shipToPhoneNumber: '123456789',
                shipToPostalCode : '1234',
                shipToStreet1    : 'ship to street line 1',
                shipToStreet2    : 'ship to street line 2',
                shipToName       : 'John Doe',
        ]
    }
}
