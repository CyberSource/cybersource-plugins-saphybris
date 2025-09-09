package isv.sap.payment.spec.b2c.alternative

import isv.sap.payment.data.TestData
import org.junit.experimental.categories.Category

import isv.sap.payment.pageobject.page.LoginPage
import isv.sap.payment.pageobject.page.OrderConfirmationPage
import isv.sap.payment.pageobject.page.ProductDescriptionPage
import isv.sap.payment.pageobject.page.alternative.klarna.KlarnaWidgetPage
import isv.sap.payment.pageobject.page.asm.AsmLoginPage
import isv.sap.payment.pageobject.page.checkout.B2cCheckoutPage
import isv.sap.payment.spec.IsvGebSpec
import isv.sap.payment.suite.Regression
import isv.sap.payment.suite.Smoke
import isv.sap.payment.suite.category.b2c.Klarna

import static isv.sap.payment.data.constants.PaymentConstants.PaymentMethod.ALTERNATIVE_PAYMENT
import static isv.sap.payment.data.constants.TransactionStatus.ACCEPT
import static isv.sap.payment.data.constants.TransactionStatus.WAITING_FOR_PAYMENT
import static isv.sap.payment.data.constants.TransactionType.AUTHORIZATION
import static isv.sap.payment.data.constants.TransactionType.CREATE_SESSION
import static isv.sap.payment.data.constants.TransactionType.UPDATE_SESSION

@Category(Klarna)
class KlarnaSpec extends IsvGebSpec
{

    TestData data
    void setup()
    {
        useUkSite()
        data = getData('klarna')
        api.importDefaultCurrency(data)
    }
    @Regression
    def 'Should place Order as Guest using Klarna'()
    {
        given: 'the checkout is started'
        api.setPaymentAcceptanceTypeAuth()
        to(ProductDescriptionPage, data.product)
                .addProductToCart()
                .checkoutAsGuest()
                .loginAsGuest(data.email)
                .populateShippingAndBilling(data)

        when: 'User submits Klarna order'
        at(B2cCheckoutPage)
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
        waitFor { api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == WAITING_FOR_PAYMENT }
    }

   @Smoke
    def 'Should place Order as Registered using Klarna'()
    {
        given: 'A cart with product and addresses'
        api.setPaymentAcceptanceTypeAuth()
        api.importCustomer(data)
        to(LoginPage)
                .login(data.email, data.loginCode)
        to(ProductDescriptionPage, data.product)
                .addProductToCart()

        to(B2cCheckoutPage)
                .populateShippingAndBilling(data)

        when: 'User submits Klarna order'
        to(B2cCheckoutPage)
                .startWithPayment()
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
        waitFor { api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == WAITING_FOR_PAYMENT }
    }

    @Regression
    def 'should complete order from asm with Klarna'()
    {
        given: 'A cart with product and addresses'
        api.setPaymentAcceptanceTypeAuth()
        api.importCustomer(data)
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
        waitFor { api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == WAITING_FOR_PAYMENT }
    }
}
