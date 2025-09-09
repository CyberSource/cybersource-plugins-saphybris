package isv.sap.payment.spec.b2c.creditcard

import org.junit.experimental.categories.Category
import spock.lang.Unroll

import isv.sap.payment.pageobject.page.LoginPage
import isv.sap.payment.pageobject.page.OrderConfirmationPage
import isv.sap.payment.pageobject.page.ProductDescriptionPage
import isv.sap.payment.pageobject.page.asm.AsmLoginPage
import isv.sap.payment.pageobject.page.checkout.B2cCheckoutPage
import isv.sap.payment.pageobject.page.secure3d.Secure3D2PopUpPage
import isv.sap.payment.spec.IsvGebSpec
import isv.sap.payment.suite.Regression
import isv.sap.payment.suite.Smoke
import isv.sap.payment.suite.category.b2c.CreditCardFlex

import static isv.sap.payment.data.constants.PaymentConstants.CreditCard.*
import static isv.sap.payment.data.constants.PaymentConstants.PaymentMethod.CREDIT_CARD
import static isv.sap.payment.data.constants.TransactionStatus.ACCEPT
import static isv.sap.payment.data.constants.TransactionStatus.COMPLETED
import static isv.sap.payment.data.constants.TransactionStatus.REJECT
import static isv.sap.payment.data.constants.TransactionStatus.ORDER_SPLIT
import static isv.sap.payment.data.constants.TransactionStatus.PAYMENT_AUTHORIZED
import static isv.sap.payment.data.constants.TransactionType.AUTHORIZATION
import static isv.sap.payment.data.constants.TransactionType.CAPTURE
import static isv.sap.payment.data.constants.TransactionType.ENROLLMENT

@Category(CreditCardFlex)
class CreditCardFlexMicroformsSpec extends IsvGebSpec
{

    void setupSpec()
    {
        api.setPciStrategyFlexMicroforms()
    }

    void setup()
    {
        useUkSite()
        api.importDefaultCurrency(data)
    }

    @Regression
    @Unroll
    'should create order for Flexible Forms with Auth (registered user)'()
    {
        given: 'A cart with product and addresses'
        api.setPaymentAcceptanceTypeAuth()
        api.importCart(data)
        to(LoginPage)
                .login(data.email, data.loginCode)

        when: 'User starts Payment'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectCreditCard()
                .fillFlexFormCard(number, cvv)
                .placeOrder()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transactions are created'
        api.getTransactionPaymentProvider(orderNumber) == CREDIT_CARD
        api.getTransactionEntryStatus(orderNumber, ENROLLMENT) == ACCEPT
        api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == PAYMENT_AUTHORIZED }

        where: 'Following cards are used'
        number     | cvv
        VISA       | PIN_3_DIGITS
        MASTERCARD | PIN_3_DIGITS
        AMEX       | PIN_4_DIGITS
        MAESTRO    | PIN_3_DIGITS
        DISCOVER   | PIN_3_DIGITS
    }

    @Regression
    @Unroll
    'should create order for Flexible Forms with Sale (registered user)'()
    {
        given: 'A cart with product and addresses'
        api.setPaymentAcceptanceTypeSale()
        api.importCart(data)
        to(LoginPage)
                .login(data.email, data.loginCode)

        when: 'User starts Payment'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectCreditCard()
                .fillFlexFormCard(number, cvv)
                .placeOrder()

        then: 'Order is created'
         String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transactions are created'
        api.getTransactionPaymentProvider(orderNumber) == CREDIT_CARD
        api.getTransactionEntryStatus(orderNumber, ENROLLMENT) == ACCEPT
        api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CAPTURE) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == ORDER_SPLIT }

        where: 'Following cards are used'
        number     | cvv
        VISA       | PIN_3_DIGITS
        MASTERCARD | PIN_3_DIGITS
        AMEX       | PIN_4_DIGITS
        MAESTRO    | PIN_3_DIGITS
        DISCOVER   | PIN_3_DIGITS
    }


    @Smoke
    @Unroll
    'should create order for Flexible Forms with 3d secure 2'()
    {
        given: 'A cart with product and addresses'
        api.setPaymentAcceptanceTypeSale()
        api.importCart(data)
        to(LoginPage)
                .login(data.email, data.loginCode)

        when: 'User starts Payment'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectCreditCard()
                .fillFlexFormCard(number, cvv)
                .placeOrder()

        and: 'Populates 3d Secure'
        at(Secure3D2PopUpPage)
                .fill3dSecure2InFrame()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transactions are created'
        api.getTransactionPaymentProvider(orderNumber) == CREDIT_CARD
        api.getTransactionEntryStatus(orderNumber, ENROLLMENT) == REJECT
        api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CAPTURE) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == ORDER_SPLIT }

        where: 'Following cards are used'
        number                | cvv
        VISA_3DS2_VALID       | PIN_3_DIGITS
        MASTERCARD_3DS2_VALID | PIN_3_DIGITS
        AMEX_3DS2_VALID       | PIN_4_DIGITS
        DISCOVER_3DS2_VALID   | PIN_3_DIGITS
    }

    @Regression
    @Unroll
    'should not create order for Flexible Forms with wrong 3d secure 2'()
    {
        given: 'A cart with product and addresses'
        api.importCart(data)
        to(LoginPage)
                .login(data.email, data.loginCode)

        when: 'User starts Payment'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectCreditCard()
                .fillFlexFormCard(number, cvv)
                .placeOrder()

        and: 'Populates wrong 3d secure 2'
        if (stepUp)
        {
            at(Secure3D2PopUpPage)
                    .fill3dSecure2InFrame()
        }

        then: 'Order is not created'
        at(B2cCheckoutPage)
                .globalError.displayed

        and: 'Transaction is Declined'
        api.getCartTransactionStatus(data.email, ENROLLMENT) == REJECT
        if (stepUp)
        {
            api.getCartTransactionStatus(data.email, AUTHORIZATION) == REJECT
        }

        where: 'Following cards are used'
        number                           | cvv           |  stepUp
        VISA_3DS2_INVALID                | PIN_3_DIGITS  |  true
        MASTERCARD_3DS2_INVALID          | PIN_3_DIGITS  |  true
        DISCOVER_3DS2_INVALID            | PIN_3_DIGITS  |  true
        AMEX_3DS2_INVALID                | PIN_4_DIGITS  |  true

        VISA_3DS2_NOSTEPUP_INVALID       | PIN_3_DIGITS  |  false
        MASTERCARD_3DS2_NOSTEPUP_INVALID | PIN_3_DIGITS  |  false
        DISCOVER_3DS2_NOSTEPUP_INVALID   | PIN_3_DIGITS  |  false
        AMEX_3DS2_NOSTEPUP_INVALID       | PIN_4_DIGITS  |  false
    }

    @Regression
    def 'should create order for Flexible Forms (guest user)'() {
        given: 'Checkout is started and shipping address and method is selected'
        api.setPaymentAcceptanceTypeSale()
        to(ProductDescriptionPage, data.product)
                .addProductToCart()
                .checkoutAsGuest()
                .loginAsGuest(data.email)
                .populateShippingAndBilling(data)

        when: 'I start Payment'
        at(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectCreditCard()
                .fillFlexFormCard(VISA, PIN_3_DIGITS)
                .placeOrder()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transactions are created'
        waitFor(timeout: 10) {
            api.getTransactionPaymentProvider(orderNumber) == CREDIT_CARD
            api.getTransactionEntryStatus(orderNumber, ENROLLMENT) == ACCEPT
            api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT
        }
        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CAPTURE) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == ORDER_SPLIT }

    }


    @Regression
    'should display error when card fields not populated'()
    {
        given: 'A cart with product and addresses'
          api.setPaymentAcceptanceTypeSale()
        api.importCart(data)
        to(LoginPage)
                .login(data.email, data.loginCode)

        when: 'User starts Payment'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectCreditCard()
                .fillFlexFormCard('', '')
                .placeOrder()

        then: 'Relevant Error message is displayed'
        at(B2cCheckoutPage)
                .flexMicroform.accountNumberError.displayed
    }

    @Regression
    'should compete order in asm'()
    {
        given:
        api.importCart(data)
     api.setPaymentAcceptanceTypeSale()
        to(AsmLoginPage)
                .loginToAsm(credentials.asm)
                .selectUser(data.email)

        when: 'User starts Payment'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectCreditCard()
                .fillFlexFormCard(VISA, PIN_3_DIGITS)
                .placeOrder()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transactions are created'
        api.getTransactionPaymentProvider(orderNumber) == CREDIT_CARD
        api.getTransactionEntryStatus(orderNumber, ENROLLMENT) == ACCEPT
        api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CAPTURE) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == COMPLETED }
    }

    @Regression
    @Unroll
    'should create order for Flexible Forms with 3d secure 2 regression'()
    {
        given: 'A cart with product and addresses'
        api.importCart(data)
        api.setPaymentAcceptanceTypeSale()
        to(LoginPage)
                .login(data.email, data.loginCode)

        when: 'User starts Payment'
        to(B2cCheckoutPage)
                .startPayment()
                .paymentMode.selectCreditCard()
                .fillFlexFormCard(number, cvv)
                .placeOrder()

        then: 'Order is created'
        String orderNumber = at(OrderConfirmationPage).extractOrderNumber()

        and: 'Transactions are created'
        api.getTransactionPaymentProvider(orderNumber) == CREDIT_CARD
        api.getTransactionEntryStatus(orderNumber, ENROLLMENT) == ACCEPT
        api.getTransactionEntryStatus(orderNumber, AUTHORIZATION) == ACCEPT

        and: 'Order is completed'
        waitFor { api.getTransactionEntryStatus(orderNumber, CAPTURE) == ACCEPT }
        waitFor { api.getOrderStatus(orderNumber) == ORDER_SPLIT }

        where: 'Following cards are used'
        number                         | cvv
        VISA_3DS2_NOSTEPUP_VALID       | PIN_3_DIGITS
        MASTERCARD_3DS2_NOSTEPUP_VALID | PIN_3_DIGITS
        AMEX_3DS2_NOSTEPUP_VALID       | PIN_4_DIGITS
        DISCOVER_3DS2_NOSTEPUP_VALID   | PIN_3_DIGITS
        VISA_3DS2_TIMEOUT_VALID        | PIN_3_DIGITS
        MASTERCARD_3DS2_TIMEOUT_VALID  | PIN_3_DIGITS
        AMEX_3DS2_TIMEOUT_VALID        | PIN_4_DIGITS
        DISCOVER_3DS2_TIMEOUT_VALID    | PIN_3_DIGITS
    }
}

