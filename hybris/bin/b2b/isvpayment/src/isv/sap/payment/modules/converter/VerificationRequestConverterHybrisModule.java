package isv.sap.payment.modules.converter;

import javax.annotation.Resource;

import org.springframework.guice.annotation.EnableGuiceModules;

import isv.cjl.module.converter.VerificationRequestConverterModule;
import isv.cjl.payment.data.Converter;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.request.Request;

import static com.google.inject.name.Names.named;
import static isv.cjl.module.util.ModuleUtil.CONVERTER;
import static isv.cjl.module.util.RequestConverterConstants.Verification.DELIVERY_ADDRESS;
import static isv.cjl.module.util.RequestConverterConstants.Verification.EXPORT_COMPLIANCE;

@EnableGuiceModules
public class VerificationRequestConverterHybrisModule extends VerificationRequestConverterModule
{
    @Resource(name = "isv.sap.payment.converter.verification.deliveryAddressVerificationRequestConverter")
    private Converter<PaymentServiceRequest, Request> deliveryAddressVerificationRequestConverter;

    @Resource(name = "isv.sap.payment.converter.verification.exportComplianceRequestConverter")
    private Converter<PaymentServiceRequest, Request> exportComplianceRequestConverter;

    @Override
    protected void configure()
    {
        bind(CONVERTER).annotatedWith(named(DELIVERY_ADDRESS)).toInstance(deliveryAddressVerificationRequestConverter);
        bind(CONVERTER).annotatedWith(named(EXPORT_COMPLIANCE)).toInstance(exportComplianceRequestConverter);
    }
}
