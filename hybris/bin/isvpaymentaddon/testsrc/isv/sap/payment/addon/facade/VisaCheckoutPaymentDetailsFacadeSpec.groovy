package isv.sap.payment.addon.facade

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.CartModel
import de.hybris.platform.order.CartService
import de.hybris.platform.payment.model.PaymentTransactionModel
import de.hybris.platform.servicelayer.dto.converter.Converter
import org.junit.Test
import spock.lang.Specification

import isv.sap.payment.addon.VisaCheckoutPaymentDetailsData
import isv.sap.payment.model.IsvPaymentTransactionEntryModel
import isv.sap.payment.service.PaymentTransactionService

import static de.hybris.platform.payment.enums.PaymentTransactionType.GET
import static isv.sap.payment.enums.PaymentType.VISA_CHECKOUT
import static java.util.Optional.empty

@UnitTest
class VisaCheckoutPaymentDetailsFacadeSpec extends Specification
{
    def paymentDetailsConverter = Mock(Converter)
    def cartService = Mock(CartService)
    def paymentTransactionService = Mock(PaymentTransactionService)
    def cart = Mock(CartModel)
    def vcTransaction = Mock(PaymentTransactionModel)
    def getTransactionEntry = Mock(IsvPaymentTransactionEntryModel)
    def vcPaymentDetailsData = Mock(VisaCheckoutPaymentDetailsData)

    def facade =
            new VisaCheckoutPaymentDetailsFacadeImpl(paymentDetailsConverter: paymentDetailsConverter,
                                                     cartService: cartService,
                                                     paymentTransactionService: paymentTransactionService)

    @Test
    def 'should return visa checkout payment details data'()
    {
        when:
        def actual = facade.VCPaymentDetails

        then:
        1 * cartService.sessionCart >> cart
        1 * paymentTransactionService.getLatestTransaction(VISA_CHECKOUT, cart) >> Optional.of(vcTransaction)
        1 * paymentTransactionService.getLatestAcceptedTransactionEntry(vcTransaction, GET) >> Optional.of(getTransactionEntry)
        1 * paymentDetailsConverter.convert(getTransactionEntry) >> vcPaymentDetailsData
        actual == Optional.of(vcPaymentDetailsData)
    }

    @Test
    def 'should not return visa checkout payment details as there is no VISA_CHECKOUT transaction'()
    {
        when:
        def actual = facade.VCPaymentDetails

        then:
        1 * cartService.sessionCart >> cart
        1 * paymentTransactionService.getLatestTransaction(VISA_CHECKOUT, cart) >> empty()
        0 * paymentDetailsConverter.convert(_)
        actual == empty()
    }

    @Test
    def 'should not return visa checkout payment details as there is no VISA_CHECKOUT GET transaction entry'()
    {
        when:
        def actual = facade.VCPaymentDetails

        then:
        1 * cartService.sessionCart >> cart
        1 * paymentTransactionService.getLatestTransaction(VISA_CHECKOUT, cart) >> Optional.of(vcTransaction)
        1 * paymentTransactionService.getLatestAcceptedTransactionEntry(vcTransaction, GET) >> empty()
        0 * paymentDetailsConverter.convert(_)
        actual == empty()
    }
}
