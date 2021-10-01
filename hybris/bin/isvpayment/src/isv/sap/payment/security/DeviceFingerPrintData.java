package isv.sap.payment.security;

import static com.google.common.base.Preconditions.checkNotNull;

public class DeviceFingerPrintData
{
    private final String merchantId;

    private final String organizationId;

    private final String sessionId;

    public DeviceFingerPrintData(final String merchantId, final String organizationId, final String sessionId)
    {
        this.merchantId = checkNotNull(merchantId);
        this.organizationId = checkNotNull(organizationId);
        this.sessionId = checkNotNull(sessionId);
    }

    public String getMerchantId()
    {
        return merchantId;
    }

    public String getOrganizationId()
    {
        return organizationId;
    }

    public String getSessionId()
    {
        return sessionId;
    }
}
