package isv.cjl.payment.utils;

import isv.cjl.payment.configuration.service.ConfigurationService;
import java.io.File;
import javax.naming.ConfigurationException;

public class PropertiesUtil {
    public PropertiesUtil() {
    }

    public static String getKeyFilePath(ConfigurationService configurationService) throws ConfigurationException {
        String keyFile = configurationService.getString("isv.payment.p12.keyFile");
        String keyDirectory = configurationService.getString("isv.payment.p12.keysDirectory");
        File file;
        if (!keyFile.endsWith(".p12")) {
            file = new File(keyDirectory, keyFile + ".p12");
        } else {
            file = new File(keyDirectory, keyFile);
        }

        String fullPath = file.getAbsolutePath();
        if (!file.isFile()) {
            throw new ConfigurationException("The file \"" + fullPath + "\" is missing or is not a file.");
        } else if (!file.canRead()) {
            throw new ConfigurationException("This application does not have permission to read the file \"" + fullPath + "\".");
        } else {
            return fullPath;
        }
    }
}
