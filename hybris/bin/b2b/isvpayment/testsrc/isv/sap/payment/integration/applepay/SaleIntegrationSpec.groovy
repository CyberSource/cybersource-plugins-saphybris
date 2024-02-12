package isv.sap.payment.integration.applepay

import de.hybris.bootstrap.annotations.ManualTest
import de.hybris.platform.payment.enums.PaymentTransactionType
import org.junit.Test

import isv.cjl.payment.enums.CardType
import isv.cjl.payment.enums.DecryptionType
import isv.cjl.payment.service.executor.request.builder.applepay.SaleRequestBuilder
import isv.sap.payment.integration.helpers.IsvIntegrationSpec

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER
import static isv.cjl.payment.enums.PaymentType.APPLE_PAY

@ManualTest
class SaleIntegrationSpec extends IsvIntegrationSpec
{
    def builder = new SaleRequestBuilder()

    @Test
    "Should receive reject for isv decryption with fake payment token"()
    {
        given:
        def order = testOrderUk()
        def fakeToken = applePayTransactionsCreator.encryptedFakeToken()

        def operationStartTime = new Date()

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .setPaymentToken(fakeToken)
                .addParam(ORDER, order)
                .setDecryptionType(DecryptionType.ISV_PAYMENT)
                .setCardType(CardType.VISA)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == APPLE_PAY.name()
            type == PaymentTransactionType.SALE
            transactionStatus == 'REJECT'
            transactionStatusDetails == '102'

            amount == null
            code.toString().contains(order.code)
            requestId != null
            currency == null

            time > operationStartTime
            time <= new Date()

            with(properties) {
                invalidField == '[c:encryptedPayment/c:data]'
                missingField == '[]'

                requestID != null
                requestToken != null

                reasonCode == '102'
                decision == 'REJECT'
            }
        }
    }

    @Test
    "Should receive accept for merchant decryption"()
    {
        given:
        def order = testOrderUk()
        def card = testCard()
        def decryptedPaymentToken = applePayTransactionsCreator.decryptedToken(card.cardNumber, order.totalPrice)

        when:
        def request = builder
                .setMerchantId(testConfig.merchant)
                .setPaymentToken(decryptedPaymentToken)
                .addParam(ORDER, order)
                .setDecryptionType(DecryptionType.MERCHANT)
                .setCardType(CardType.VISA)
                .build()

        def result = paymentServiceExecutor.execute(request)

        then:
        with(result.getData('transaction')) {
            paymentTransaction.paymentProvider == APPLE_PAY.name()
            type == PaymentTransactionType.SALE
            transactionStatus == 'ACCEPT'
            transactionStatusDetails == '100'

            code.toString().contains(order.code)
            requestId != null

            with(properties) {
                invalidField == '[]'
                missingField == '[]'

                requestID != null
                requestToken != null

                reasonCode == '100'
                decision == 'ACCEPT'
            }
        }
    }
}

