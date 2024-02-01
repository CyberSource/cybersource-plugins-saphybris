package isv.sap.payment.addon.b2b.helper

import com.google.common.collect.ImmutableMap
import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.basecommerce.model.site.BaseSiteModel
import de.hybris.platform.core.model.c2l.CurrencyModel
import de.hybris.platform.core.model.order.CartModel
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.service.executor.PaymentServiceExecutor
import isv.cjl.payment.service.executor.PaymentServiceResult
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.sap.payment.configuration.service.PaymentConfigurationService
import isv.sap.payment.enums.IsvConfigurationType
import isv.sap.payment.enums.IsvPaymentChannel
import isv.sap.payment.enums.PaymentType
import isv.sap.payment.model.IsvMerchantPaymentConfigurationModel
import isv.sap.payment.model.IsvPaymentTransactionEntryModel

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION
import static isv.sap.payment.model.IsvMerchantPaymentConfigurationModel.CURRENCY
import static isv.sap.payment.model.IsvMerchantPaymentConfigurationModel.PAYMENTCHANNEL
import static isv.sap.payment.model.IsvMerchantPaymentConfigurationModel.PAYMENTTYPE
import static isv.sap.payment.model.IsvMerchantPaymentConfigurationModel.SITE

@UnitTest
class DefaultB2bPaymentAuthorizationHelperSpec extends Specification
{
    def cart = Mock([useObjenesis: false], CartModel)

    def transactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

    def paymentConfiguration = Mock([useObjenesis: false], IsvMerchantPaymentConfigurationModel)

    def paymentServiceResult = PaymentServiceResult.create()

    def executor = Mock([useObjenesis: false], PaymentServiceExecutor)

    def paymentConfigurationService = Mock([useObjenesis: false], PaymentConfigurationService)

    def paymentAuthorizationHelper = new DefaultB2bPaymentAuthorizationHelper()

    def setup()
    {
        paymentAuthorizationHelper.setPaymentConfigurationService(paymentConfigurationService)
        paymentAuthorizationHelper.paymentServiceExecutor = executor
        transactionEntry.properties >> ['req_merchant_defined_data99': 'merchant_data_', 'payment_token': 'token_data']

        def site = Mock([useObjenesis: false], BaseSiteModel)
        def currency = Mock([useObjenesis: false], CurrencyModel)
        cart.site >> site
        cart.currency >> currency

        paymentConfigurationService.getConfiguration(IsvConfigurationType.MERCHANT_CONFIG,
                                                     ImmutableMap.of(
                                                             SITE, site,
                                                             PAYMENTTYPE, PaymentType.CREDIT_CARD,
                                                             PAYMENTCHANNEL, IsvPaymentChannel.WEB,
                                                             CURRENCY, currency
                                                     )) >> paymentConfiguration
    }

    @Test
    def 'authorize payment'()
    {
        given:
        def newTransactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)
        paymentServiceResult.addData(TRANSACTION, newTransactionEntry)
        executor.execute(_ as PaymentServiceRequest) >> paymentServiceResult

        when:
        def transaction = paymentAuthorizationHelper.authorizePayment(transactionEntry, cart)

        then:
        transaction == newTransactionEntry
    }

    @Test
    def 'authorize recurring payment'()
    {
        given:
        def newTransactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)
        paymentServiceResult.addData(TRANSACTION, newTransactionEntry)
        executor.execute(_ as PaymentServiceRequest) >> paymentServiceResult
        paymentConfiguration.authServiceCommerceIndicator >> 'recurring'

        when:
        def transaction = paymentAuthorizationHelper.authorizeRecurringPayment(transactionEntry, cart)

        then:
        transaction == newTransactionEntry
    }

    @Test
    def 'authorize recurring payment fail when commerce indicator is not set'()
    {
        given:
        def newTransactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)
        paymentServiceResult.addData(TRANSACTION, newTransactionEntry)
        executor.execute(_ as PaymentServiceRequest) >> paymentServiceResult
        paymentConfiguration.authServiceCommerceIndicator >> ''

        when:
        paymentAuthorizationHelper.authorizeRecurringPayment(transactionEntry, cart)

        then:
        thrown IllegalStateException
    }
}
