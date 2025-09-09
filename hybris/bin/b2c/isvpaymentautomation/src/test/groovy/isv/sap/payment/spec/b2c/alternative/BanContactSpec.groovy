package isv.sap.payment.spec.b2c.alternative

import org.junit.experimental.categories.Category
import spock.lang.Timeout

import isv.sap.payment.pageobject.page.LoginPage
import isv.sap.payment.pageobject.page.OrderConfirmationPage
import isv.sap.payment.pageobject.page.ProductDescriptionPage
import isv.sap.payment.pageobject.page.alternative.mollie.PaymentPage
import isv.sap.payment.pageobject.page.asm.AsmLoginPage
import isv.sap.payment.pageobject.page.checkout.B2cCheckoutPage
import isv.sap.payment.spec.IsvGebSpec
import isv.sap.payment.suite.Regression
import isv.sap.payment.suite.Smoke
import isv.sap.payment.suite.category.b2c.BanContact

import static isv.sap.payment.data.constants.PaymentConstants.PaymentMethod.ALTERNATIVE_PAYMENT
import static isv.sap.payment.data.constants.TransactionStatus.ACCEPT
import static isv.sap.payment.data.constants.TransactionStatus.WAITING_FOR_PAYMENT
import static isv.sap.payment.data.constants.TransactionType.SALE

@Timeout(300)
@Category(BanContact)
class BanContactSpec extends IsvGebSpec
{
    void setup()
    {
        useDeSite()
        api.setPaymentAcceptanceTypeSale()
        api.importDefaultCurrency(data)
    }

    @Smoke
    'should create order for registered user'()
    {
        given: 'A cart with product and addresses'
        api.importCart(data)
        to(LoginPage)
                .login(data.email, data.loginCode)

        when: 'User places bancontact order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectBancontact()
                .placeOrder()

        and: 'user pays using bancontact'
        at(PaymentPage)
                .selectOpenStatus()
                .submitPayment()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transaction is created'
        api.getTransactionPaymentProvider(orderNumber, SALE) == ALTERNATIVE_PAYMENT
        api.getTransactionEntryStatus(orderNumber, SALE) == ACCEPT
        and: 'Order is completed'
        waitFor { api.getOrderStatus(orderNumber) == WAITING_FOR_PAYMENT }
    }

    @Regression
    'should create order from asm'()
    {
        given: 'A cart with product and addresses'
        api.importCart(data)
        to(AsmLoginPage)
                .loginToAsm(credentials.asm)
                .selectUser(data.email)

        when: 'User places bancontact order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectBancontact()
                .placeOrder()

        and: 'user pays using bancontact'
        at(PaymentPage)
                .selectOpenStatus()
                .submitPayment()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transaction is created'
        api.getTransactionPaymentProvider(orderNumber, SALE) == ALTERNATIVE_PAYMENT
        api.getTransactionEntryStatus(orderNumber, SALE) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getOrderStatus(orderNumber) == WAITING_FOR_PAYMENT }
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

        when: 'User places bancontact order'
        at(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectBancontact()
                .placeOrder()

        and: 'user pays using bancontact'
        at(PaymentPage)
                .selectOpenStatus()
                .submitPayment()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transaction is created'
        api.getTransactionPaymentProvider(orderNumber, SALE) == ALTERNATIVE_PAYMENT
        api.getTransactionEntryStatus(orderNumber, SALE) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getOrderStatus(orderNumber) == WAITING_FOR_PAYMENT }
    }

    @Regression
    'should create order for bancontact'()
    {
        given: 'A cart with product and addresses'
        api.importCart(data)
        to(LoginPage)
                .login(data.email, data.loginCode)

        when: 'User places bancontact order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectBancontact()
                .placeOrder()

        and: 'user pays using bancontact'
        at(PaymentPage)
                .selectSuccessStatus()
                .submitPayment()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transaction is created'
        api.getTransactionPaymentProvider(orderNumber, SALE) == ALTERNATIVE_PAYMENT
        api.getTransactionEntryStatus(orderNumber, SALE) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getOrderStatus(orderNumber) == WAITING_FOR_PAYMENT }
    }

    @Regression
    'should not create order if payment canceled'()
    {
        given: 'A cart with product and addresses'
        api.importCart(data)
        to(LoginPage)
                .login(data.email, data.loginCode)

        when: 'User places bancontact order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectBancontact()
                .placeOrder()

        and: 'user pays using bancontact'
        at(PaymentPage)
                .selectCancelledStatus()
                .submitPayment()

        then: 'Order is not created'
        at B2cCheckoutPage
    }

    @Regression
    'should not create order if payment failed'()
    {
        given: 'A cart with product and addresses'
        api.importCart(data)
        to(LoginPage)
                .login(data.email, data.loginCode)

        when: 'User places bancontact order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectBancontact()
                .placeOrder()

        and: 'user pays using bancontact'

        at(PaymentPage)
                .selectFailureStatus()
                .submitPayment()

        then: 'Order is Not created'
        at(B2cCheckoutPage)
                .globalError.displayed
    }

    @Regression
    'should not create order if payment expired'()
    {
        given: 'A cart with product and addresses'
        api.importCart(data)
        to(LoginPage)
                .login(data.email, data.loginCode)

        when: 'User places bancontact order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectBancontact()
                .placeOrder()

        and: 'user pays using bancontact'

        at(PaymentPage)
                .selectExpiredStatus()
                .submitPayment()

        then: 'Order is Not created'
        at(PaymentPage)
                .expiredPaymentStatus()
    }
}