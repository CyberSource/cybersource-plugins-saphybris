package isv.sap.payment.spec.b2c.googlepay

import org.junit.experimental.categories.Category
import spock.lang.IgnoreIf

import isv.sap.payment.pageobject.page.LoginPage
import isv.sap.payment.pageobject.page.OrderConfirmationPage
import isv.sap.payment.pageobject.page.ProductDescriptionPage
import isv.sap.payment.pageobject.page.asm.AsmLoginPage
import isv.sap.payment.pageobject.page.checkout.B2cCheckoutPage
import isv.sap.payment.pageobject.page.googlepay.GooglePayPage
import isv.sap.payment.spec.IsvGebSpec
import isv.sap.payment.suite.Regression
import isv.sap.payment.suite.Smoke
import isv.sap.payment.suite.category.GooglePay

import static isv.sap.payment.data.constants.PaymentConstants.PaymentMethod.GOOGLE_PAY
import static isv.sap.payment.data.constants.TransactionStatus.ACCEPT
import static isv.sap.payment.data.constants.TransactionStatus.COMPLETED
import static isv.sap.payment.data.constants.TransactionType.AUTHORIZATION
import static isv.sap.payment.data.constants.TransactionType.CAPTURE

@Category(GooglePay)
@IgnoreIf({ !System.properties['googlepay.username'] })
class GooglePaySpec extends IsvGebSpec
{
    void setup()
    {
        useUkSite()
    }

    @Smoke
    'should create order for registered user'()
    {
        given: 'A cart with product and addresses'
        api.importCart(data)
        to(LoginPage)
                .login(data.email, data.password)

        when: 'User submits GooglePay order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectGooglePay()
                .buyWithGooglePay()

        and: 'Selects google pay'
        withWindow({ title.toUpperCase().contains('GOOGLE') }) {
            at(GooglePayPage)
                    .loginToGoogle(credentials.google)
                    .acceptPayment()
        }

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transactions are created'
        api.getTransactionPaymentProvider(orderNumber) == GOOGLE_PAY
        api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CAPTURE) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == COMPLETED }
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

        when: 'User submits Google Pay order'
        at(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectGooglePay()
                .buyWithGooglePay()

        and: 'Selects google pay'
        withWindow({ title.toUpperCase().contains('GOOGLE') }) {
            at(GooglePayPage)
                    .loginToGoogle(credentials.google)
                    .acceptPayment()
        }

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transactions are created'
        api.getTransactionPaymentProvider(orderNumber) == GOOGLE_PAY
        api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CAPTURE) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == COMPLETED }
    }

    @Regression
    'should complete order from asm'()
    {
        given: 'A cart with product and addresses'
        api.importCart(data)
        to(AsmLoginPage)
                .loginToAsm(credentials.asm)
                .selectUser(data.email)

        when: 'User submits Google Pay order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectGooglePay()
                .buyWithGooglePay()

        and: 'Selects google pay'
        withWindow({ title.toUpperCase().contains('GOOGLE') }) {
            at(GooglePayPage)
                    .loginToGoogle(credentials.google)
                    .acceptPayment()
        }

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transactions are created'
        api.getTransactionPaymentProvider(orderNumber) == GOOGLE_PAY
        api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CAPTURE) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == COMPLETED }
    }
}
