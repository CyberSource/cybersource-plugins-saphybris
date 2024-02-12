package isv.sap.payment.integration.helpers

import org.springframework.guice.annotation.EnableGuiceModules

import isv.cjl.module.PaymentModule
import isv.cjl.payment.configuration.resolver.ConfigurationResolver
import isv.cjl.payment.service.MerchantService
import isv.cjl.payment.service.executor.PaymentServiceExecutor
import isv.sap.payment.configuration.resolver.IsvConfigurationResolver
import isv.sap.payment.service.executor.CorePaymentServiceExecutor

import static com.google.inject.name.Names.named
import static isv.cjl.payment.constants.PaymentConstants.ConfigurationResolvers.CUSTOM

@EnableGuiceModules
class TestModule extends PaymentModule
{
    @Override
    protected void configure()
    {
        bind(PaymentServiceExecutor).to(CorePaymentServiceExecutor).in(Singleton)

        bind(MerchantService).to(MockMerchantService).in(Singleton)

        bind(ConfigurationResolver).annotatedWith(named(CUSTOM)).to(IsvConfigurationResolver).in(Singleton)
    }
}
