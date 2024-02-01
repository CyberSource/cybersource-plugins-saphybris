package isv.sap.payment.addon.b2b.facade.impl.assertions

import java.time.DayOfWeek

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.b2bacceleratorfacades.checkout.data.PlaceOrderData
import de.hybris.platform.b2bacceleratorfacades.exception.EntityValidationException
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BPaymentTypeData
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BReplenishmentRecurrenceEnum
import de.hybris.platform.commercefacades.order.data.CartData
import de.hybris.platform.commercefacades.order.data.DeliveryModeData
import de.hybris.platform.commercefacades.user.data.AddressData
import org.junit.Test
import spock.lang.Specification
import spock.lang.Unroll

import isv.sap.payment.addon.b2b.facade.assertions.OrderAssertions

@UnitTest
class OrderAssertionsSpec extends Specification
{
    @Test
    def 'Should assert that payment type is supported'()
    {
        given:
        def cartData = new CartData()
        def paymentType = new B2BPaymentTypeData()
        paymentType.code = paymentTypeCode
        cartData.paymentType = paymentType

        when:
        OrderAssertions.assertPaymentTypeSupported(cartData)

        then:
        noExceptionThrown()

        where:
        paymentTypeCode | _
        'CARD'          | _
        'ACCOUNT'       | _
    }

    @Test
    def 'Should throw exception when payment type is not supported'()
    {
        given:
        def cartData = new CartData()
        def paymentType = new B2BPaymentTypeData()
        paymentType.code = 'NOT_SUPPORTED'
        cartData.paymentType = paymentType

        when:
        OrderAssertions.assertPaymentTypeSupported(cartData)

        then:
        thrown(EntityValidationException)
    }

    @Test
    def 'Should assert that delivery mode is not empty'()
    {
        given:
        def cartData = new CartData()
        def deliveryMode = new DeliveryModeData()
        cartData.deliveryMode = deliveryMode

        when:
        OrderAssertions.assertDeliveryModeNotEmpty(cartData)

        then:
        noExceptionThrown()
    }

    @Test
    def 'Should throw exception when delivery mode is empty'()
    {
        given:
        def cartData = new CartData()

        when:
        OrderAssertions.assertDeliveryModeNotEmpty(cartData)

        then:
        thrown(EntityValidationException)
    }

    @Test
    def 'Should assert that delivery address is not empty'()
    {
        given:
        def cartData = new CartData()
        def deliveryAddress = new AddressData()
        cartData.deliveryAddress = deliveryAddress

        when:
        OrderAssertions.assertDeliveryAddressNotEmpty(cartData)

        then:
        noExceptionThrown()
    }

    @Test
    def 'Should throw exception when delivery address is empty'()
    {
        given:
        def cartData = new CartData()

        when:
        OrderAssertions.assertDeliveryAddressNotEmpty(cartData)

        then:
        thrown(EntityValidationException)
    }

    @Test
    def 'Should assert that cart is calculated'()
    {
        given:
        def cartData = new CartData()
        cartData.calculated = true

        when:
        OrderAssertions.assertCartCalculated(cartData)

        then:
        noExceptionThrown()
    }

    @Test
    def 'Should throw exception when cart is not calculated'()
    {
        given:
        def cartData = new CartData()

        when:
        OrderAssertions.assertCartCalculated(cartData)

        then:
        thrown(EntityValidationException)
    }

    @Test
    def 'Should assert that order terms are checked'()
    {
        given:
        def placeOrderData = new PlaceOrderData()
        placeOrderData.termsCheck = true

        when:
        OrderAssertions.assertOrderTermsChecked(placeOrderData)

        then:
        noExceptionThrown()
    }

    @Test
    def 'Should throw exception when order terms are not checked'()
    {
        given:
        def placeOrderData = new PlaceOrderData()
        placeOrderData.termsCheck = false

        when:
        OrderAssertions.assertOrderTermsChecked(placeOrderData)

        then:
        thrown(EntityValidationException)
    }

    @Test
    @Unroll
    def 'Should assert that replenishment recurrence is set'()
    {
        given:
        def placeOrderData = new PlaceOrderData()
        placeOrderData.replenishmentRecurrence = recurrenceParam
        placeOrderData.nDaysOfWeek = nDaysOfWeekParam

        when:
        OrderAssertions.assertReplenishmentRecurrenceSet(placeOrderData)

        then:
        noExceptionThrown()

        where:
        nDaysOfWeekParam   | recurrenceParam
        []                 | B2BReplenishmentRecurrenceEnum.MONTHLY
        [DayOfWeek.MONDAY] | B2BReplenishmentRecurrenceEnum.WEEKLY
    }

    @Test
    def 'Should throw exception when replenishment recurrence is not set'()
    {
        given:
        def placeOrderData = new PlaceOrderData()
        placeOrderData.replenishmentRecurrence = B2BReplenishmentRecurrenceEnum.WEEKLY
        placeOrderData.nDaysOfWeek = []

        when:
        OrderAssertions.assertReplenishmentRecurrenceSet(placeOrderData)

        then:
        thrown(EntityValidationException)
    }

    @Test
    def 'Should assert that replenishment start date is not null'()
    {
        given:
        def placeOrderData = new PlaceOrderData()
        placeOrderData.replenishmentStartDate = new Date()

        when:
        OrderAssertions.assertReplenishmentStartDateNotNull(placeOrderData)

        then:
        noExceptionThrown()
    }

    @Test
    def 'Should throw exception when replenishment start date is null'()
    {
        given:
        def placeOrderData = new PlaceOrderData()

        when:
        OrderAssertions.assertReplenishmentStartDateNotNull(placeOrderData)

        then:
        thrown(EntityValidationException)
    }
}
