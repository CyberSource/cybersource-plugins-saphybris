package isv.sap.payment.fraud;

import java.time.Instant;
import javax.annotation.Resource;

import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionService;

import isv.cjl.payment.model.Merchant;
import isv.cjl.payment.service.MerchantService;
import isv.sap.payment.security.DeviceFingerPrintData;

import static isv.cjl.payment.enums.PaymentType.CREDIT_CARD;

/**
 * Encapsulates the default implementation of {@link FraudFacade} interface.
 * <p>
 * Current implementation provides default logic related to fraud-management like device fingerprint computation.
 */
public class DefaultFraudFacade implements FraudFacade
{
    @Resource(name = "isv.sap.payment.hybrisMerchantService")
    private MerchantService merchantService;

    @Resource
    private SessionService sessionService;

    @Resource
    private CartService cartService;

    @Resource
    private ModelService modelService;

    @Resource
    private ConfigurationService configurationService;

    @Override
    public DeviceFingerPrintData getDeviceFingerPrint()
    {
        final Merchant merchant = merchantService.getCurrentMerchant(CREDIT_CARD);

        final String orgID = configurationService.getConfiguration()
                .getString("isv.payment.fraud.device.finger.print.orgId");

        final DeviceFingerPrintData deviceFingerPrintData = new DeviceFingerPrintData(merchant.getId(), orgID,
                newSessionId());

        storeSessionIdToCart(deviceFingerPrintData.getSessionId());

        return deviceFingerPrintData;
    }

    private String newSessionId()
    {
        return sessionService.getCurrentSession().getSessionId() + '_' + Instant.now().toEpochMilli();
    }

    private void storeSessionIdToCart(final String sessionId)
    {
        final CartModel sessionCart = cartService.getSessionCart();
        sessionCart.setFingerPrintSessionID(sessionId);

        modelService.save(sessionCart);
    }
}
