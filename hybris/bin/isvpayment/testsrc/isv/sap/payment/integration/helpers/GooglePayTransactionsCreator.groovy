package isv.sap.payment.integration.helpers

import de.hybris.platform.core.model.order.AbstractOrderModel

import isv.cjl.payment.service.executor.request.builder.googlepay.AuthorizationRequestBuilder
import isv.cjl.payment.service.executor.request.builder.googlepay.CaptureRequestBuilder

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION

class GooglePayTransactionsCreator extends TransactionCreator
{
    static Map getFakePaymentData()
    {
        [
                'apiVersion'       : 2,
                'apiVersionMinor'  : 0,
                'paymentMethodData': [
                        'description'     : 'Visa ***** 1234',
                        'info'            : [
                                'cardDetails': '1234',
                                'cardNetwork': 'VISA'
                        ],
                        'tokenizationData': [
                                'token': 'FAKE',
                                'type' : 'PAYMENT_GATEWAY'
                        ],
                        type              : 'CARD'
                ]
        ]
    }

    //This token is generated in the front end when accepting payment with Google Pay and has an expiration date.
    // Therefore must be updated before running integration tests
    static Map getPaymentData()
    {
        [:]
    }

    static Map getExpiredPaymentData()
    {
        [
                'apiVersion'       : 2,
                'apiVersionMinor'  : 0,
                'paymentMethodData': [
                        'description'     : 'Visa ***** 1234',
                        'info'            : [
                                'cardDetails': '1234',
                                'cardNetwork': 'VISA'
                        ],
                        'tokenizationData': [
                                'token': '{"signature":"MEUCIQD/MCXAkvZF97SsPXE4S8Ag3sGTVi2KinLPy1o3GcCUcgIgIQSPnJwBcEcmayLOACn14Wxm2wlAYOOYjM9CEQxpOBw\\u003d","protocolVersion":"ECv1","signedMessage":"{\\"encryptedMessage\\":\\"hDLfgJ+P2iJUCYbV2o1iJTJV8ZGF4C//No/0zrxvopn7DxUGomVJqfn7BmGteZuGQHWwoOMXLP1/VyOepMq74R2eeSP2cCsfOeOOmvDLqFzlhc3GvzGEhbX7uDLY7lfQfKJkKS1HYWJps3lD/P5V9ATctMEsBpaAsLPSYYEp4NuPsSVsh1U7wDsI1Jhh60g1b5MjaXV+mFo9pkdQ0sewlXRz4nlPh369w20obPfCd8JRF4Eczfmg3sCHcADYaS5g4kFOouDf3Prba+JYAKnrlw8rzXgXxPjfCRbf9Ct4jROlPOoMzD20au+AgTU3BO75sFbW3A58C15rcOk1JxU1KMfNZqugR2d4YIINFmlQGOZAPHAJbrFS3rtXg5k9eLBc+O+SKZsH6aYWfhXJaouUt45Prc9b0Nq1W/gyqwP4vQufhug9Ql+0oJIOneqew9oohCGVXzdeSPos5Ay+Hj15sWA6KOvnNbZkpavM6ev9sgrksKl6rlzJcBWlj8OFy5leV0hRhcqFLW3xJcWG9H1qqwrVTdRL+g1lOp8IlCzktv11u7gpA9CuHRXudSjBc7/hHrb+DhuccyfjrFS2fgSiABSmiI0\\\\u003d\\",\\"ephemeralPublicKey\\":\\"BMJwLsGwGqpGLLNnHhpl0yDVydJhEL4w1cCH7Sp74gt5ymeQ/qKbBHTYO3yjPyrW5i4bP6+tToNXCIbcP5oXEcU\\\\u003d\\",\\"tag\\":\\"yZ/RN80PdB6Axc0SxjgYlnrt4bOEMazdC77Mw7Ty+4E\\\\u003d\\"}"}',
                                'type' : 'PAYMENT_GATEWAY'
                        ],
                        type              : 'CARD'
                ]
        ]
    }

    static String getEncryptedExpiredPaymentToken()
    {
        expiredPaymentData.paymentMethodData.tokenizationData.token.bytes.encodeBase64().toString()
    }

    def createAuthorization(AbstractOrderModel order)
    {
        def request = new AuthorizationRequestBuilder()
                .setMerchantId(testConfig.merchant)
                .setPaymentData(paymentData)
                .addParam(ORDER, order)
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
