package isv.sap.payment.modules.converter;

import javax.annotation.Resource;

import org.springframework.guice.annotation.EnableGuiceModules;

import isv.cjl.module.converter.KlarnaRequestConverterModule;
import isv.cjl.payment.data.Converter;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.request.Request;

import static com.google.inject.name.Names.named;
import static isv.cjl.module.util.ModuleUtil.CONVERTER;
import static isv.cjl.module.util.RequestConverterConstants.Klarna.AUTHORIZATION;
import static isv.cjl.module.util.RequestConverterConstants.Klarna.AUTHORIZATION_REVERSAL;
import static isv.cjl.module.util.RequestConverterConstants.Klarna.CAPTURE;

@EnableGuiceModules
public class KlarnaRequestConverterHybrisModule extends KlarnaRequestConverterModule
{
    @Resource(name = "isv.sap.payment.converter.alternative.captureRequestConverter")
    private Converter<PaymentServiceRequest, Request> captureRequestConverter;

    @Resource(name = "isv.sap.payment.converter.alternative.authorizationRequestConverter")
    private Converter<PaymentServiceRequest, Request> authorizationRequestConverter;

    @Resource(name = "isv.sap.payment.converter.alternative.authorizationReversalRequestConverter")
    private Converter<PaymentServiceRequest, Request> authorizationReversalRequestConverter;

    @Override
    protected void configure()
    {
        bind(CONVERTER).annotatedWith(named(CAPTURE)).toInstance(captureRequestConverter);
        bind(CONVERTER).annotatedWith(named(AUTHORIZATION)).toInstance(authorizationRequestConverter);
        bind(CONVERTER).annotatedWith(named(AUTHORIZATION_REVERSAL)).toInstance(authorizationReversalRequestConverter);
    }
}
