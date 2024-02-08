package isv.sap.payment.spec.b2c.creditcard

import org.junit.experimental.categories.Category

import isv.sap.payment.pageobject.page.LoginPage
import isv.sap.payment.pageobject.page.OrderConfirmationPage
import isv.sap.payment.pageobject.page.ProductDescriptionPage
import isv.sap.payment.pageobject.page.asm.AsmLoginPage
import isv.sap.payment.pageobject.page.checkout.B2cCheckoutPage
import isv.sap.payment.pageobject.page.hop.SaHopPaymentPage
import isv.sap.payment.spec.IsvGebSpec
import isv.sap.payment.suite.Regression
import isv.sap.payment.suite.Smoke
import isv.sap.payment.suite.category.CreditCard

import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.*
import static isv.sap.payment.data.constants.PaymentConstants.PaymentMethod.CREDIT_CARD
import static isv.sap.payment.data.constants.TransactionStatus.ACCEPT
import static isv.sap.payment.data.constants.TransactionStatus.COMPLETED
import static isv.sap.payment.data.constants.TransactionStatus.DECLINE
import static isv.sap.payment.data.constants.TransactionType.AUTHORIZATION
import static isv.sap.payment.data.constants.TransactionType.CAPTURE

@Category(CreditCard)
class CreditCardHopSpec extends IsvGebSpec
{

    void setupSpec()
    {
        api.setPciStrategyHop()
    }

    void setup()
    {
        useUkSite()
    }

    @Regression
    'should create order for HOP (registered user)'()
    {
        given: 'A cart with product and addresses'
        api.importCart(data)
        to(LoginPage)
                .login(data.email, data.password)

        when: 'User submits the order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectCreditCard()
                .placeOrder()

        and: 'Populates card data in iframe'
        at(SaHopPaymentPage)
                .authorizePayment(type, number, cvv)
                .confirmPayment()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transactions are created'
        api.getTransactionPaymentProvider(orderNumber) == CREDIT_CARD
        api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CAPTURE) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == COMPLETED }

        where: 'Following cards are used'
        type                    | number     | cvv
        HOP_SELECTOR_VISA       | VISA       | PIN_3_DIGITS
        HOP_SELECTOR_MASTERCARD | MASTERCARD | PIN_3_DIGITS
        HOP_SELECTOR_AMEX       | AMEX       | PIN_4_DIGITS
        HOP_SELECTOR_MAESTRO    | MAESTRO    | PIN_3_DIGITS
    }

    @Regression
    'should create order for HOP with 3D secure 2'()
    {
        given: 'A cart with product and addresses'
        api.importCart(data)
        to(LoginPage)
                .login(data.email, data.password)

        when: 'User submits the order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectCreditCard()
                .placeOrder()

        and: 'Populate card data in iframe'
        at(SaHopPaymentPage)
                .authorizePayment(HOP_SELECTOR_VISA, VISA_3DS2_VALID, PIN_3_DIGITS)
                .confirm3DS2Payment()
                .fill3dSecure2()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transactions are created'
        api.getTransactionPaymentProvider(orderNumber) == CREDIT_CARD
        api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CAPTURE) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == COMPLETED }
    }

    @Regression
    'should not create order for HOP with wrong 3D secure 2'()
    {
        given: 'A cart with product and addresses'
        api.importCart(data)
        to(LoginPage)
                .login(data.email, data.password)

        when: 'User places the order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectCreditCard()
                .placeOrder()

        and: 'Populate card data in iframe'
        at(SaHopPaymentPage)
                .authorizePayment(HOP_SELECTOR_VISA, VISA_3DS2_INVALID, PIN_3_DIGITS)
                .confirm3DS2Payment()
                .fill3dSecure2()

        then: 'Order is Not created'
        at(B2cCheckoutPage)
                .globalError.displayed

        and: 'Transaction is Declined'
        waitFor { api.getCartTransactionStatus(data.email) == DECLINE }
    }

    @Regression
    'should create order for HOP (guest)'()
    {
        given: 'Checkout for guest user is started'
        to(ProductDescriptionPage, data.product)
                .addProductToCart()
                .checkoutAsGuest()
                .loginAsGuest(data.email)
                .populateShippingAndBilling(data)

        when: 'I place the order'
        at(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectCreditCard()
                .placeOrder()

        and: 'Populate Card Data'
        at(SaHopPaymentPage)
                .authorizePayment(HOP_SELECTOR_VISA, VISA, PIN_3_DIGITS)
                .confirmPayment()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transactions are created'
        api.getTransactionPaymentProvider(orderNumber) == CREDIT_CARD
        api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CAPTURE) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == COMPLETED }
    }

    @Smoke
    'should complete order in asm'()
    {
        given:
        api.importCart(data)
        to(AsmLoginPage)
                .loginToAsm(credentials.asm)
                .selectUser(data.email)

        when: 'User submits the order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectCreditCard()
                .placeOrder()

        and: 'Populates card data in iframe'
        at(SaHopPaymentPage)
                .authorizePayment(HOP_SELECTOR_VISA, VISA, PIN_3_DIGITS)
                .confirmPayment()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transactions are created'
        api.getTransactionPaymentProvider(orderNumber) == CREDIT_CARD
        api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CAPTURE) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == COMPLETED }
    }

    @Regression
    'should return to checkout when payment cancelled'()
    {
        given: 'A cart with product and addresses'
        api.importCart(data)
        to(LoginPage)
                .login(data.email, data.password)

        when: 'User submits the order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectCreditCard()
                .placeOrder()

        and: 'User cancels payment'
        at(SaHopPaymentPage)
                .cancelPayment()

        then: 'User is returned to checkout'
        at B2cCheckoutPage
    }
}
