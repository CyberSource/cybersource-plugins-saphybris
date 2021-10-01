package isv.sap.payment.addon.facade

import de.hybris.platform.commerceservices.customer.CustomerEmailResolutionService
import de.hybris.platform.core.model.order.CartModel
import de.hybris.platform.core.model.order.payment.PaymentInfoModel
import de.hybris.platform.core.model.user.AddressModel
import de.hybris.platform.core.model.user.CustomerModel
import de.hybris.platform.servicelayer.model.ModelService
import org.junit.Test
import spock.lang.Specification

import isv.sap.payment.addon.facade.impl.PaymentInfoFacadeImpl
import isv.sap.payment.model.IsvPaymentInfoModel

class PaymentInfoFacadeSpec extends Specification
{
    CustomerEmailResolutionService customerEmailResolutionService = Mock()

    ModelService modelService = Mock()

    def paymentInfoFacade = new PaymentInfoFacadeImpl()

    CartModel cart = Mock()

    AddressModel deliveryAddress = Mock()
    AddressModel billingAddress = Mock()
    PaymentInfoModel paymentInfo = Mock()
    CustomerModel customer = Mock()

    def setup()
    {
        paymentInfoFacade.modelService = modelService
        paymentInfoFacade.customerEmailResolutionService = customerEmailResolutionService

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
}
