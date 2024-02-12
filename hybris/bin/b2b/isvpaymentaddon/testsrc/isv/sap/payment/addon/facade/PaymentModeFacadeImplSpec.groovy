package isv.sap.payment.addon.facade

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.acceleratorservices.payment.data.PaymentModeData
import de.hybris.platform.core.model.c2l.CountryModel
import de.hybris.platform.core.model.order.CartModel
import de.hybris.platform.core.model.order.payment.PaymentInfoModel
import de.hybris.platform.core.model.user.AddressModel
import de.hybris.platform.order.CartService
import de.hybris.platform.servicelayer.dto.converter.Converter
import de.hybris.platform.store.BaseStoreModel
import de.hybris.platform.store.services.BaseStoreService
import org.junit.Test
import spock.lang.Specification

import isv.sap.payment.enums.AlternativePaymentMethod
import isv.sap.payment.enums.PaymentType
import isv.sap.payment.model.IsvPaymentModeModel

import static java.util.Objects.isNull
import static org.apache.commons.collections.ListUtils.EMPTY_LIST

@UnitTest
class PaymentModeFacadeImplSpec extends Specification
{
    def paymentModeConverter = Mock([useObjenesis: false], Converter)

    def baseStoreService = Mock([useObjenesis: false], BaseStoreService)

    def cartService = Mock([useObjenesis: false], CartService)

    PaymentModeFacade facade = new PaymentModeFacadeImpl()

    def baseStore = Mock([useObjenesis: false], BaseStoreModel)

    IsvPaymentModeModel paymentModeModel1 = new IsvPaymentModeModel()

    IsvPaymentModeModel paymentModeModel2 = new IsvPaymentModeModel()

    PaymentModeData paymentModeData1 = new PaymentModeData()

    PaymentModeData paymentModeData2 = new PaymentModeData()

    CartModel cart = Mock([useObjenesis: false])

    PaymentInfoModel paymentInfo = Mock([useObjenesis: false])

    AddressModel address = Mock([useObjenesis: false])

    CountryModel country = Mock([useObjenesis: false])

    def 'setup'()
    {
        facade.baseStoreService = baseStoreService
        facade.paymentModeConverter = paymentModeConverter
        facade.cartService = cartService
        baseStoreService.currentBaseStore >> baseStore
        paymentModeData1.code = '10_paypal'
        paymentModeData2.code = '1_creditcard'
    }

    @Test
    def 'should return payment modes'()
    {
        when:
        def actual = facade.paymentModes

        then:
        1 * baseStore.allowedIsvPaymentModes >> [paymentModeModel1, paymentModeModel2]
        1 * paymentModeConverter.convert(paymentModeModel1) >> paymentModeData1
        1 * paymentModeConverter.convert(paymentModeModel2) >> paymentModeData2
        actual.size() == 2
        actual.get(0).is(paymentModeData2)
        actual.get(1).is(paymentModeData1)
    }

    @Test
    def 'should return empty list when no payment modes found'()
    {
        when:
        def actual = facade.paymentModes

        then:
        actual == EMPTY_LIST
    }

    @Test
    def 'should replace payment mode prefix with lower sort priority when it is not a number'()
    {
        given:
        def paymentModeModel3 = Mock(IsvPaymentModeModel)
        def paymentModeData3 = new PaymentModeData()
        paymentModeData3.code = 'one_paypal'

        when:
        def actual = facade.paymentModes

        then:
        1 * baseStore.allowedIsvPaymentModes >> [paymentModeModel3, paymentModeModel1, paymentModeModel2]
        1 * paymentModeConverter.convert(paymentModeModel1) >> paymentModeData1
        1 * paymentModeConverter.convert(paymentModeModel2) >> paymentModeData2
        1 * paymentModeConverter.convert(paymentModeModel3) >> paymentModeData3

        and:
        actual.size() == 3
        actual.get(0).is(paymentModeData2)
        actual.get(1).is(paymentModeData1)
        actual.get(2).is(paymentModeData3)
    }

    @Test
    def 'isPaymentModeSupported: should throw error if paymentType isnt provided'()
    {
        when:
        facade.isPaymentModeSupported(null, null)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'Parameter paymentType can not be null'
    }

    @Test
    def 'isPaymentModeSupported: should return false if payment method isnt supported'()
    {
        when:
        baseStore.allowedIsvPaymentModes >> []

        then:
        !facade.isPaymentModeSupported(PaymentType.CREDIT_CARD, null)
    }

    @Test
    def 'isPaymentModeSupported: should return true if paymentType coincide'()
    {
        when:
        baseStore.allowedIsvPaymentModes >> [paymentModeModel1, paymentModeModel2]
        paymentModeModel2.paymentType = PaymentType.CREDIT_CARD

        then:
        facade.isPaymentModeSupported(PaymentType.CREDIT_CARD, null)
    }

    @Test
    def 'isPaymentModeSupported: should return true if paymentType and paymentSubType coincide'()
    {
        when:
        baseStore.allowedIsvPaymentModes >> [paymentModeModel1, paymentModeModel2]
        paymentModeModel2.paymentType = PaymentType.ALTERNATIVE_PAYMENT
        paymentModeModel2.paymentSubType = AlternativePaymentMethod.APY

        then:
        facade.isPaymentModeSupported(PaymentType.ALTERNATIVE_PAYMENT,
                                      AlternativePaymentMethod.APY)
    }

    @Test
    def 'isPaymentModeSupported: should return false if paymentSubType doesnt coincide'()
    {
        when:
        baseStore.allowedIsvPaymentModes >> [paymentModeModel1, paymentModeModel2]
        paymentModeModel2.paymentType = PaymentType.ALTERNATIVE_PAYMENT

        then:
        !facade.isPaymentModeSupported(PaymentType.ALTERNATIVE_PAYMENT,
                                       AlternativePaymentMethod.APY)
    }

    @Test
    def 'getPaymentCountryCode: should return billing address country isocode if available'()
    {
        when:
        cartService.sessionCart >> cart
        cart.paymentInfo >> paymentInfo
        paymentInfo.billingAddress >> address
        address.getCountry() >> country
        country.isocode >> 'GB'

        then:
        facade.paymentCountryCode == 'GB'
    }

    @Test
    def 'getPaymentCountryCode: should return null if billing address country isocode not available'()
    {
        when:
        cartService.sessionCart >> cart
        cart.paymentInfo >> paymentInfo

        then:
        isNull(facade.paymentCountryCode)
    }
}
