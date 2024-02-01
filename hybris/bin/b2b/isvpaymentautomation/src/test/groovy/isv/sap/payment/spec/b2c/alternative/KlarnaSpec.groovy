package isv.sap.payment.spec.b2c.alternative

import org.junit.experimental.categories.Category
import spock.lang.Unroll

import isv.sap.payment.pageobject.page.LoginPage
import isv.sap.payment.pageobject.page.OrderConfirmationPage
import isv.sap.payment.pageobject.page.ProductDescriptionPage
import isv.sap.payment.pageobject.page.alternative.klarna.KlarnaWidgetPage
import isv.sap.payment.pageobject.page.asm.AsmLoginPage
import isv.sap.payment.pageobject.page.checkout.B2cCheckoutPage
import isv.sap.payment.spec.IsvGebSpec
import isv.sap.payment.suite.Regression
import isv.sap.payment.suite.Smoke
import isv.sap.payment.suite.category.Klarna

import static isv.sap.payment.data.constants.PaymentConstants.Klarna.DENIED_EMAIL
import static isv.sap.payment.data.constants.PaymentConstants.Klarna.PAYMENT_NOT_AVAILABLE_EMAIL
import static isv.sap.payment.data.constants.PaymentConstants.Klarna.PENDING_ACCEPT_EMAIL
import static isv.sap.payment.data.constants.PaymentConstants.Klarna.PENDING_REJECT_EMAIL
import static isv.sap.payment.data.constants.PaymentConstants.PaymentMethod.ALTERNATIVE_PAYMENT
import static isv.sap.payment.data.constants.TransactionStatus.ACCEPT
import static isv.sap.payment.data.constants.TransactionStatus.COMPLETED
import static isv.sap.payment.data.constants.TransactionType.AUTHORIZATION
import static isv.sap.payment.data.constants.TransactionType.CAPTURE
import static isv.sap.payment.data.constants.TransactionType.CREATE_SESSION
import static isv.sap.payment.data.constants.TransactionType.UPDATE_SESSION

@Category(Klarna)
class KlarnaSpec extends IsvGebSpec
{
    def setup()
    {
        useUkSite()
    }

    @Regression
    def 'Should place Order as Guest using Klarna'()
    {
        given: 'the checkout is started'
        to(ProductDescriptionPage, data.product)
                .addProductToCart()
                .checkoutAsGuest()
                .loginAsGuest(data.email)

        when: 'User submits Klarna order'
        at(B2cCheckoutPage)
                .populateShippingAndBilling(data)
                .startPayment()
                .paymentMode.selectKlarna()
                .placeOrder()

        and: 'Submits address form on Klarna widget'
        at(KlarnaWidgetPage)
                .submitBillingAddressForm(data)

        then: 'Order is Placed'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transactions are created'
        api.getTransactionPaymentProvider(orderNumber) == ALTERNATIVE_PAYMENT
        api.getTransactionEntryStatus(orderNumber, CREATE_SESSION) == ACCEPT
        api.getTransactionEntryStatus(orderNumber, UPDATE_SESSION) == ACCEPT
        api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CAPTURE) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == COMPLETED }
    }

    @Smoke
    def 'Should place Order as Registered using Klarna'()
    {
        given: 'A cart with product and addresses'
        api.importCart(data)
        to(LoginPage)
                .login(data.email, data.password)

        when: 'User submits Klarna order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectKlarna()
                .placeOrder()

        and: 'Submits address form on Klarna widget'
        at(KlarnaWidgetPage)
                .submitBillingAddressForm(data)

        then: 'Order is Placed'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transactions are created'
        api.getTransactionPaymentProvider(orderNumber) == ALTERNATIVE_PAYMENT
        api.getTransactionEntryStatus(orderNumber, CREATE_SESSION) == ACCEPT
        api.getTransactionEntryStatus(orderNumber, UPDATE_SESSION) == ACCEPT
        api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CAPTURE) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == COMPLETED }
    }

    @Regression
    def 'Should reject cart with user setup to be rejected'()
    {
        given: 'A cart with user setup to be rejected'
        data.email = DENIED_EMAIL
        to(ProductDescriptionPage, data.product)
                .addProductToCart()
                .checkoutAsGuest()
                .loginAsGuest(data.email)

        when: 'User submits Klarna order'
        to(B2cCheckoutPage)
                .populateShippingAndBilling(data)
                .startPayment()
                .paymentMode.selectKlarna()
                .placeOrder()

        and: 'Submits address form with DENIED_EMAIL on Klarna widget'
        at(KlarnaWidgetPage)
                .submitBillingAddressForm(data)

        then: 'Klarna denied message is displayed'
        at(KlarnaWidgetPage)
                .verifyDeniedMessageDisplayed()

        and: 'Error is displayed in checkout page'
        at(B2cCheckoutPage)
                .globalError.displayed
    }

    @Regression
    def 'Should not place Klarna order with PAYMENT_NOT_AVAILABLE_EMAIL'()
    {
        given: 'A cart with user setup to be rejected'
        data.email = PAYMENT_NOT_AVAILABLE_EMAIL
        to(ProductDescriptionPage, data.product)
                .addProductToCart()
                .checkoutAsGuest()
                .loginAsGuest(data.email)

        when: 'User submits place Visa Checkout order'
        to(B2cCheckoutPage)
                .populateShippingAndBilling(data)
                .startPayment()
                .paymentMode.selectKlarna()
                .placeOrder()

        and: 'Submits address form with PAYMENT_NOT_AVAILABLE_EMAIL on Klarna widget'
        at(KlarnaWidgetPage)
                .submitBillingAddressForm(data)

        then: 'User clicks option to change Payment method on Klarna widget'
        at(KlarnaWidgetPage)
                .changePaymentMethod()

        and: 'Error message asking to change payment method is displayed on checkout page'
        at(B2cCheckoutPage)
                .globalError.displayed
    }

    @Regression
    @Unroll
    def 'Should put order authorization in pending for Klarna pending email'()
    {
        given: 'A cart with user setup to be rejected'
        data.email = email
        to(ProductDescriptionPage, data.product)
                .addProductToCart()
                .checkoutAsGuest()
                .loginAsGuest(data.email)

        when: 'User submits place Visa Checkout order'
        to(B2cCheckoutPage)
                .populateShippingAndBilling(data)
                .startPayment()
                .paymentMode.selectKlarna()
                .placeOrder()

        and: 'Submits address form with PENDING email on Klarna widget'
        at(KlarnaWidgetPage)
                .submitBillingAddressForm(data)

        then: 'Order is Placed'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transactions are created'
        api.getTransactionPaymentProvider(orderNumber) == ALTERNATIVE_PAYMENT
        api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT

        and: 'Order is not completed'
        sleep(10000) //Wait some seconds before verifying capture wasn't generated
        api.getTransactionEntryStatus(orderNumber, CAPTURE) == null
        api.getOrderStatus(orderNumber) != COMPLETED

        where:
        email << [PENDING_ACCEPT_EMAIL, PENDING_REJECT_EMAIL]
    }

    @Regression
    def 'should complete order from asm with Klarna'()
    {
        given: 'A cart with product and addresses'
        api.importCart(data)
        to(AsmLoginPage)
                .loginToAsm(credentials.asm)
                .selectUser(data.email)

        when: 'User submits place Visa Checkout order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectKlarna()
                .placeOrder()

        and: 'Submits address form on Klarna widget'
        at(KlarnaWidgetPage)
                .submitBillingAddressForm(data)

        then: 'Order is Placed'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transactions are created'
        api.getTransactionPaymentProvider(orderNumber) == ALTERNATIVE_PAYMENT
        api.getTransactionEntryStatus(orderNumber, CREATE_SESSION) == ACCEPT
        api.getTransactionEntryStatus(orderNumber, UPDATE_SESSION) == ACCEPT
        api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CAPTURE) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == COMPLETED }
    }
}
