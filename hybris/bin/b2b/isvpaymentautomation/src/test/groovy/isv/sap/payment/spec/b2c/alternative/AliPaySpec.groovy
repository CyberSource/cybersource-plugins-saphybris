package isv.sap.payment.spec.b2c.alternative

import org.junit.experimental.categories.Category

import isv.sap.payment.pageobject.page.LoginPage
import isv.sap.payment.pageobject.page.OrderConfirmationPage
import isv.sap.payment.pageobject.page.ProductDescriptionPage
import isv.sap.payment.pageobject.page.asm.AsmLoginPage
import isv.sap.payment.pageobject.page.checkout.B2cCheckoutPage
import isv.sap.payment.spec.IsvGebSpec
import isv.sap.payment.suite.Regression
import isv.sap.payment.suite.Smoke
import isv.sap.payment.suite.category.AliPay

import static isv.sap.payment.data.constants.PaymentConstants.AliPay.ID_FOR_ABANDONED
import static isv.sap.payment.data.constants.PaymentConstants.AliPay.ID_FOR_COMPLETED
import static isv.sap.payment.data.constants.PaymentConstants.AliPay.ID_FOR_ERROR
import static isv.sap.payment.data.constants.PaymentConstants.AliPay.ID_FOR_PENDING
import static isv.sap.payment.data.constants.PaymentConstants.PaymentMethod.ALTERNATIVE_PAYMENT
import static isv.sap.payment.data.constants.TransactionStatus.ACCEPT
import static isv.sap.payment.data.constants.TransactionStatus.CANCELLED
import static isv.sap.payment.data.constants.TransactionStatus.COMPLETED
import static isv.sap.payment.data.constants.TransactionStatus.REJECT
import static isv.sap.payment.data.constants.TransactionStatus.WAITING_FOR_PAYMENT
import static isv.sap.payment.data.constants.TransactionType.CHECK_STATUS
import static isv.sap.payment.data.constants.TransactionType.INITIATE

@Category(AliPay)
class AliPaySpec extends IsvGebSpec
{

    void setup()
    {
        useDeSite()
    }

    @Smoke
    'should create order for registered user'()
    {
        given: 'A cart with product and addresses'
        api.setAliPayStatus(ID_FOR_COMPLETED)
        api.importCart(data)
        to(LoginPage)
                .login(data.email, data.password)
        to(ProductDescriptionPage, data.product)
                .addProductToCart()
                .checkoutB2B()

        when: 'User submits AliPay order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectAliPay()
                .placeOrder()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transaction is created'
        waitFor { api.getTransactionPaymentProvider(orderNumber, CHECK_STATUS) == ALTERNATIVE_PAYMENT }
        api.getTransactionEntryStatus(orderNumber, INITIATE) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CHECK_STATUS) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == COMPLETED }
    }

    @Regression
    'should create order for guest user'()
    {
        given: 'Checkout for guest user is started'
        api.setAliPayStatus(ID_FOR_COMPLETED)
        to(ProductDescriptionPage, data.product)
                .addProductToCart()
                .checkoutAsGuest()
                .loginAsGuest(data.email)
                .populateShippingAndBilling(data)

        when: 'User submits AliPay order'
        at(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectAliPay()
                .placeOrder()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transaction is created'
        waitFor { api.getTransactionPaymentProvider(orderNumber, CHECK_STATUS) == ALTERNATIVE_PAYMENT }
        api.getTransactionEntryStatus(orderNumber, INITIATE) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CHECK_STATUS) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == COMPLETED }
    }

    @Regression
    'should create order from asm'()
    {
        given: 'A cart with product and addresses'
        api.setAliPayStatus(ID_FOR_COMPLETED)
        api.importCart(data)
        to(AsmLoginPage)
                .loginToAsm(credentials.asm)
                .selectUser(data.email)

        when: 'User submits AliPay order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectAliPay()
                .placeOrder()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transaction is created'
        waitFor { api.getTransactionPaymentProvider(orderNumber, CHECK_STATUS) == ALTERNATIVE_PAYMENT }
        api.getTransactionEntryStatus(orderNumber, INITIATE) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CHECK_STATUS) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == COMPLETED }
    }

    @Regression
    'should not complete order for error status from AliPay'()
    {
        given: 'A cart with product and addresses'
        api.setAliPayStatus(ID_FOR_ERROR)
        api.importCart(data)
        to(LoginPage)
                .login(data.email, data.password)

        when: 'User submits AliPay order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectAliPay()
                .placeOrder()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transaction is created'
        waitFor { api.getTransactionPaymentProvider(orderNumber, CHECK_STATUS) == ALTERNATIVE_PAYMENT }
        api.getTransactionEntryStatus(orderNumber, INITIATE) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CHECK_STATUS) == REJECT }
        waitFor { api.getOrderStatus(orderNumber) == CANCELLED }
    }

    @Regression
    'should not complete order for abandoned status from AliPay'()
    {
        given: 'A cart with product and addresses'
        api.setAliPayStatus(ID_FOR_ABANDONED)
        api.importCart(data)
        to(LoginPage)
                .login(data.email, data.password)

        when: 'User submits AliPay order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectAliPay()
                .placeOrder()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transaction is created'
        waitFor { api.getTransactionPaymentProvider(orderNumber, CHECK_STATUS) == ALTERNATIVE_PAYMENT }
        api.getTransactionEntryStatus(orderNumber, INITIATE) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CHECK_STATUS) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == CANCELLED }
    }

    @Regression
    'should not complete order for pending status from AliPay'()
    {
        given: 'A cart with product and addresses'
        api.setAliPayStatus(ID_FOR_PENDING)
        api.importCart(data)
        to(LoginPage)
                .login(data.email, data.password)

        when: 'User submits AliPay order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectAliPay()
                .placeOrder()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transaction is created'
        waitFor { api.getTransactionPaymentProvider(orderNumber, CHECK_STATUS) == ALTERNATIVE_PAYMENT }
        api.getTransactionEntryStatus(orderNumber, INITIATE) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CHECK_STATUS) == ACCEPT }
        api.getOrderStatus(orderNumber) == WAITING_FOR_PAYMENT
    }
}
