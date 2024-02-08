package isv.sap.payment.addon.facade

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.acceleratorservices.config.SiteConfigService
import de.hybris.platform.acceleratorservices.urlresolver.SiteBaseUrlResolutionService
import de.hybris.platform.basecommerce.model.site.BaseSiteModel
import de.hybris.platform.core.model.order.CartModel
import de.hybris.platform.payment.enums.PaymentTransactionType
import de.hybris.platform.payment.model.PaymentTransactionModel
import de.hybris.platform.site.BaseSiteService
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.enums.PaymentSource
import isv.cjl.payment.model.Merchant
import isv.cjl.payment.service.MerchantService
import isv.cjl.payment.service.executor.PaymentServiceExecutor
import isv.cjl.payment.service.executor.PaymentServiceResult
import isv.cjl.payment.service.executor.request.PaymentServiceRequest
import isv.sap.payment.addon.constants.IsvPaymentAddonConstants
import isv.sap.payment.addon.facade.impl.KlarnaPaymentFacadeImpl
import isv.sap.payment.enums.PaymentType
import isv.sap.payment.model.IsvPaymentTransactionEntryModel
import isv.sap.payment.service.PaymentTransactionService

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION
import static isv.cjl.payment.enums.PaymentTransactionType.CREATE_SESSION
import static isv.cjl.payment.enums.PaymentTransactionType.UPDATE_SESSION
import static isv.cjl.payment.enums.PaymentType.ALTERNATIVE_PAYMENT

@UnitTest
class KlarnaPaymentFacadeImplSpec extends Specification
{
    PaymentServiceExecutor paymentServiceExecutor = Mock()
    PaymentTransactionService paymentTransactionService = Mock()
    MerchantService merchantService = Mock()

    BaseSiteService baseSiteService = Mock()
    SiteBaseUrlResolutionService siteBaseUrlResolutionService = Mock()
    SiteConfigService siteConfigService = Mock()

    def facade = new KlarnaPaymentFacadeImpl()

    PaymentTransactionModel transaction = Mock()

    CartModel cart = Mock()
    Merchant merchantModel = Mock()
    IsvPaymentTransactionEntryModel transactionEntry = Mock()

    BaseSiteModel site = Mock()

    def setup()
    {
        facade.merchantService = merchantService
        facade.paymentServiceExecutor = paymentServiceExecutor
        facade.paymentTransactionService = paymentTransactionService
        facade.siteBaseUrlResolutionService = siteBaseUrlResolutionService
        facade.siteConfigService = siteConfigService
        facade.baseSiteService = baseSiteService

        merchantService.getCurrentMerchant(_) >> merchantModel
        merchantModel.id >> 'test_merchant'
        baseSiteService.currentBaseSite >> site
    }

    @Test
    def 'createKlarnaSession: Should fail if request was rejected'()
    {
        given:
        def createSessionTxn = new IsvPaymentTransactionEntryModel()
        createSessionTxn.setTransactionStatus('REJECT')

        def result = PaymentServiceResult.create().addData(TRANSACTION, createSessionTxn)

        when:
        facade.createKlarnaSession(cart)

        then:
        interaction {
            verifyAndStubForCreateKlarnaSession()
        }

        1 * paymentServiceExecutor.execute { it -> checkCreateSessionParams(it) } >> result

        IllegalStateException ex = thrown()
        ex.message == 'Klarna create session call wasn\'t successful, decision was: REJECT'
    }

    @Test
    def 'createKlarnaSession: Should return sessionId if request was accepted'()
    {
        given:
        def createSessionTxn = new IsvPaymentTransactionEntryModel()
        createSessionTxn.setTransactionStatus('ACCEPT')
        createSessionTxn.properties = [apSessionsReplyProcessorToken: '123']
        def result = PaymentServiceResult.create().addData(TRANSACTION, createSessionTxn)

        when:
        def sessionId = facade.createKlarnaSession(cart)

        then:
        interaction {
            verifyAndStubForCreateKlarnaSession()
        }

        1 * paymentServiceExecutor.execute { it -> checkCreateSessionParams(it) } >> result

        sessionId == '123'
    }

    @Test
    def 'updateKlarnaSession: Should fail if request was rejected'()
    {
        given:
        def sessionTxn = new IsvPaymentTransactionEntryModel()
        sessionTxn.setTransactionStatus('REJECT')

        def result = PaymentServiceResult.create().addData(TRANSACTION, sessionTxn)

        when:
        facade.updateKlarnaSession(cart)

        then:
        interaction {
            verifyAndStubForUpdateKlarnaSession()
        }

        1 * paymentServiceExecutor.execute { it -> checkUpdateSessionParams(it) } >> result

        IllegalStateException ex = thrown()
        ex.message == 'Klarna update session call wasn\'t successful, decision was: REJECT'
    }

    @Test
    def 'updateKlarnaSession: Should return sessionId if request was accepted'()
    {
        given:
        def sessionTxn = new IsvPaymentTransactionEntryModel()
        sessionTxn.setTransactionStatus('ACCEPT')
        sessionTxn.properties = [apSessionsReplyProcessorToken: '123']

        def result = PaymentServiceResult.create().addData(TRANSACTION, sessionTxn)

        when:
        def sessionId = facade.updateKlarnaSession(cart)

        then:
        interaction {
            verifyAndStubForUpdateKlarnaSession()
        }

        1 * paymentServiceExecutor.execute { it -> checkUpdateSessionParams(it) } >> result

        sessionId == '123'
    }

    @Test
    def 'updateKlarnaSession: Should throw exception if create session transaction is not found'()
    {
        when:
        facade.updateKlarnaSession(cart)

        then:
        1 * paymentTransactionService.getLatestTransaction(PaymentType.ALTERNATIVE_PAYMENT, cart) >> Optional.of(transaction)
        1 * paymentTransactionService.getLatestAcceptedTransactionEntry(transaction, PaymentTransactionType.CREATE_SESSION) >> Optional.empty()

        and:
        def exception = thrown(IllegalStateException)
        exception.message == "Unable to find Klarna create session transaction for order: ${cart.guid}"
    }

    def verifyAndStubForCreateKlarnaSession()
    {
        1 * merchantService.getCurrentMerchant(ALTERNATIVE_PAYMENT) >> merchantModel
        1 * siteConfigService.getProperty(IsvPaymentAddonConstants.AlternativePayments.RELATIVE_RETURN_URL) >> 'success'
        1 * siteConfigService.getProperty(IsvPaymentAddonConstants.AlternativePayments.RELATIVE_FAILED_URL) >> 'failure'
        1 * siteConfigService.getProperty(IsvPaymentAddonConstants.AlternativePayments.RELATIVE_CANCEL_URL) >> 'cancel'
        1 * siteBaseUrlResolutionService.getWebsiteUrlForSite(site, true, 'cancel') >> 'https://localhost:9001/cancel'
        1 * siteBaseUrlResolutionService.getWebsiteUrlForSite(site, true, 'success') >> 'https://localhost:9001/success?ap='
        1 * siteBaseUrlResolutionService.getWebsiteUrlForSite(site, true, 'failure') >> 'https://localhost:9001/failure'
    }

    def verifyAndStubForUpdateKlarnaSession()
    {
        1 * paymentTransactionService.getLatestTransaction(PaymentType.ALTERNATIVE_PAYMENT, cart) >> Optional.of(transaction)
        1 * paymentTransactionService.getLatestAcceptedTransactionEntry(transaction, PaymentTransactionType.CREATE_SESSION) >> Optional.of(transactionEntry)
        1 * siteConfigService.getProperty(IsvPaymentAddonConstants.AlternativePayments.KLARNA_LANGUAGE) >> 'en-GB'

        verifyAndStubForCreateKlarnaSession()
    }

    def checkCreateSessionParams(PaymentServiceRequest request)
    {
        checkParams(request, CREATE_SESSION)
    }

    def checkUpdateSessionParams(PaymentServiceRequest request)
    {
        assert request.getParam('transaction') == transactionEntry
        assert request.getParam('billToLanguage') == 'en-GB'

        checkParams(request, UPDATE_SESSION)
    }

    def checkParams(PaymentServiceRequest request, isv.cjl.payment.enums.PaymentTransactionType type)
    {
        assert request.paymentSource == PaymentSource.SERVICE_API
        assert request.paymentTransactionType == type
        assert request.getParam('merchantId') == 'test_merchant'
        assert request.getParam('order') == cart
        assert request.getParam('apSessionsServiceCancelURL') == 'https://localhost:9001/cancel'
        assert request.getParam('apSessionsServiceFailureURL') == 'https://localhost:9001/failure'
        assert request.getParam('apSessionsServiceSuccessURL') == 'https://localhost:9001/success?ap=KLI'

        true
    }
}
