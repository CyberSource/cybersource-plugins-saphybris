package isv.sap.payment.addon.b2b.action.replenishment

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.b2bacceleratorservices.model.process.ReplenishmentProcessModel
import de.hybris.platform.commerceservices.impersonation.ImpersonationService
import de.hybris.platform.core.model.order.CartModel
import de.hybris.platform.orderscheduling.model.CartToOrderCronJobModel
import de.hybris.platform.processengine.helpers.ProcessParameterHelper
import de.hybris.platform.processengine.model.BusinessProcessParameterModel
import de.hybris.platform.servicelayer.model.ModelService
import org.junit.Test
import spock.lang.Specification

import isv.sap.payment.addon.b2b.helper.B2bPaymentAuthorizationHelper
import isv.sap.payment.addon.b2b.service.B2bPaymentTransactionService
import isv.sap.payment.model.IsvPaymentTransactionEntryModel
import isv.sap.payment.service.PaymentTransactionService

import static de.hybris.platform.b2bacceleratorservices.enums.CheckoutPaymentType.ACCOUNT
import static de.hybris.platform.b2bacceleratorservices.enums.CheckoutPaymentType.CARD
import static de.hybris.platform.payment.enums.PaymentTransactionType.CREATE_SUBSCRIPTION
import static de.hybris.platform.processengine.action.AbstractSimpleDecisionAction.Transition.OK
import static isv.sap.payment.constants.IsvPaymentConstants.SAResponseFields.CARD_TYPE
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.ACCEPT
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.ERROR
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.REJECT
import static isv.sap.payment.enums.PaymentType.CREDIT_CARD

@UnitTest
class IsvAuthorizePaymentActionSpec extends Specification
{
    def b2bPaymentTransactionService = Mock([useObjenesis: false], B2bPaymentTransactionService)

    def b2bPaymentAuthorizationHelper = Mock([useObjenesis: false], B2bPaymentAuthorizationHelper)

    def processParameterHelper = Mock([useObjenesis: false], ProcessParameterHelper)

    def replenishmentProcess = Mock([useObjenesis: false], ReplenishmentProcessModel)

    def cartToOrderCronJobModel = Mock([useObjenesis: false], CartToOrderCronJobModel)

    def modelService = Mock([useObjenesis: false], ModelService)

    def impersonationService = Mock([useObjenesis: false], ImpersonationService)

    def businessProcessParameterModel = Mock([useObjenesis: false], BusinessProcessParameterModel)

    def cart = Mock([useObjenesis: false], CartModel)

    def subscriptionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

    def action = new IsvAuthorizePaymentAction()

    def paymentTransactionService = Mock([useObjenesis: false], PaymentTransactionService)

    void setup()
    {
        action.b2bPaymentAuthorizationHelper = b2bPaymentAuthorizationHelper
        action.b2bPaymentTransactionService = b2bPaymentTransactionService
        action.processParameterHelper = processParameterHelper
        action.modelService = modelService
        action.impersonationService = impersonationService
        action.paymentTransactionService = paymentTransactionService
    }

    @Test
    def 'process authorization of a replenishment payment'()
    {
        given:
        replenishmentProcess.cartToOrderCronJob >> cartToOrderCronJobModel
        cartToOrderCronJobModel.cart >> cart
        cart.paymentType >> CARD

        and:
        subscriptionEntry.properties >> ['req_card_type': '001']

        and:
        processParameterHelper.getProcessParameterByName(replenishmentProcess, 'cart') >> businessProcessParameterModel
        businessProcessParameterModel.value >> cart

        b2bPaymentTransactionService.getLatestAcceptedTransactionEntry(CREDIT_CARD, CREATE_SUBSCRIPTION, cart) >> subscriptionEntry
        def authEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)
        b2bPaymentAuthorizationHelper.authorizeRecurringPayment(subscriptionEntry, cart) >> authEntry

        authEntry.transactionStatus >> transactionStatus

        when:
        def result = action.execute(replenishmentProcess)

        then:
        result == transition
        1 * paymentTransactionService.addProperty(CARD_TYPE, '001', authEntry)

        where:
        transactionStatus | transition
        ACCEPT            | 'OK'
        ERROR             | 'NOK'
        REJECT            | 'NOK'
    }

    @Test
    def 'processing of ACCOUNT payment type'()
    {
        given:
        replenishmentProcess.cartToOrderCronJob >> cartToOrderCronJobModel
        cartToOrderCronJobModel.cart >> cart
        cart.paymentType >> ACCOUNT

        processParameterHelper.getProcessParameterByName(replenishmentProcess, 'cart') >> businessProcessParameterModel
        businessProcessParameterModel.value >> cart
        impersonationService.executeInContext(_, _) >> OK

        when:
        def result = action.execute(replenishmentProcess)

        then:
        result == 'OK'
        0 * b2bPaymentAuthorizationHelper.authorizeRecurringPayment(_, _)
    }
}
