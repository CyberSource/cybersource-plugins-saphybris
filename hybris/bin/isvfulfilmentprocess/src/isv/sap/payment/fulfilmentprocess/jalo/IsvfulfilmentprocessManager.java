package isv.sap.payment.fulfilmentprocess.jalo;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;

import isv.sap.payment.fulfilmentprocess.constants.IsvfulfilmentprocessConstants;

@SuppressWarnings("PMD")
public class IsvfulfilmentprocessManager extends GeneratedIsvfulfilmentprocessManager
{
    public static final IsvfulfilmentprocessManager getInstance()
    {
        ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
        return (IsvfulfilmentprocessManager) em
                .getExtension(IsvfulfilmentprocessConstants.EXTENSIONNAME);
    }
}
