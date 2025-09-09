package isv.sap.payment.spec.b2c.alternative

import isv.sap.payment.pageobject.page.alternative.wechatpay.WeChatPaymentPage
import org.junit.experimental.categories.Category

import isv.sap.payment.pageobject.page.LoginPage
import isv.sap.payment.pageobject.page.OrderConfirmationPage
import isv.sap.payment.pageobject.page.ProductDescriptionPage
import isv.sap.payment.pageobject.page.asm.AsmLoginPage
import isv.sap.payment.pageobject.page.checkout.B2cCheckoutPage
import isv.sap.payment.spec.IsvGebSpec
import isv.sap.payment.suite.Regression
import isv.sap.payment.suite.Smoke
import isv.sap.payment.suite.category.b2c.WeChatPay

import static isv.sap.payment.data.constants.PaymentConstants.PaymentMethod.ALTERNATIVE_PAYMENT
import static isv.sap.payment.data.constants.TransactionStatus.ACCEPT
import static isv.sap.payment.data.constants.TransactionType.CHECK_STATUS
import static isv.sap.payment.data.constants.TransactionType.SALE
import static isv.sap.payment.data.constants.TransactionStatus.WAITING_FOR_PAYMENT

@Category(WeChatPay)
class WeChatPaySpec extends IsvGebSpec {
    void setup() {
        useUkSite()
        api.importDefaultCurrency(data)
    }

    @Smoke
    'should create order for registered user'() {
        given: 'A cart with product and addresses'
        api.setPaymentAcceptanceTypeSale()
        api.importCart(data)
        to(LoginPage)
                .login(data.email, data.loginCode)

        when: 'User places WeChatPay order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectWeChatPay()
                .placeOrder()

        and: 'user pays using WeChatPay'
        at(WeChatPaymentPage)
        .acceptPayment()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transaction is created'
        api.getTransactionPaymentProvider(orderNumber, SALE) == ALTERNATIVE_PAYMENT
        api.getTransactionEntryStatus(orderNumber, SALE) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CHECK_STATUS) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == WAITING_FOR_PAYMENT }
    }

    @Regression
    'should create order for guest user'()
    {
        given: 'Checkout for guest user is started'
        api.importDefaultCurrency(data)
        api.setPaymentAcceptanceTypeSale()
        to(ProductDescriptionPage, data.product)
                .addProductToCart()
                .checkoutAsGuest()
                .loginAsGuest(data.email)
                .populateShippingAndBilling(data)

        when: 'User places WeChatPay order'
        to(B2cCheckoutPage)
                .startWithPayment()
                .paymentMode.selectWeChatPay()
                .placeOrder()

        and: 'user pays using WeChatPay'
        sleep(5000)
        at(WeChatPaymentPage)
        .acceptPayment()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transaction is created'
        api.getTransactionPaymentProvider(orderNumber, SALE) == ALTERNATIVE_PAYMENT
        api.getTransactionEntryStatus(orderNumber, SALE) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CHECK_STATUS) == ACCEPT }
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

        when: 'User places WeChatPay order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectWeChatPay()
                .placeOrder()

        and: 'user pays using WeChatPay'
        sleep(5000)
        at(WeChatPaymentPage)
                .acceptPayment()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transaction is created'
        api.getTransactionPaymentProvider(orderNumber, SALE) == ALTERNATIVE_PAYMENT
        api.getTransactionEntryStatus(orderNumber, SALE) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CHECK_STATUS) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == WAITING_FOR_PAYMENT }
    }
}
