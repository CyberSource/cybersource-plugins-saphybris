package isv.sap.payment.spec.b2b.replenishment

import org.junit.experimental.categories.Category

import isv.sap.payment.pageobject.page.LoginPage
import isv.sap.payment.pageobject.page.ProductDescriptionPage
import isv.sap.payment.pageobject.page.ReplenishmentConfirmationPage
import isv.sap.payment.pageobject.page.asm.AsmLoginPage
import isv.sap.payment.pageobject.page.checkout.B2bCheckoutPage
import isv.sap.payment.spec.IsvGebSpec
import isv.sap.payment.suite.Regression
import isv.sap.payment.suite.Smoke
import isv.sap.payment.suite.category.Account

import static isv.sap.payment.data.constants.TransactionStatus.COMPLETED

@Category(Account)
class ReplenishmentAccountSpec extends IsvGebSpec
{
    void setup()
    {
        useB2bSite()
    }

    @Smoke
    'should create order for account payment'()
    {
        given: 'The checkout is started'
        to(LoginPage)
                .login(data.email, data.password)
        to(ProductDescriptionPage, data.product)
                .addProductToCart()
                .checkoutB2B()
                .proceedToAccountPayment()

        when: 'I start Payment'
        at(B2bCheckoutPage)
                .startReplenishment()
                .scheduleReplenishment()

        then: 'Order is Planned'
        String cronJobNumber = at(ReplenishmentConfirmationPage).extractCronJobNumber()

        and: 'Order is created'
        waitFor { api.getOrderNumber(cronJobNumber) != null }
        String orderNumber = api.getOrderNumber(cronJobNumber)

        and: 'Order is completed'
        waitFor { api.getOrderStatus(orderNumber) == COMPLETED }
        api.getTransactionPaymentProvider(orderNumber) == null
    }

    @Regression
    'should create order for asm'()
    {
        given: 'The checkout is started'
        to(AsmLoginPage)
                .loginToAsm(credentials.asm)
                .selectUser(data.email)
        to(ProductDescriptionPage, data.product)
                .addProductToCart()
                .checkoutB2B()
                .proceedToAccountPayment()

        when: 'I start Payment'
        at(B2bCheckoutPage)
                .startReplenishment()
                .scheduleReplenishment()

        then: 'Order is Planned'
        String cronJobNumber = at(ReplenishmentConfirmationPage).extractCronJobNumber()

        and: 'Order is created'
        waitFor { api.getOrderNumber(cronJobNumber) != null }
        String orderNumber = api.getOrderNumber(cronJobNumber)

        and: 'Order is completed'
        waitFor { api.getOrderStatus(orderNumber) == COMPLETED }
        api.getTransactionPaymentProvider(orderNumber) == null
    }
}
