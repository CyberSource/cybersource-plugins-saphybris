package isv.sap.payment.spec.b2c.visacheckout

import org.junit.experimental.categories.Category

import isv.sap.payment.data.TestData
import isv.sap.payment.pageobject.page.LoginPage
import isv.sap.payment.pageobject.page.OrderConfirmationPage
import isv.sap.payment.pageobject.page.ProductDescriptionPage
import isv.sap.payment.pageobject.page.asm.AsmLoginPage
import isv.sap.payment.pageobject.page.checkout.B2cCheckoutPage
import isv.sap.payment.pageobject.page.visacheckout.VisaCheckoutPage
import isv.sap.payment.spec.IsvGebSpec
import isv.sap.payment.suite.Regression
import isv.sap.payment.suite.Smoke
import isv.sap.payment.suite.category.VisaCheckout

import static isv.sap.payment.data.constants.PaymentConstants.PaymentMethod.VISA_CHECKOUT
import static isv.sap.payment.data.constants.TransactionStatus.ACCEPT
import static isv.sap.payment.data.constants.TransactionStatus.COMPLETED
import static isv.sap.payment.data.constants.TransactionType.AUTHORIZATION
import static isv.sap.payment.data.constants.TransactionType.CAPTURE

@Category(VisaCheckout)
class VisaCheckoutSpec extends IsvGebSpec
{
    static final String VC_IFRAME_SRC = 'https://sandbox.secure.checkout.visa.com'
    TestData vcoData

    def setup()
    {
        useUkSite()
        vcoData = getData('vco')
    }

    def cleanup()
    {
        browser.clearCookies()
        browser.clearCookies(VC_IFRAME_SRC)
    }

    @Smoke
    'Should place order as guest user using Visa Checkout'()
    {
        given: 'the checkout is started'
        to(ProductDescriptionPage, data.product)
                .addProductToCart()
                .checkoutAsGuest()
                .loginAsGuest(data.email)

        when: 'User submits place Visa Checkout order'
        at(B2cCheckoutPage)
                .populateShippingAndBilling(data)
                .startPayment()
                .paymentMode.selectVisaCheckout()
                .placeOrderVCO()

        and: 'Order is accepted'
        at(VisaCheckoutPage)
                .performVisaCheckout(vcoData)

        then: 'Order is Placed'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transactions are created'
        api.getTransactionPaymentProvider(orderNumber) == VISA_CHECKOUT
        api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CAPTURE) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == COMPLETED }

        and: 'Shipping Address from Order Confirmation and Visa Checkout match'
        String shippingAddress = at(OrderConfirmationPage).extractShippingAddress()
        shippingAddress.contains(vcoData.address)
    }

    @Regression
    'Should place order as registered user using Visa Checkout'()
    {
        given: 'A cart with product and addresses'
        api.importCart(data)
        to(LoginPage)
                .login(data.email, data.password)

        when: 'User submits place Visa Checkout order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectVisaCheckout()
                .placeOrderVCO()

        and: 'Order is accepted'
        at(VisaCheckoutPage)
                .performVisaCheckout(vcoData)

        then: 'Order is Placed'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transactions are created'
        api.getTransactionPaymentProvider(orderNumber) == VISA_CHECKOUT
        api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CAPTURE) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == COMPLETED }
    }

    @Regression
    'Should not create order if Visa Checkout is cancelled'()
    {
        given: 'A cart with product and addresses'
        api.importCart(data)
        to(LoginPage)
                .login(data.email, data.password)

        when: 'User submits place Visa Checkout order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectVisaCheckout()
                .placeOrderVCO()

        and: 'Order is accepted'
        at(VisaCheckoutPage)
                .cancelVisaCheckout()

        then: 'Order is Not Placed'
        at(B2cCheckoutPage)
    }

    @Regression
    'should complete order from asm with Visa Checkout'()
    {
        given: 'A cart with product and addresses'
        api.importCart(data)
        to(AsmLoginPage)
                .loginToAsm(credentials.asm)
                .selectUser(data.email)

        when: 'User submits place Visa Checkout order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectVisaCheckout()
                .placeOrderVCO()

        and: 'Accepts Payment'
        at(VisaCheckoutPage)
                .performVisaCheckout(data)

        then: 'Order is Placed'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transactions are created'
        api.getTransactionPaymentProvider(orderNumber) == VISA_CHECKOUT
        api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CAPTURE) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == COMPLETED }
    }

    @Smoke
    'Should create order for VCO express'()
    {
        given: 'A cart with product and addresses'
        api.importCart(data)
        to(LoginPage)
                .login(data.email, data.password)

        when:
        to(ProductDescriptionPage, data.product)
                .addProductToCart()
                .startVisaCheckout()

        and:
        at(VisaCheckoutPage)
                .performVisaCheckout(data)

        at(B2cCheckoutPage)
                .placeOrder()

        then: 'Order is Placed'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transactions are created'
        api.getTransactionPaymentProvider(orderNumber) == VISA_CHECKOUT
        api.getTransactionEntryStatus(orderNumber, 'AUTHORIZATION') == 'ACCEPT'

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, 'CAPTURE') == 'ACCEPT' }
        waitFor { api.getOrderStatus(orderNumber) == 'COMPLETED' }
    }
}
