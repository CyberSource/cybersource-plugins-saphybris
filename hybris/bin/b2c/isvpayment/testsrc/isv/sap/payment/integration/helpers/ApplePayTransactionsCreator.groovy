package isv.sap.payment.integration.helpers

import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.payment.dto.BasicCardInfo

import isv.cjl.payment.enums.CardType
import isv.cjl.payment.enums.DecryptionType
import isv.cjl.payment.service.executor.request.builder.applepay.AuthorizationRequestBuilder
import isv.cjl.payment.service.executor.request.builder.applepay.CaptureRequestBuilder

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION

class ApplePayTransactionsCreator extends TransactionCreator
{

    def encryptedFakeToken()
    {
        [
                token: [
                        paymentData  : [
                                data: 'FAKE'
                        ],
                        paymentMethod: [
                                network: 'Visa'
                        ]
                ]
        ]
    }

    def decryptedToken(String cardNumber, Double totalPrice)
    {
        [
                'applicationPrimaryAccountNumber': cardNumber,
                'applicationExpirationDate'      : '221101',//YYMMDD
                'currencyCode'                   : '840',
                'transactionAmount'              : totalPrice,
                'cardHolderName'                 : 'Some Name',
                'deviceManufacturerIdentifier'   : 'XXXXXXXXXX',
                'paymentData'                    : [key: 'value'],
                'paymentDataType'                : '3DSecure',//3DSecure or EMV
        ]
    }

    def createAuthorization(AbstractOrderModel order, BasicCardInfo card)
    {
        def request = new AuthorizationRequestBuilder()
                .setMerchantId(testConfig.merchant)
                .setPaymentToken(decryptedToken(card.cardNumber, order.totalPrice))
                .addParam(ORDER, order)
                .setDecryptionType(DecryptionType.MERCHANT)
                .setCardType(CardType.VISA)
                .build()

        def result = paymentServiceExecutor.execute(request)

        result.getData(TRANSACTION)
    }

    def createCapture(AbstractOrderModel order, authorization)
    {

        def request = new CaptureRequestBuilder()
                .setMerchantId(testConfig.merchant)
                .addParam('order', order)
                .addParam('transaction', authorization)
                .build()

        def result = paymentServiceExecutor.execute(request)

        result.getData(TRANSACTION)
    }
}
