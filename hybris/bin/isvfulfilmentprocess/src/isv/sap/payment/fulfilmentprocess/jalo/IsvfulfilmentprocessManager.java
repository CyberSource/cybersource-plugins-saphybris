package isv.sap.payment.fulfilmentprocess.jalo;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;

import isv.sap.payment.fulfilmentprocess.constants.IsvfulfilmentprocessConstants;

public class IsvfulfilmentprocessManager extends GeneratedIsvfulfilmentprocessManager
{
    public static final IsvfulfilmentprocessManager getInstance()
    {
        final ExtensionManager extensionManager = JaloSession.getCurrentSession().getExtensionManager();
        return (IsvfulfilmentprocessManager) extensionManager
                .getExtension(IsvfulfilmentprocessConstants.EXTENSIONNAME);
    }
}
