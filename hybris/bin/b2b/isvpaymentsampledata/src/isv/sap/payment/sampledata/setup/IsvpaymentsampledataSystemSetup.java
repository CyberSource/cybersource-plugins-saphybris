package isv.sap.payment.sampledata.setup;

import java.io.IOException;

import de.hybris.platform.commerceservices.setup.SetupImpexService;
import de.hybris.platform.core.initialization.SystemSetup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import isv.sap.payment.sampledata.constants.IsvpaymentsampledataConstants;

@SystemSetup(extension = IsvpaymentsampledataConstants.EXTENSIONNAME)
public class IsvpaymentsampledataSystemSetup
{
    public static final String DATA_FOLDER = "/isvpaymentsampledata/";

    @Autowired
    private SetupImpexService setupImpexService;

    @SystemSetup(process = SystemSetup.Process.ALL, type = SystemSetup.Type.PROJECT)
    public void createEssentialData() throws IOException
    {
        final ClassLoader classLoader = this.getClass().getClassLoader();
        final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);
        importCommonData(resolver);
        importData(resolver);
    }

    private void importCommonData(final ResourcePatternResolver resolver) throws IOException
    {
        final Resource[] resources = resolver.getResources("classpath*:" + DATA_FOLDER + "**/common/*.impex");
        importResources(resources);
    }

    private void importData(final ResourcePatternResolver resolver) throws IOException
    {
        final Resource[] resources = resolver.getResources("classpath*:" + DATA_FOLDER + "**/*.impex");
        importResources(resources);
    }

    private void importResources(final Resource[] resources) throws IOException
    {
        for (final Resource resource : resources)
        {
            setupImpexService.importImpexFile(toRelativePath(resource.getFile().getPath()), true);
        }
    }

    private String toRelativePath(final String filePath)
    {
        return filePath.substring(filePath.lastIndexOf(DATA_FOLDER));
    }
}
