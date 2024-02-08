package isv.sap.payment.addon.b2b.facade.impl

import java.time.DayOfWeek
import java.time.Instant

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.b2bacceleratorfacades.checkout.data.PlaceOrderData
import de.hybris.platform.b2bacceleratorfacades.exception.EntityValidationException
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BPaymentTypeData
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BReplenishmentRecurrenceEnum
import de.hybris.platform.b2bacceleratorfacades.order.data.ScheduledCartData
import de.hybris.platform.b2bacceleratorfacades.order.data.TriggerData
import de.hybris.platform.commercefacades.order.data.CartData
import de.hybris.platform.commercefacades.order.data.DeliveryModeData
import de.hybris.platform.commercefacades.order.data.OrderData
import de.hybris.platform.commercefacades.user.data.AddressData
import de.hybris.platform.core.model.order.CartModel
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel
import de.hybris.platform.core.model.order.payment.PaymentInfoModel
import de.hybris.platform.core.model.user.AddressModel
import de.hybris.platform.order.CartService
import de.hybris.platform.payment.dto.TransactionStatus
import de.hybris.platform.payment.model.PaymentTransactionEntryModel
import de.hybris.platform.payment.model.PaymentTransactionModel
import de.hybris.platform.servicelayer.dto.converter.Converter
import de.hybris.platform.servicelayer.model.ModelService
import org.junit.Test
import spock.lang.Shared
import spock.lang.Specification

import isv.sap.payment.addon.b2b.helper.B2bPaymentAuthorizationHelper
import isv.sap.payment.addon.b2b.model.ReplenishmentInfoModel
import isv.sap.payment.addon.b2b.service.B2bPaymentTransactionService
import isv.sap.payment.enums.PaymentType

import static de.hybris.platform.payment.dto.TransactionStatus.ACCEPTED
import static de.hybris.platform.payment.dto.TransactionStatus.REJECTED
import static de.hybris.platform.payment.enums.PaymentTransactionType.CREATE_SUBSCRIPTION
import static isv.sap.payment.enums.PaymentType.CREDIT_CARD
import static isv.sap.payment.enums.PaymentType.PAY_PAL

@UnitTest
class IsvB2BAcceleratorCheckoutFacadeSpec extends Specification
{
    @Shared
    def address = Mock(AddressModel)

    @Shared
    def deliveryMode = Mock(DeliveryModeModel)

    @Shared
    def paymentInfo = Mock(PaymentInfoModel)

    def cart = Mock(CartModel)

    def order = Mock(OrderModel)

    def orderData = Mock(OrderData)

    def orderConverter = Mock(Converter)

    def replenishmentPlaceOrderConverter = Mock(Converter)

    def cartService = Mock(CartService)

    def modelService = Mock(ModelService)

    def b2bPaymentAuthorizationHelper = Mock(B2bPaymentAuthorizationHelper)

    def b2bPaymentTransactionService = Mock(B2bPaymentTransactionService)

    def facade = Spy(new IsvB2BAcceleratorCheckoutFacade(
            orderConverter: orderConverter,
            cartService: cartService,
            modelService: modelService,
            b2bPaymentAuthorizationHelper: b2bPaymentAuthorizationHelper,
            b2bPaymentTransactionService: b2bPaymentTransactionService,
            replenishmentPlaceOrderConverter: replenishmentPlaceOrderConverter
    ))

    def setup()
    {
        orderConverter.convert(order) >> orderData
    }

    @Test
    def 'should validate data for specified cart model'()
    {
        given:
        cart.deliveryAddress >> deliveryAddress
        cart.deliveryMode >> mode
        cart.paymentInfo >> payment

        when:
        def result = facade.validOrder(cart)

        then:
        result == expectedResult

        where:
        deliveryAddress | mode         | payment     | expectedResult
        address         | deliveryMode | paymentInfo | true
        null            | deliveryMode | paymentInfo | false
        address         | null         | paymentInfo | false
        address         | deliveryMode | null        | false
    }

    @Test
    def 'should place order and return order data object'()
    {
        when:
        def resultData = facade.performPlaceOrder(cart)

        then:
        1 * facade.beforePlaceOrder(cart) >> { }
        1 * facade.placeOrder(cart) >> order
        1 * facade.afterPlaceOrder(cart, _ as OrderModel) >> { }
        resultData == orderData
    }

    @Test
    def 'should place replenishment order and return replenishment order data object'()
    {
        given:
        def replenishmentInfo = Mock(ReplenishmentInfoModel)
        cart.replenishmentInfo >> replenishmentInfo
        def placeOrderData = createPlaceOrderData()

        and:
        def cartData = createCartData()
        facade.getCheckoutCart() >> cartData
        def scheduleCartData = new ScheduledCartData()

        when:
        def resultData = facade.performPlaceOrder(cart)

        then:
        1 * facade.beforePlaceOrder(cart) >> { }
        1 * replenishmentPlaceOrderConverter.convert(replenishmentInfo) >> placeOrderData
        1 * facade.populateTriggerDataFromPlaceOrderData(placeOrderData, _ as TriggerData) >> { }
        1 * facade.scheduleOrder(_ as TriggerData) >> scheduleCartData
        resultData == scheduleCartData
    }

    @Test
    def 'should throw exception when cart for replenishment is not valid'()
    {
        given:
        def replenishmentInfo = Mock(ReplenishmentInfoModel)
        cart.replenishmentInfo >> replenishmentInfo
        def placeOrderData = createPlaceOrderData()

        and:
        def cartData = new CartData()
        facade.getCheckoutCart() >> cartData

        when:
        facade.performPlaceOrder(cart)

        then:
        1 * facade.beforePlaceOrder(cart) >> { }
        1 * replenishmentPlaceOrderConverter.convert(replenishmentInfo) >> placeOrderData
        thrown(EntityValidationException)
    }

    @Test
    def 'should place regular order when cart is not for replenishment'()
    {
        given:
        def replenishmentInfo = Mock(ReplenishmentInfoModel)
        cart.replenishmentInfo >> replenishmentInfo
        def placeOrderData = createPlaceOrderData()
        placeOrderData.replenishmentOrder = false

        and:
        def cartData = createCartData()
        facade.getCheckoutCart() >> cartData

        when:
        def resultData = facade.performPlaceOrder(cart)

        then:
        1 * facade.beforePlaceOrder(cart) >> { }
        1 * replenishmentPlaceOrderConverter.convert(replenishmentInfo) >> placeOrderData
        1 * facade.placeOrder() >> orderData
        resultData == orderData
    }

    @Test
    def 'should return null if cart model not provided'()
    {
        when:
        def resultData = facade.performPlaceOrder(null)

        then:
        resultData == null
    }

    @Test
    def 'should remove given cart and refresh order model'()
    {
        given:
        cartService.hasSessionCart() >> false

        when:
        facade.afterPlaceOrder(cart, order)

        then:
        1 * modelService.remove(cart)
        1 * modelService.refresh(order)
    }

    @Test
    def 'should remove session cart and refresh order model'()
    {
        given:
        cartService.hasSessionCart() >> true

        when:
        facade.afterPlaceOrder(cart, order)

        then:
        1 * cartService.removeSessionCart()
        1 * modelService.refresh(order)
    }

    @Test
    def 'should do nothing if order is null'()
    {
        when:
        facade.afterPlaceOrder(cart, null)

        then:
        0 * cartService._
        0 * modelService._
    }

    @Test
    def 'authorize subscription should not apply to non credit card orders'()
    {
        given:
        def creditCardPaymentTransaction = createTransaction(CREDIT_CARD, REJECTED, Date.from(Instant.parse('2020-03-10T16:17:00.000Z')))
        def paypalPaymentTransaction = createTransaction(PAY_PAL, ACCEPTED, Date.from(Instant.parse('2020-03-10T16:18:00.000Z')))
        cart.paymentTransactions >> [creditCardPaymentTransaction, paypalPaymentTransaction]

        when:
        facade.beforePlaceOrder(cart)

        then:
        1 * facade.superBeforePlaceOrder(cart) >> null
        0 * b2bPaymentAuthorizationHelper.authorizePayment(_, cart)
    }

    @Test
    def 'authorize subscription should apply to credit card orders'()
    {
        given:
        def creditCardPaymentTransaction = createTransaction(CREDIT_CARD, ACCEPTED, Date.from(Instant.parse('2020-03-10T16:17:00.000Z')))
        cart.paymentTransactions >> [creditCardPaymentTransaction]

        when:
        facade.beforePlaceOrder(cart)

        then:
        1 * facade.superBeforePlaceOrder(cart) >> null
        1 * b2bPaymentTransactionService
                .getLatestAcceptedTransactionEntry(CREDIT_CARD, CREATE_SUBSCRIPTION, cart)
        1 * b2bPaymentAuthorizationHelper.authorizePayment(_, cart)
    }

    PaymentTransactionModel createTransaction(PaymentType type, TransactionStatus status, Date date)
    {
        def transaction = Mock(PaymentTransactionModel)
        transaction.paymentProvider >> type.name()
        def entry = Mock(PaymentTransactionEntryModel)
        entry.transactionStatus >> status.name()
        entry.time >> date
        entry.paymentTransaction >> transaction
        transaction.entries >> [entry]

        transaction
    }

    def createCartData()
    {
        def cartData = new CartData()
        cartData.calculated = true
        def deliveryAddress = new AddressData()
        cartData.deliveryAddress = deliveryAddress
        def deliveryMode = new DeliveryModeData()
        cartData.deliveryMode = deliveryMode
        def paymentType = new B2BPaymentTypeData()
        paymentType.code = 'CARD'
        cartData.paymentType = paymentType

        cartData
    }

    def createPlaceOrderData()
    {
        def placeOrderData = new PlaceOrderData()
        placeOrderData.replenishmentRecurrence = B2BReplenishmentRecurrenceEnum.MONTHLY
        placeOrderData.nDaysOfWeek = [DayOfWeek.MONDAY]
        placeOrderData.replenishmentStartDate = new Date()
        placeOrderData.termsCheck = true
        placeOrderData.replenishmentOrder = true
        placeOrderData
    }
}
