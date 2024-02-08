package isv.sap.payment.spec.b2c.alternative

import org.junit.experimental.categories.Category
import spock.lang.Timeout

import isv.sap.payment.pageobject.page.LoginPage
import isv.sap.payment.pageobject.page.OrderConfirmationPage
import isv.sap.payment.pageobject.page.ProductDescriptionPage
import isv.sap.payment.pageobject.page.alternative.mollie.PaymentPage
import isv.sap.payment.pageobject.page.alternative.mollie.SelectBankPage
import isv.sap.payment.pageobject.page.asm.AsmLoginPage
import isv.sap.payment.pageobject.page.checkout.B2cCheckoutPage
import isv.sap.payment.spec.IsvGebSpec
import isv.sap.payment.suite.Regression
import isv.sap.payment.suite.Smoke
import isv.sap.payment.suite.category.Ideal

import static isv.sap.payment.data.constants.PaymentConstants.PaymentMethod.ALTERNATIVE_PAYMENT
import static isv.sap.payment.data.constants.TransactionStatus.ACCEPT
import static isv.sap.payment.data.constants.TransactionStatus.COMPLETED
import static isv.sap.payment.data.constants.TransactionStatus.WAITING_FOR_PAYMENT
import static isv.sap.payment.data.constants.TransactionType.CHECK_STATUS
import static isv.sap.payment.data.constants.TransactionType.SALE

@Timeout(300)
@Category(Ideal)
class IdealSpec extends IsvGebSpec
{

    void setup()
    {
        useDeSite()
    }

    @Smoke
    'should create order for registered user'()
    {
        given: 'A cart with product and addresses'
        api.importCart(data)
        to(LoginPage)
                .login(data.email, data.password)

        when: 'User places ideal order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectIdeal()
                .placeOrder()

        and: 'user pays using ideal'
        at(SelectBankPage)
                .selectINGBank()

        at(PaymentPage)
                .selectSuccessStatus()
                .submitPayment()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transaction is created'
        api.getTransactionPaymentProvider(orderNumber, SALE) == ALTERNATIVE_PAYMENT
        api.getTransactionEntryStatus(orderNumber, SALE) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CHECK_STATUS) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == COMPLETED }
    }

    @Regression
    'should create order from asm'()
    {
        given: 'A cart with product and addresses'
        api.importCart(data)
        to(AsmLoginPage)
                .loginToAsm(credentials.asm)
                .selectUser(data.email)

        when: 'User places ideal order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectIdeal()
                .placeOrder()

        and: 'user pays using ideal'
        at(SelectBankPage)
                .selectINGBank()
                .selectSuccessStatus()
                .submitPayment()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transaction is created'
        api.getTransactionPaymentProvider(orderNumber, SALE) == ALTERNATIVE_PAYMENT
        api.getTransactionEntryStatus(orderNumber, SALE) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CHECK_STATUS) == ACCEPT }
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

        when: 'User places ideal order'
        at(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectIdeal()
                .placeOrder()

        and: 'user pays using ideal'
        at(SelectBankPage)
                .selectINGBank()
                .selectSuccessStatus()
                .submitPayment()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transaction is created'
        api.getTransactionPaymentProvider(orderNumber, SALE) == ALTERNATIVE_PAYMENT
        api.getTransactionEntryStatus(orderNumber, SALE) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CHECK_STATUS) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == COMPLETED }
    }

    @Regression
    'should create order for bancontact'()
    {
        given: 'A cart with product and addresses'
        api.importCart(data)
        to(LoginPage)
                .login(data.email, data.password)

        when: 'User places bancontact order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectBancontact()
                .placeOrder()

        and: 'user pays using ideal'
        at(SelectBankPage)
                .selectINGBank()

        at(PaymentPage)
                .selectSuccessStatus()
                .submitPayment()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transaction is created'
        api.getTransactionPaymentProvider(orderNumber, SALE) == ALTERNATIVE_PAYMENT
        api.getTransactionEntryStatus(orderNumber, SALE) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CHECK_STATUS) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == COMPLETED }
    }

    @Regression
    'should not creat order if payment canceled'()
    {
        given: 'A cart with product and addresses'
        api.importCart(data)
        to(LoginPage)
                .login(data.email, data.password)

        when: 'User places ideal order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectIdeal()
                .placeOrder()

        and: 'user pays using ideal'
        at(SelectBankPage)
                .selectINGBank()

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
                .login(data.email, data.password)

        when: 'User places ideal order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectIdeal()
                .placeOrder()

        and: 'user pays using ideal'
        at(SelectBankPage)
                .selectINGBank()

        at(PaymentPage)
                .selectFailureStatus()
                .submitPayment()

        then: 'Order is Not created'
        at(B2cCheckoutPage)
                .globalError.displayed
    }

    @Regression
    'should not complete order if payment pending'()
    {
        given: 'A cart with product and addresses'
        api.importCart(data)
        to(LoginPage)
                .login(data.email, data.password)

        when: 'User places ideal order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectIdeal()
                .placeOrder()

        and: 'user pays using ideal'
        at(SelectBankPage)
                .selectINGBank()

        at(PaymentPage)
                .selectOpenStatus()
                .submitPayment()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transaction is created'
        api.getTransactionPaymentProvider(orderNumber, SALE) == ALTERNATIVE_PAYMENT
        api.getTransactionEntryStatus(orderNumber, SALE) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CHECK_STATUS) == ACCEPT }
        api.getOrderStatus(orderNumber) == WAITING_FOR_PAYMENT
    }
}
