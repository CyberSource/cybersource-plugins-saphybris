package isv.sap.payment.addon.order.converters.populator

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.commercefacades.order.data.OrderData
import de.hybris.platform.commercefacades.user.data.AddressData
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.core.model.order.payment.PaymentInfoModel
import de.hybris.platform.core.model.user.AddressModel
import de.hybris.platform.servicelayer.dto.converter.Converter
import org.junit.Test
import spock.lang.Specification

@UnitTest
class BillingOrderPopulatorSpec extends Specification
{
    def source = Mock([useObjenesis: false], OrderModel)

    def paymentAddressModel = Mock([useObjenesis: false], AddressModel)

    def paymentAddress = Mock([useObjenesis: false], AddressData)

    def target = Mock([useObjenesis: false], OrderData)

    def converter = Mock([useObjenesis: false], Converter)

    @SuppressWarnings('BracesForClass')
    def populator = new BillingOrderPopulator() {
        @Override
        protected void callSuperPopulate(final OrderModel source, final OrderData target)
        {
            // EMPTY
        }

        @Override
        protected Converter<AddressModel, AddressData> getAddressConverter()
        {
            converter
        }
    }

    @Test
    def 'should populate billing address'()
    {
        when:
        populator.populate(source, target)

        then:
        1 * source.paymentAddress >> paymentAddressModel
        1 * converter.convert(paymentAddressModel) >> paymentAddress
        1 * target.setBillingAddress(paymentAddress)
    }

    @Test
    def 'should populate paymentInfoData with billingAddressData'()
    {
        given:
        PaymentInfoModel paymentInfoModel = new PaymentInfoModel()
        paymentInfoModel.setBillingAddress(paymentAddressModel)

        when:
        populator.addPaymentInformation(source, target)

        then:
        1 * source.paymentInfo >> paymentInfoModel
        1 * converter.convert(paymentAddressModel) >> paymentAddress
        1 * target.setPaymentInfo { it -> it.billingAddress.is(paymentAddress) }
    }

    @Test
    def 'should not throw error if paymentInfo is null'()
    {
        when:
        populator.addPaymentInformation(source, target)

        then:
        1 * source.paymentInfo >> null
        0 * converter.convert(paymentAddressModel) >> paymentAddress
        0 * target.setPaymentInfo { it -> it.billingAddress.is(paymentAddress) }
    }

    @Test
    def 'should not throw error if paymentInfo has empty billing address'()
    {
        given:
        def paymentInfoModel = new PaymentInfoModel()

        when:
        populator.addPaymentInformation(source, target)

        then:
        1 * source.paymentInfo >> paymentInfoModel
        0 * converter.convert(paymentAddressModel) >> paymentAddress
        0 * target.setPaymentInfo { it -> it.billingAddress.is(paymentAddress) }
    }
}
