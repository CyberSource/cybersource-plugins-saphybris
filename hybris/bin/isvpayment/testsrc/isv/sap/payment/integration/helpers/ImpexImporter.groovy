package isv.sap.payment.integration.helpers

import de.hybris.platform.servicelayer.ServicelayerTest

/**
 * Created by esanchez on 9/25/17.
 */
class ImpexImporter extends ServicelayerTest
{
    protected static importCurrency()
    {
        importCsv('/import/importCurrencies.csv', 'windows-1252')
    }
}
