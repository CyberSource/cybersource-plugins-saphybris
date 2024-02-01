package isv.sap.payment.fraud

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.core.model.order.CartModel
import de.hybris.platform.order.CartService
import de.hybris.platform.servicelayer.config.ConfigurationService
import de.hybris.platform.servicelayer.model.ModelService
import de.hybris.platform.servicelayer.session.Session
import de.hybris.platform.servicelayer.session.SessionService
import org.apache.commons.configuration.Configuration
import org.junit.Test
import spock.lang.Specification

import isv.cjl.payment.enums.PaymentType
import isv.cjl.payment.model.Merchant
import isv.cjl.payment.service.MerchantService

@UnitTest
class DefaultFraudFacadeSpec extends Specification
{
    def merchant = Mock([useObjenesis: false], Merchant)

    def merchantService = Mock([useObjenesis: false], MerchantService)

    def sessionService = Mock([useObjenesis: false], SessionService)

    def session = Mock(useObjenesis: false, Session)

    def cart = new CartModel()

    def cartService = Mock([useObjenesis: false], CartService)

    def modelService = Mock([useObjenesis: false], ModelService)

    def configurationService = Mock([useObjenesis: false], ConfigurationService)

    def fraudFacade = new DefaultFraudFacade()

    def fingerPrint

    void setup()
    {
        def configuration = Mock([useObjenesis: false], Configuration)
        configuration.getString('isv.payment.fraud.device.finger.print.orgId') >> '1snn5n9w'
        configurationService.configuration >> configuration

        merchantService.getCurrentMerchant(PaymentType.CREDIT_CARD) >> merchant
        merchant.id >> 'test_merchant'

        fraudFacade.configurationService = configurationService
        fraudFacade.modelService = modelService
        fraudFacade.sessionService = sessionService
        fraudFacade.merchantService = merchantService
        fraudFacade.cartService = cartService
        cartService.sessionCart >> cart
    }

    @Test
    def 'should return a device finger print data'()
    {
        given:
        session.sessionId >> 'session_id_0002'
        sessionService.currentSession >> session

        when:
        fingerPrint = fraudFacade.deviceFingerPrint

        then:
        cart.fingerPrintSessionID == fingerPrint.sessionId
        1 * modelService.save(cart)

        fingerPrint.merchantId == 'test_merchant'
        fingerPrint.organizationId == '1snn5n9w'
        fingerPrint.sessionId.startsWith('session_id_0002') == true
    }
}
