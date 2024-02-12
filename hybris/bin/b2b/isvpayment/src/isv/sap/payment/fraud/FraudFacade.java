package isv.sap.payment.fraud;

import isv.sap.payment.security.DeviceFingerPrintData;

/**
 * This interface defines methods related to fraud-management functionality.
 */
public interface FraudFacade
{
    /**
     * Encapsulates creation of device fingerprint data for current session and merchant.
     *
     * @return an instance of device fingerprint data object.
     */
    DeviceFingerPrintData getDeviceFingerPrint();
}
