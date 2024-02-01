package isv.sap.payment.fulfilmentprocess.actions.order

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.basecommerce.enums.FraudStatus
import de.hybris.platform.core.enums.OrderStatus
import de.hybris.platform.core.model.order.OrderModel
import de.hybris.platform.core.model.user.UserModel
import de.hybris.platform.fraud.model.FraudReportModel
import de.hybris.platform.orderprocessing.model.OrderProcessModel
import de.hybris.platform.payment.dto.TransactionStatus
import de.hybris.platform.payment.enums.PaymentTransactionType
import de.hybris.platform.payment.model.PaymentTransactionEntryModel
import de.hybris.platform.payment.model.PaymentTransactionModel
import de.hybris.platform.servicelayer.model.ModelService
import de.hybris.platform.ticket.enums.CsTicketCategory
import de.hybris.platform.ticket.enums.CsTicketPriority
import de.hybris.platform.ticket.model.CsTicketModel
import de.hybris.platform.ticket.service.TicketBusinessService
import org.junit.Test
import spock.lang.Specification

@UnitTest
class CheckTransactionReviewStatusActionSpock extends Specification
{
    def order = Mock([useObjenesis: false], OrderModel)

    def user = Mock([useObjenesis: false], UserModel)

    def paymentTransaction = Mock([useObjenesis: false], PaymentTransactionModel)

    def paymentTransactionEntry = Mock([useObjenesis: false], PaymentTransactionEntryModel)

    def fraudReport = Mock([useObjenesis: false], FraudReportModel)

    def ticket = Mock([useObjenesis: false], CsTicketModel)

    def process = Mock([useObjenesis: false], OrderProcessModel)

    def ticketBusinessService = Mock([useObjenesis: false], TicketBusinessService)

    def modelService = Mock([useObjenesis: false], ModelService)

    @SuppressWarnings('BracesForClass')
    def action = new CheckTransactionReviewStatusAction() {
        @Override
        protected String getLocalizedString(final String key, final Object... params)
        {
            'localized_string'
        }
    }

    void setup()
    {
        action.ticketBusinessService = ticketBusinessService
        action.modelService = modelService

        process.order >> order
        order.user >> user
        order.fraudReports >> [fraudReport]
        paymentTransaction.entries >> [paymentTransactionEntry]

        order.paymentTransactions >> [paymentTransaction]
        paymentTransactionEntry.transactionStatus >> TransactionStatus.REVIEW.name()

        modelService.create(CsTicketModel) >> ticket
    }

    @Test
    def 'action should pass for order which is not in review state'()
    {
        given:
        paymentTransactionEntry.transactionStatus >> TransactionStatus.ACCEPTED

        when:
        def result = action.execute(process)

        then:
        result == 'OK'
    }

    @Test
    def 'action create a ticket for fraud decision'()
    {
        given:
        paymentTransactionEntry.type >> PaymentTransactionType.AUTHORIZATION
        fraudReport.status >> null

        when:
        action.execute(process)

        then:
        1 * ticket.setHeadline('localized_string')
        1 * ticket.setCategory(CsTicketCategory.FRAUD)
        1 * ticket.setPriority(CsTicketPriority.HIGH)
        1 * ticket.setOrder(order)
        1 * ticket.setCustomer(user)
        1 * ticketBusinessService.createTicket(*_) >> { arguments ->
            assert arguments[0] == ticket
            assert arguments[1].text == 'localized_string'
        }
    }

    def 'action should validate transition value'()
    {
        given:
        paymentTransactionEntry.type >> PaymentTransactionType.AUTHORIZATION
        fraudReport.status >> fraudStatus

        when:
        def result = action.execute(process)

        then:
        result == expectedTransition
        1 * order.setStatus(orderStatus)
        1 * modelService.save(order)

        where:
        fraudStatus       | orderStatus                        | expectedTransition
        FraudStatus.OK    | OrderStatus.PAYMENT_AUTHORIZED     | 'OK'
        FraudStatus.FRAUD | OrderStatus.PAYMENT_NOT_AUTHORIZED | 'NOK'
        null              | OrderStatus.FRAUD_DECISION         | 'WAIT'
    }
}
