package isv.sap.payment.addon.address.converters.populator

import isv.cjl.payment.data.enrollment.AddressData
import spock.lang.Specification
import org.junit.Test

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.c2l.CountryModel
import de.hybris.platform.core.model.c2l.RegionModel
import de.hybris.platform.core.model.user.AddressModel

@UnitTest
class EnrollmentAddressPopulatorSpec extends Specification
{
    def populator = new EnrollmentAddressPopulator()

    @Test
    def 'Should populate address data for enrollment transactions'()
    {
        given:
        def addressModel = createAddressModel()
        def addressData = new AddressData()

        when:
        populator.populate(addressModel, addressData)

        then:
        addressData.firstName == 'Bon'
        addressData.middleName == 'John'
        addressData.lastName == 'Doe'
        addressData.address1 == 'Maiden Ln'
        addressData.address2 == '27'
        addressData.city == 'San Francisco'
        addressData.state == 'California'
        addressData.postalCode == '91929'
        addressData.countryCode == 'US'
        addressData.phone1 == '1234567890'
        addressData.phone2 == '0987654321'
    }

    def createAddressModel()
    {
        def addressModel = Mock(AddressModel)
        addressModel.firstname >> 'Bon'
        addressModel.middlename >> 'John'
        addressModel.lastname >> 'Doe'
        addressModel.line1 >> 'Maiden Ln'
        addressModel.line2 >> '27'
        addressModel.town >> 'San Francisco'
        addressModel.postalcode >> '91929'
        addressModel.phone1 >> '1234567890'
        addressModel.phone2 >> '0987654321'

        def regionModel = Mock(RegionModel)
        regionModel.name >> 'California'
        addressModel.region >> regionModel

        def countryModel = Mock(CountryModel)
        countryModel.isocode >> 'US'
        addressModel.country >> countryModel
        addressModel
    }
}
