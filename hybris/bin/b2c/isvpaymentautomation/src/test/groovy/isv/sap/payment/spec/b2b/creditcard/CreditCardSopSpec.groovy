package isv.sap.payment.spec.b2b.creditcard

import org.junit.experimental.categories.Category
import spock.lang.Unroll

import isv.sap.payment.pageobject.page.LoginPage
import isv.sap.payment.pageobject.page.OrderConfirmationPage
import isv.sap.payment.pageobject.page.ProductDescriptionPage
import isv.sap.payment.pageobject.page.asm.AsmLoginPage
import isv.sap.payment.pageobject.page.checkout.B2bCheckoutPage
import isv.sap.payment.spec.IsvGebSpec
import isv.sap.payment.suite.Regression
import isv.sap.payment.suite.Smoke
import isv.sap.payment.suite.category.b2b.CreditCardSOP

import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.CARTESBANCAIRES
import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.DINERS
import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.MAESTRO
import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.DISCOVER
import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.AMEX
import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.MASTERCARD
import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.PIN_3_DIGITS
import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.PIN_4_DIGITS
import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.SOP_SELECTOR_AMEX
import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.SOP_SELECTOR_MASTERCARD
import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.SOP_SELECTOR_VISA
import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.SOP_SELECTOR_DISCOVER
import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.SOP_SELECTOR_MAESTRO
import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.SOP_SELECTOR_DINERS
import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.SOP_SELECTOR_CARTESBANCAIRES

import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.VISA
import static isv.sap.payment.data.constants.PaymentConstants.PaymentMethod.CREDIT_CARD
import static isv.sap.payment.data.constants.TransactionStatus.ACCEPT
import static isv.sap.payment.data.constants.TransactionStatus.COMPLETED
import static isv.sap.payment.data.constants.TransactionStatus.ORDER_SPLIT
import static isv.sap.payment.data.constants.TransactionStatus.PAYMENT_AUTHORIZED
import static isv.sap.payment.data.constants.TransactionType.AUTHORIZATION
import static isv.sap.payment.data.constants.TransactionType.CAPTURE

@Category(CreditCardSOP)
class CreditCardSopSpec extends IsvGebSpec {
    void setupSpec() {
        api.setPciStrategySop()
    }

    void setup() {
        useB2bSite()
    }

    @Regression
     @Unroll
    'should create order for SOP with Sale'() {
        given: 'The checkout is started'
        api.setPaymentAcceptanceTypeSale()
        to(LoginPage)
                .login(data.email, data.loginCode)
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
        waitFor { api.getOrderStatus(orderNumber) == ORDER_SPLIT }

        where: 'Following cards are used'
        type                        | number          | cvv
       SOP_SELECTOR_VISA            | VISA            | PIN_3_DIGITS
       SOP_SELECTOR_MASTERCARD      | MASTERCARD      | PIN_3_DIGITS
       SOP_SELECTOR_AMEX            | AMEX            | PIN_4_DIGITS
       SOP_SELECTOR_DISCOVER        | DISCOVER        | PIN_3_DIGITS
       SOP_SELECTOR_MAESTRO         | MAESTRO         | PIN_3_DIGITS
       SOP_SELECTOR_DINERS          | DINERS          | PIN_3_DIGITS
       SOP_SELECTOR_CARTESBANCAIRES | CARTESBANCAIRES | PIN_3_DIGITS
    }

    @Regression
    @Unroll
    'should create order for SOP with Auth'() {
        given: 'The checkout is started'
        api.setPaymentAcceptanceTypeAuth()
        to(LoginPage)
                .login(data.email, data.loginCode)
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
        waitFor { api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == PAYMENT_AUTHORIZED }


        where: 'Following cards are used'
        type                        | number          | cvv
        SOP_SELECTOR_VISA            | VISA            | PIN_3_DIGITS
        SOP_SELECTOR_MASTERCARD      | MASTERCARD      | PIN_3_DIGITS
        SOP_SELECTOR_AMEX            | AMEX            | PIN_4_DIGITS
        SOP_SELECTOR_DISCOVER        | DISCOVER        | PIN_3_DIGITS
        SOP_SELECTOR_MAESTRO         | MAESTRO         | PIN_3_DIGITS
        SOP_SELECTOR_DINERS          | DINERS          | PIN_3_DIGITS
        SOP_SELECTOR_CARTESBANCAIRES | CARTESBANCAIRES | PIN_3_DIGITS
    }


    @Smoke
    'should create order from ASM'()
    {
        given: 'The checkout is started with ASM'
        api.setPaymentAcceptanceTypeSale()
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
                .login(data.email, data.loginCode)
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