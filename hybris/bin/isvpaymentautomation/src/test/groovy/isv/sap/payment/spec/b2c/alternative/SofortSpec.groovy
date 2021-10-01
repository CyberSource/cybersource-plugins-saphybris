package isv.sap.payment.spec.b2c.alternative

import org.junit.experimental.categories.Category

import isv.sap.payment.pageobject.page.LoginPage
import isv.sap.payment.pageobject.page.OrderConfirmationPage
import isv.sap.payment.pageobject.page.ProductDescriptionPage
import isv.sap.payment.pageobject.page.alternative.sofort.SelectCountryPage
import isv.sap.payment.pageobject.page.asm.AsmLoginPage
import isv.sap.payment.pageobject.page.checkout.B2cCheckoutPage
import isv.sap.payment.spec.IsvGebSpec
import isv.sap.payment.suite.Regression
import isv.sap.payment.suite.Smoke
import isv.sap.payment.suite.category.Sofort

import static isv.sap.payment.data.constants.PaymentConstants.PaymentMethod.ALTERNATIVE_PAYMENT
import static isv.sap.payment.data.constants.PaymentConstants.Sofort.ACCOUNT_NUMBER
import static isv.sap.payment.data.constants.PaymentConstants.Sofort.BIC
import static isv.sap.payment.data.constants.PaymentConstants.Sofort.PIN
import static isv.sap.payment.data.constants.PaymentConstants.Sofort.TAN
import static isv.sap.payment.data.constants.TransactionStatus.ACCEPT
import static isv.sap.payment.data.constants.TransactionStatus.COMPLETED
import static isv.sap.payment.data.constants.TransactionType.CHECK_STATUS
import static isv.sap.payment.data.constants.TransactionType.SALE

@Category(Sofort)
class SofortSpec extends IsvGebSpec
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

        when: 'User places Sofort order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectSofort()
                .placeOrder()

        and: 'user pays using Sofort'
        sleep(5000) // Wait for redirect to get to select country page
        at(SelectCountryPage)
                .inputBic(BIC)
                .loginToSofort(ACCOUNT_NUMBER, PIN)
                .selectAccount()
                .provideTan(TAN)

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

        when: 'User places Sofort order'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectSofort()
                .placeOrder()

        and: 'user pays using Sofort'
        sleep(5000) // Wait for redirect to get to select country page
        at(SelectCountryPage)
                .inputBic(BIC)
                .loginToSofort(ACCOUNT_NUMBER, PIN)
                .selectAccount()
                .provideTan(TAN)

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

        when: 'User places Sofort order'
        at(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectSofort()
                .placeOrder()

        and: 'user pays using Sofort'
        sleep(5000) // Wait for redirect to get to select country page
        at(SelectCountryPage)
                .inputBic(BIC)
                .loginToSofort(ACCOUNT_NUMBER, PIN)
                .selectAccount()
                .provideTan(TAN)

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()
        and: 'Transaction is created'
        api.getTransactionPaymentProvider(orderNumber, SALE) == ALTERNATIVE_PAYMENT
        api.getTransactionEntryStatus(orderNumber, SALE) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CHECK_STATUS) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == COMPLETED }
    }
}
