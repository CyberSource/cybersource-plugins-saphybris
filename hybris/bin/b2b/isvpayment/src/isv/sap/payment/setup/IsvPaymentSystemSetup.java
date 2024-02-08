package isv.sap.payment.setup;

import javax.annotation.Resource;

import de.hybris.platform.commerceservices.setup.SetupImpexService;
import de.hybris.platform.core.initialization.SystemSetup;

import isv.sap.payment.constants.IsvPaymentConstants;

/**
 * This class is responsible for executing system maintaining tasks during initialization or update system,
 * like importing project and essential data
 */
@SystemSetup(extension = IsvPaymentConstants.EXTENSIONNAME)
public class IsvPaymentSystemSetup
{
    private static final String DATA_IMPORT_FOLDER = "/import/";

    @Resource
    private SetupImpexService setupImpexService;

    @SystemSetup(process = SystemSetup.Process.INIT, type = SystemSetup.Type.ESSENTIAL)
    public void createEssentialData()
    {
        setupImpexService.importImpexFile(DATA_IMPORT_FOLDER + "cronjobs.impex", true);
    }
}
