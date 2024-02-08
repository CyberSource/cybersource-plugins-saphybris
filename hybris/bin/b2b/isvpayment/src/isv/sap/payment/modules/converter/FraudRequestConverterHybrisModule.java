package isv.sap.payment.modules.converter;

import javax.annotation.Resource;

import org.springframework.guice.annotation.EnableGuiceModules;

import isv.cjl.module.converter.FraudRequestConverterModule;
import isv.cjl.payment.data.Converter;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.request.Request;

import static com.google.inject.name.Names.named;
import static isv.cjl.module.util.ModuleUtil.CONVERTER;
import static isv.cjl.module.util.RequestConverterConstants.Fraud.ACCOUNT_TAKEOVER_PROTECTION;
import static isv.cjl.module.util.RequestConverterConstants.Fraud.ADVANCED_SCREEN;

@EnableGuiceModules
public class FraudRequestConverterHybrisModule extends FraudRequestConverterModule
{
    @Resource(name = "isv.sap.payment.converter.fraud.accountTakeoverProtectionRequestConverter")
    private Converter<PaymentServiceRequest, Request> accountTakeoverProtectionRequestConverter;

    @Resource(name = "isv.sap.payment.converter.fraud.advancedFraudScreenRequestConverter")
    private Converter<PaymentServiceRequest, Request> advancedFraudScreenRequestConverter;

    @Override
    protected void configure()
    {
        bind(CONVERTER).annotatedWith(named(ACCOUNT_TAKEOVER_PROTECTION))
                .toInstance(accountTakeoverProtectionRequestConverter);
        bind(CONVERTER).annotatedWith(named(ADVANCED_SCREEN)).toInstance(advancedFraudScreenRequestConverter);
    }
}
