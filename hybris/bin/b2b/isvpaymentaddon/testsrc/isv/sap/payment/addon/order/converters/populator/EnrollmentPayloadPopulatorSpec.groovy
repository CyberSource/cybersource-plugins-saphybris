package isv.sap.payment.addon.order.converters.populator

import isv.cjl.payment.data.enrollment.AddressData
import isv.cjl.payment.data.enrollment.OrderData
import spock.lang.Specification
import org.junit.Test

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.commerceservices.customer.CustomerEmailResolutionService
import de.hybris.platform.converters.Populator
import de.hybris.platform.core.model.c2l.CurrencyModel
import de.hybris.platform.core.model.order.AbstractOrderEntryModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.core.model.product.ProductModel
import de.hybris.platform.core.model.user.AddressModel
import de.hybris.platform.core.model.user.CustomerModel

@UnitTest
class EnrollmentPayloadPopulatorSpec extends Specification
{
    def enrollmentAddressPopulator = Mock(Populator)

    def customerEmailResolutionService = Mock(CustomerEmailResolutionService)

    def populator = new EnrollmentPayloadPopulator(
            enrollmentAddressPopulator: enrollmentAddressPopulator,
            customerEmailResolutionService: customerEmailResolutionService
    )

    @Test
    def 'Should populate enrollment payload data'()
    {
        given:
        def orderModel = createOrder()
        def orderData = new OrderData()

        and:
        def paymentAddress = Stub(AddressModel)
        def deliveryAddress = Stub(AddressModel)
        orderModel.paymentAddress >> paymentAddress
        orderModel.deliveryAddress >> deliveryAddress
        def customerModel = Stub(CustomerModel)
        orderModel.user >> customerModel

        when:
        populator.populate(orderModel, orderData)

        then:
        1 * enrollmentAddressPopulator.populate(orderModel.deliveryAddress, _ as AddressData)
        1 * enrollmentAddressPopulator.populate(orderModel.paymentAddress, _ as AddressData)
        1 * customerEmailResolutionService.getEmailForCustomer(_ as CustomerModel) >> 'john.doe@example.com'

        and:
        orderData.orderDetails.orderNumber == 'code123455'
        orderData.orderDetails.amount == 10000.0D
        orderData.orderDetails.currencyCode == 'USD'

        def cartItems = orderData.cart
        cartItems[0].SKU == 'product12345'
        cartItems[0].name == 'Test Product'
        cartItems[0].quantity == 2

        orderData.consumer.email1 == 'john.doe@example.com'
        orderData.consumer.shippingAddress != null
        orderData.consumer.billingAddress != null
    }

    @Test
    def 'Should not populate email and addresses if no customer data is present '()
    {
        given:
        def orderModel = createOrder()
        def orderData = new OrderData()

        when:
        populator.populate(orderModel, orderData)

        then:
        0 * enrollmentAddressPopulator.populate(_, _)
        0 * enrollmentAddressPopulator.populate(_, _)
        0 * customerEmailResolutionService.getEmailForCustomer(_)

        and:
        orderData.orderDetails.orderNumber == 'code123455'
        orderData.orderDetails.amount == 10000.0D
        orderData.orderDetails.currencyCode == 'USD'

        def cartItems = orderData.cart
        cartItems[0].SKU == 'product12345'
        cartItems[0].name == 'Test Product'
        cartItems[0].quantity == 2

        orderData.consumer.email1 == null
        orderData.consumer.shippingAddress == null
        orderData.consumer.billingAddress == null
    }

    def createOrder()
    {
        def orderModel = Mock(AbstractOrderModel)
        orderModel.code >> 'code123455'
        orderModel.totalPrice >> 100.0D

        def currencyModel = Mock(CurrencyModel)
        currencyModel.isocode >> 'USD'
        orderModel.currency >> currencyModel

        def orderEntryModel = Mock(AbstractOrderEntryModel)
        orderEntryModel.quantity >> 2
        orderModel.entries >> [orderEntryModel]

        def productModel = Mock(ProductModel)
        productModel.code >> 'product12345'
        productModel.name >> 'Test Product'
        orderEntryModel.product >> productModel

        orderModel
    }
}
