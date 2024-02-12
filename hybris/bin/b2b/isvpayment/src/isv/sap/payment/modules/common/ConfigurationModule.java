package isv.sap.payment.modules.common;

import javax.annotation.Resource;

import org.springframework.guice.annotation.EnableGuiceModules;

import isv.cjl.payment.configuration.service.impl.DefaultConfigurationService;
import isv.cjl.payment.data.PaymentSystemInfo;

@EnableGuiceModules
public class ConfigurationModule extends isv.cjl.module.common.ConfigurationModule
{
    @Resource(name = "isv.sap.payment.paymentSystemInfo")
    private isv.sap.payment.data.PaymentSystemInfo paymentSystemInfo;

    @Override
    protected PaymentSystemInfo createPaymentSystemInfo(final DefaultConfigurationService configService)
    {
        return new PaymentSystemInfo(
                paymentSystemInfo.getDeveloperID(),
                paymentSystemInfo.getClientLibrary(),
                paymentSystemInfo.getClientEnvironment(),
                paymentSystemInfo.getClientLibraryVersion(),
                paymentSystemInfo.getClientApplicationVersion(),
                paymentSystemInfo.getPartnerSolutionID()
        );
    }
}
