package isv.sap.payment.spec.b2b.paypal

import org.junit.experimental.categories.Category

import isv.sap.payment.pageobject.page.LoginPage
import isv.sap.payment.pageobject.page.OrderConfirmationPage
import isv.sap.payment.pageobject.page.ProductDescriptionPage
import isv.sap.payment.pageobject.page.asm.AsmLoginPage
import isv.sap.payment.pageobject.page.checkout.B2bCheckoutPage
import isv.sap.payment.pageobject.page.paypal.PayPalPage
import isv.sap.payment.spec.IsvGebSpec
import isv.sap.payment.suite.Regression
import isv.sap.payment.suite.Smoke
import isv.sap.payment.suite.category.PayPal

import static isv.sap.payment.data.constants.PaymentConstants.PaymentMethod.PAY_PAL
import static isv.sap.payment.data.constants.TransactionStatus.ACCEPT
import static isv.sap.payment.data.constants.TransactionStatus.AUTHORIZED
import static isv.sap.payment.data.constants.TransactionStatus.COMPLETED
import static isv.sap.payment.data.constants.TransactionStatus.SETTLED
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
        useB2bSite()
    }

    @Smoke
    'should create order'()
    {
        given: 'The checkout is started'
        to(LoginPage)
                .login(data.email, data.password)
        to(ProductDescriptionPage, data.product)
                .addProductToCart()
                .checkoutB2B()
                .proceedToPayPalPayment()
                .populateShippingAndBilling(data)

        when: 'I pay with PayPal'
        at(B2bCheckoutPage)
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
        api.getTransactionEntryPaymentStatus(data.baseStore, orderNumber, CAPTURE) == SETTLED
        waitFor { api.getOrderStatus(orderNumber) == COMPLETED }
    }

    @Regression
    'should create order for asm'()
    {
        given: 'The checkout is started with ASM'
        to(AsmLoginPage)
                .loginToAsm(credentials.asm)
                .selectUser(data.email)
        to(ProductDescriptionPage, data.product)
                .addProductToCart()
                .checkoutB2B()
                .proceedToPayPalPayment()
                .populateShippingAndBilling(data)

        when: 'I pay with credit card'
        at(B2bCheckoutPage)
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
        api.getTransactionEntryPaymentStatus(data.baseStore, orderNumber, CAPTURE) == SETTLED
        waitFor { api.getOrderStatus(orderNumber) == COMPLETED }
    }

    @Regression
    'should not create order if paypal canceled'()
    {
        given: 'The checkout is started'
        to(LoginPage)
                .login(data.email, data.password)
        to(ProductDescriptionPage, data.product)
                .addProductToCart()
                .checkoutB2B()
                .proceedToPayPalPayment()
                .populateShippingAndBilling(data)

        when: 'I pay with credit card'
        at(B2bCheckoutPage)
                .placeOrder()

        and: 'Accepts Payment'
        at(PayPalPage)
                .loginToPayPal(credentials.paypal)
                .cancelPayment()

        then: 'User is returned to Checkout Page'
        at(B2bCheckoutPage)
    }
}
