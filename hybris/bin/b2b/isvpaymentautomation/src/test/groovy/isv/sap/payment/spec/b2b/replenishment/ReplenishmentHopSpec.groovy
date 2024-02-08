package isv.sap.payment.spec.b2b.replenishment

import org.junit.experimental.categories.Category

import isv.sap.payment.pageobject.page.LoginPage
import isv.sap.payment.pageobject.page.ProductDescriptionPage
import isv.sap.payment.pageobject.page.ReplenishmentConfirmationPage
import isv.sap.payment.pageobject.page.asm.AsmLoginPage
import isv.sap.payment.pageobject.page.checkout.B2bCheckoutPage
import isv.sap.payment.pageobject.page.hop.SaHopPaymentPage
import isv.sap.payment.spec.IsvGebSpec
import isv.sap.payment.suite.Regression
import isv.sap.payment.suite.Smoke
import isv.sap.payment.suite.category.CreditCard

import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.HOP_SELECTOR_VISA
import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.PIN_3_DIGITS
import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.VISA
import static isv.sap.payment.data.constants.PaymentConstants.PaymentMethod.CREDIT_CARD
import static isv.sap.payment.data.constants.TransactionStatus.ACCEPT
import static isv.sap.payment.data.constants.TransactionStatus.COMPLETED
import static isv.sap.payment.data.constants.TransactionType.AUTHORIZATION
import static isv.sap.payment.data.constants.TransactionType.CAPTURE

@Category(CreditCard)
class ReplenishmentHopSpec extends IsvGebSpec
{
    void setupSpec()
    {
        api.setPciStrategyHop()
    }

    void setup()
    {
        useB2bSite()
    }

    @Smoke
    'should create order for HOP'()
    {
        given: 'The checkout is started'
        to(LoginPage)
                .login(data.email, data.password)
        to(ProductDescriptionPage, data.product)
                .addProductToCart()
                .checkoutB2B()
                .proceedToCardPayment()
                .populateShippingAndBilling(data)

        when: 'I pay schedule Replenishment'
        at(B2bCheckoutPage)
                .startReplenishment()
                .scheduleReplenishment()

        and: 'Populate Card Data'
        at(SaHopPaymentPage)
                .authorizeSubscription(HOP_SELECTOR_VISA, VISA, PIN_3_DIGITS)

        then: 'Order is Planned'
        String cronJobNumber = at(ReplenishmentConfirmationPage).extractCronJobNumber()

        and: 'Order is created'
        waitFor { api.getOrderNumber(cronJobNumber) != null }
        String orderNumber = api.getOrderNumber(cronJobNumber)

        and: 'Transactions are created'
        api.getTransactionPaymentProvider(orderNumber) == CREDIT_CARD
        api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CAPTURE) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == COMPLETED }
    }

    @Regression
    'should create order for HOP from ASM'()
    {
        given: 'The checkout is started'
        to(AsmLoginPage)
                .loginToAsm(credentials.asm)
                .selectUser(data.email)
        to(ProductDescriptionPage, data.product)
                .addProductToCart()
                .checkoutB2B()
                .proceedToCardPayment()
                .populateShippingAndBilling(data)

        when: 'I pay schedule Replenishment'
        at(B2bCheckoutPage)
                .startReplenishment()
                .scheduleReplenishment()

        and: 'Populate Card Data'
        at(SaHopPaymentPage)
                .authorizeSubscription(HOP_SELECTOR_VISA, VISA, PIN_3_DIGITS)

        then: 'Order is Planned'
        String cronJobNumber = at(ReplenishmentConfirmationPage).extractCronJobNumber()

        and: 'Order is created'
        waitFor { api.getOrderNumber(cronJobNumber) != null }
        String orderNumber = api.getOrderNumber(cronJobNumber)

        and: 'Transactions are created'
        api.getTransactionPaymentProvider(orderNumber) == CREDIT_CARD
        api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CAPTURE) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == COMPLETED }
    }
}
