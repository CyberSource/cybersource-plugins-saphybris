package isv.sap.payment.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * Provides information about payment api system (e.g. version, application, etc.).
 */
public class PaymentSystemInfo
{
    private static final Logger LOG = LoggerFactory.getLogger(PaymentSystemInfo.class);

    private static final String VERSION_INFO_FILE = "version.info";

    private static final String DEFAULT_CLIENT_APPLICATION = "SOAP Toolkit API";

    @Value("${build.vendor}")
    private String clientEnvironment;

    @Value("${build.version}")
    private String clientApplicationVersion;

    @Value("${isv.payment.customer.request.developer.id}")
    private String developerID;

    @Value("${isv.payment.customer.request.client.library}")
    private String clientLibrary;

    @Value("${isv.payment.customer.request.partnerSolution.id:D2OKBRXN}")
    private String partnerSolutionId;

    private String clientLibraryVersion;

    @PostConstruct
    public void init()
    {
        try
        {
            setClientLibraryVersion();
        }
        catch (final IOException e)
        {
            LOG.warn(e.getMessage(), e);
        }
    }

    protected void setClientLibraryVersion() throws IOException
    {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(VERSION_INFO_FILE))
        {
            if (stream != null)
            {
                final Properties props = new Properties();
                props.load(stream);
                clientLibraryVersion = props.getProperty("build_version");
            }
        }
    }

    public String getPartnerSolutionID()
    {
        return partnerSolutionId;
    }

    public String getClientApplication()
    {
        return DEFAULT_CLIENT_APPLICATION;
    }

    public String getDeveloperID()
    {
        return developerID;
    }

    public String getClientEnvironment()
    {
        return clientEnvironment;
    }

    public String getClientApplicationVersion()
    {
        return clientApplicationVersion;
    }

    public String getClientLibrary()
    {
        return clientLibrary;
    }

    public String getClientLibraryVersion()
    {
        return clientLibraryVersion;
    }
}
