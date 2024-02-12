package isv.sap.payment.addon.facade

import isv.sap.payment.addon.facade.impl.PaymentInfoFacadeImpl
import isv.sap.payment.model.IsvPaymentInfoModel
import spock.lang.Specification
import org.junit.Test

import de.hybris.platform.commercefacades.user.data.AddressData
import de.hybris.platform.commerceservices.customer.CustomerEmailResolutionService
import de.hybris.platform.core.model.order.CartModel
import de.hybris.platform.core.model.order.payment.PaymentInfoModel
import de.hybris.platform.core.model.user.AddressModel
import de.hybris.platform.core.model.user.CustomerModel
import de.hybris.platform.order.CartService
import de.hybris.platform.servicelayer.dto.converter.Converter
import de.hybris.platform.servicelayer.model.ModelService

class PaymentInfoFacadeSpec extends Specification
{
    def customerEmailResolutionService = Mock(CustomerEmailResolutionService)

    def modelService = Mock(ModelService)

    def cartService = Mock(CartService)

    def addressConverter = Mock(Converter)

    def paymentInfoFacade = new PaymentInfoFacadeImpl(
            customerEmailResolutionService: customerEmailResolutionService,
            cartService: cartService,
            modelService: modelService,
            addressConverter: addressConverter
    )

    def cart = Mock(CartModel)

    def deliveryAddress = Mock(AddressModel)

    def billingAddress = Mock(AddressModel)

    def paymentInfo = Mock(PaymentInfoModel)

    def customer = Mock(CustomerModel)

    def setup()
    {
        cart.deliveryAddress >> deliveryAddress
        cart.paymentInfo >> paymentInfo
        paymentInfo.billingAddress >> billingAddress
    }

    @Test
    def 'Should not resolve payment info when already existing'()
    {
        given:
        cart.paymentInfo >> paymentInfo

        when:
        def result = paymentInfoFacade.resolvePaymentInfo(cart, customer)

        then:
        result == paymentInfo
    }

    @Test
    def 'Should resolve payment info when not existing'()
    {
        given:
        def isvPaymentInfo = Mock(IsvPaymentInfoModel)

        customerEmailResolutionService.getEmailForCustomer(customer) >> 'email'
        modelService.create(IsvPaymentInfoModel) >> isvPaymentInfo

        when:
        paymentInfoFacade.resolvePaymentInfo(cart, customer)

        then:
        1 * cart.paymentInfo >> null
        2 * modelService.create(AddressModel) >> billingAddress >> deliveryAddress

        1 * billingAddress.setOwner(isvPaymentInfo)
        1 * billingAddress.setEmail('email')
        1 * deliveryAddress.setOwner(customer)

        1 * cart.setDeliveryAddress(deliveryAddress)
        1 * cart.setPaymentInfo(isvPaymentInfo)
        1 * modelService.saveAll(isvPaymentInfo, billingAddress, cart)
    }

    @Test
    def 'Should fetch billing address data from payment info of the cart in session'()
    {
        given:
        cart.paymentInfo >> paymentInfo
        def billingAddressData = Mock(AddressData)

        when:
        def addressDataOpt = paymentInfoFacade.fetchAddressFromPaymentInfo()

        then:
        1 * cartService.sessionCart >> cart
        1 * addressConverter.convert(paymentInfo.billingAddress) >> billingAddressData
        and:
        addressDataOpt.isPresent()
        addressDataOpt.get() == billingAddressData
    }
}
