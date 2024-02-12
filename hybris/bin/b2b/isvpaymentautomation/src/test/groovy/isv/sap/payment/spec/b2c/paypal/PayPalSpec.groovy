package isv.sap.payment.spec.b2c.paypal

import org.junit.experimental.categories.Category

import isv.sap.payment.pageobject.page.LoginPage
import isv.sap.payment.pageobject.page.OrderConfirmationPage
import isv.sap.payment.pageobject.page.ProductDescriptionPage
import isv.sap.payment.pageobject.page.asm.AsmLoginPage
import isv.sap.payment.pageobject.page.checkout.B2cCheckoutPage
import isv.sap.payment.pageobject.page.paypal.PayPalPage
import isv.sap.payment.spec.IsvGebSpec
import isv.sap.payment.suite.Regression
import isv.sap.payment.suite.Smoke
import isv.sap.payment.suite.category.PayPal

import static isv.sap.payment.data.constants.PaymentConstants.PaymentMethod.PAY_PAL
import static isv.sap.payment.data.constants.TransactionStatus.ACCEPT
import static isv.sap.payment.data.constants.TransactionStatus.AUTHORIZED
import static isv.sap.payment.data.constants.TransactionStatus.PENDING
import static isv.sap.payment.data.constants.TransactionStatus.WAITING_FOR_PAYMENT
import static isv.sap.payment.data.constants.TransactionType.AUTHORIZATION
import static isv.sap.payment.data.constants.TransactionType.CAPTURE
import static isv.sap.payment.data.constants.TransactionType.CHECK_STATUS
import static isv.sap.payment.data.constants.TransactionType.CREATE_SESSION
import static isv.sap.payment.data.constants.TransactionType.ORDER_SETUP

@Category(PayPal)
class PayPalSpec extends IsvGebSpec
{
    void setup()
    {
        useUkSite()
    }

    @Regression
    'should create order for guest user'()
    {
        given: 'Checkout for guest user is started'
        to(ProductDescriptionPage, data.product)
                .addProductToCart()
                .checkoutAsGuest()
                .loginAsGuest(data.email)
                .populateShippingAndBilling(data)

        when: 'User submits PayPal order'
        at(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectPayPal()
                .placeOrder()

        and: 'Accepts Payment'
        at(PayPalPage)
                .loginToPayPal(credentials.paypal)
                .acceptPayment()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transactions are created'
        api.getTransactionPaymentProvider(orderNumber) == PAY_PAL
        api.getTransactionEntryStatus(orderNumber, CREATE_SESSION) == ACCEPT
        api.getTransactionEntryStatus(orderNumber, CHECK_STATUS) == ACCEPT
        api.getTransactionEntryStatus(orderNumber, ORDER_SETUP) == ACCEPT
        api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT
        api.getTransactionEntryPaymentStatus(data.baseStore, orderNumber, AUTHORIZATION) == AUTHORIZED

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CAPTURE) == ACCEPT }
        api.getTransactionEntryPaymentStatus(data.baseStore, orderNumber, CAPTURE) == PENDING
        api.getOrderStatus(orderNumber) == WAITING_FOR_PAYMENT
    }

    @Smoke
    'should create order for registered user'()
    {
        given: 'A cart with product and addresses'
        api.importCart(data)
        to(LoginPage)
                .login(data.email, data.password)

        when: 'User submits PayPal order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectPayPal()
                .placeOrder()

        and: 'Accepts Payment'
        at(PayPalPage)
                .loginToPayPal(credentials.paypal)
                .acceptPayment()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transactions are created'
        api.getTransactionPaymentProvider(orderNumber) == PAY_PAL
        api.getTransactionEntryStatus(orderNumber, CREATE_SESSION) == ACCEPT
        api.getTransactionEntryStatus(orderNumber, CHECK_STATUS) == ACCEPT
        api.getTransactionEntryStatus(orderNumber, ORDER_SETUP) == ACCEPT
        api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT
        api.getTransactionEntryPaymentStatus(data.baseStore, orderNumber, AUTHORIZATION) == AUTHORIZED

        and: 'Capture is Pending'
        waitFor { api.getTransactionEntryStatus(orderNumber, CAPTURE) == ACCEPT }
        api.getTransactionEntryPaymentStatus(data.baseStore, orderNumber, CAPTURE) == PENDING
        api.getOrderStatus(orderNumber) == WAITING_FOR_PAYMENT
    }

    @Regression
    'should not create order if PayPal canceled'()
    {
        given: 'A cart with product and addresses'
        api.importCart(data)
        to(LoginPage)
                .login(data.email, data.password)

        when: 'User submits PayPal order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectPayPal()
                .placeOrder()

        and: 'Logs in to PayPal but rejects payment'
        at(PayPalPage)
                .loginToPayPal(credentials.paypal)
                .cancelPayment()

        then: 'User is returned to Checkout Page'
        at(B2cCheckoutPage)
    }

    @Regression
    'should complete order from asm'()
    {
        given: 'A cart with product and addresses'
        api.importCart(data)
        to(AsmLoginPage)
                .loginToAsm(credentials.asm)
                .selectUser(data.email)

        when: 'User submits PayPal order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectPayPal()
                .placeOrder()

        and: 'Accepts Payment'
        at(PayPalPage)
                .loginToPayPal(credentials.paypal)
                .acceptPayment()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transactions are created'
        api.getTransactionPaymentProvider(orderNumber) == PAY_PAL
        api.getTransactionEntryStatus(orderNumber, CREATE_SESSION) == ACCEPT
        api.getTransactionEntryStatus(orderNumber, CHECK_STATUS) == ACCEPT
        api.getTransactionEntryStatus(orderNumber, ORDER_SETUP) == ACCEPT
        api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT
        api.getTransactionEntryPaymentStatus(data.baseStore, orderNumber, AUTHORIZATION) == AUTHORIZED

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CAPTURE) == ACCEPT }
        api.getTransactionEntryPaymentStatus(data.baseStore, orderNumber, CAPTURE) == PENDING
        api.getOrderStatus(orderNumber) == WAITING_FOR_PAYMENT
    }
}
