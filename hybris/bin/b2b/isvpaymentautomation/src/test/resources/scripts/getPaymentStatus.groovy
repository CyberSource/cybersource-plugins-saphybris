package script

import de.hybris.platform.payment.enums.PaymentTransactionType

def customerAccountService = spring.getBean('customerAccountService')
def storeService = spring.getBean('baseStoreService')

def baseStore = storeService.getBaseStoreForUid("$storeCode")
def order = customerAccountService.getOrderForCode("$orderNumber", baseStore)
def transactionEntry = order.paymentTransactions.first().entries.find { it.type == PaymentTransactionType.("$transactionType") }

transactionEntry.properties.paymentStatus
