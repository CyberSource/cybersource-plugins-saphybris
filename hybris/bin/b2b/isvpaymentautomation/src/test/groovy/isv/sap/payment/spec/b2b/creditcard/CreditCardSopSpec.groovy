package isv.sap.payment.spec.b2b.creditcard

import org.junit.experimental.categories.Category

import isv.sap.payment.pageobject.page.LoginPage
import isv.sap.payment.pageobject.page.OrderConfirmationPage
import isv.sap.payment.pageobject.page.ProductDescriptionPage
import isv.sap.payment.pageobject.page.asm.AsmLoginPage
import isv.sap.payment.pageobject.page.checkout.B2bCheckoutPage
import isv.sap.payment.spec.IsvGebSpec
import isv.sap.payment.suite.Regression
import isv.sap.payment.suite.Smoke
import isv.sap.payment.suite.category.CreditCard

import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.AMEX
import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.MASTERCARD
import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.PIN_3_DIGITS
import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.PIN_4_DIGITS
import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.SOP_SELECTOR_AMEX
import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.SOP_SELECTOR_MASTERCARD
import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.SOP_SELECTOR_VISA
import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.VISA
import static isv.sap.payment.data.constants.PaymentConstants.PaymentMethod.CREDIT_CARD
import static isv.sap.payment.data.constants.TransactionStatus.ACCEPT
import static isv.sap.payment.data.constants.TransactionStatus.COMPLETED
import static isv.sap.payment.data.constants.TransactionType.AUTHORIZATION
import static isv.sap.payment.data.constants.TransactionType.CAPTURE

@Category(CreditCard)
class CreditCardSopSpec extends IsvGebSpec
{
    void setupSpec()
    {
        api.setPciStrategySop()
    }

    void setup()
    {
        useB2bSite()
    }

    @Regression
    'should create order for SOP'()
    {
        given: 'The checkout is started'
        to(LoginPage)
                .login(data.email, data.password)
        to(ProductDescriptionPage, data.product)
                .addProductToCart()
                .checkoutB2B()
                .proceedToCardPayment()
                .populateShippingAndBilling(data)

        when: 'I pay with credit card'
        at(B2bCheckoutPage)
                .fillSopCard(type, number, cvv)
                .placeOrder()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transactions are created'
        api.getTransactionPaymentProvider(orderNumber) == CREDIT_CARD
        api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CAPTURE) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == COMPLETED }

        where: 'Following cards are used'
        type                    | number     | cvv
        SOP_SELECTOR_VISA       | VISA       | PIN_3_DIGITS
        SOP_SELECTOR_MASTERCARD | MASTERCARD | PIN_3_DIGITS
        SOP_SELECTOR_AMEX       | AMEX       | PIN_4_DIGITS
    }

    @Smoke
    'should create order from ASM'()
    {
        given: 'The checkout is started with ASM'
        to(AsmLoginPage)
                .loginToAsm(credentials.asm)
                .selectUser(data.email)
        to(ProductDescriptionPage, data.product)
                .addProductToCart()
                .checkoutB2B()
                .proceedToCardPayment()
                .populateShippingAndBilling(data)

        when: 'I pay with credit card'
        at(B2bCheckoutPage)
                .fillSopCard(SOP_SELECTOR_VISA, VISA, PIN_3_DIGITS)
                .placeOrder()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transactions are created'
        api.getTransactionPaymentProvider(orderNumber) == CREDIT_CARD
        api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CAPTURE) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == COMPLETED }
    }

    @Regression
    'should display error when card fields not populated'()
    {
        given: 'The checkout is started'
        to(LoginPage)
                .login(data.email, data.password)
        to(ProductDescriptionPage, data.product)
                .addProductToCart()
                .checkoutB2B()
                .proceedToCardPayment()
                .populateShippingAndBilling(data)

        when: 'I pay with credit card'
        at(B2bCheckoutPage)
                .fillSopCard('', '', '')
                .placeOrder()

        then: 'Relevant Error message is displayed'
        at(B2bCheckoutPage)
                .card.errorMessage.displayed
    }
}
