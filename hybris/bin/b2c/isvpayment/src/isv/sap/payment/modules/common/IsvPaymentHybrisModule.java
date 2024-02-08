package isv.sap.payment.modules.common;

import java.util.List;
import javax.annotation.Resource;

import com.cybersource.flex.sdk.Credentials;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import org.springframework.guice.annotation.EnableGuiceModules;

import isv.cjl.module.PaymentModule;
import isv.cjl.payment.configuration.resolver.ConfigurationResolver;
import isv.cjl.payment.executor.WrapperExecutor;
import isv.cjl.payment.schemas.ReplyMessage;
import isv.cjl.payment.service.MerchantService;
import isv.cjl.payment.service.executor.PaymentServiceExecutor;
import isv.cjl.payment.service.executor.request.populator.Populator;
import isv.sap.payment.configuration.resolver.IsvConfigurationResolver;
import isv.sap.payment.hystrix.TenantAwareHystrixWrapperExecutor;
import isv.sap.payment.service.executor.CorePaymentServiceExecutor;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.inject.name.Names.named;
import static isv.cjl.module.util.ConfigurationConstants.PAYMENT_WRAPPER_EXECUTOR;
import static isv.cjl.module.util.ConfigurationConstants.REPORT_WRAPPER_EXECUTOR;
import static isv.cjl.payment.constants.PaymentConstants.ConfigurationResolvers.CUSTOM;

@EnableGuiceModules
public class IsvPaymentHybrisModule extends PaymentModule
{
    @Resource(name = "isv.sap.payment.merchantService")
    private MerchantService merchantService;

    @Resource(name = "isv.sap.payment.populator.l2.amexDirectPopulator")
    private Populator amexDirectPopulatorL2;

    @Resource(name = "isv.sap.payment.populator.l2.omniPayMastercardPopulator")
    private Populator omniPayMastercardPopulatorL2;

    @Resource(name = "isv.sap.payment.populator.l2.omniPayVisaPopulator")
    private Populator omniPayVisaPopulatorL2;

    @Resource(name = "isv.sap.payment.populator.l3.omniPayMastercardPopulator")
    private Populator omniPayMastercardPopulatorL3;

    @Resource(name = "isv.sap.payment.populator.l3.omniPayVisaPopulator")
    private Populator omniPayVisaPopulatorL3;

    @Resource(name = "isv.sap.payment.flexCredentials")
    private Credentials flexCredentials;

    @Override
    protected void configure()
    {
        bind(PaymentServiceExecutor.class).to(CorePaymentServiceExecutor.class).in(Singleton.class);
        bind(MerchantService.class).toInstance(merchantService);
        bind(ConfigurationResolver.class).annotatedWith(named(CUSTOM)).to(IsvConfigurationResolver.class)
                .in(Singleton.class);

        bind(Key.get(new TypeLiteral<WrapperExecutor<String, ReplyMessage>>()
        {
        }, Names.named(PAYMENT_WRAPPER_EXECUTOR)))
                .toInstance(new TenantAwareHystrixWrapperExecutor<>("isvPaymentGroup"));
        bind(Key.get(new TypeLiteral<WrapperExecutor<String, String>>()
        {
        }, named(REPORT_WRAPPER_EXECUTOR)))
                .toInstance(new TenantAwareHystrixWrapperExecutor<String>("isvReportingGroup"));

        bind(Credentials.class).annotatedWith(named("isv.sap.payment.flexCredentials"))
                .toInstance(flexCredentials);
    }

    @Provides
    protected List<Populator> createDefaultRepositoryPopulators()
    {
        return newArrayList(amexDirectPopulatorL2,
                omniPayMastercardPopulatorL2,
                omniPayVisaPopulatorL2,
                omniPayMastercardPopulatorL3,
                omniPayVisaPopulatorL3);
    }
}
