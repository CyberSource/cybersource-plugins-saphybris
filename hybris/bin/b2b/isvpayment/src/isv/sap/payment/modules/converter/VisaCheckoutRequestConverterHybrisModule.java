package isv.sap.payment.modules.converter;

import javax.annotation.Resource;

import org.springframework.guice.annotation.EnableGuiceModules;

import isv.cjl.module.converter.VisaCheckoutRequestConverterModule;
import isv.cjl.payment.data.Converter;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.request.Request;

import static com.google.inject.name.Names.named;
import static isv.cjl.module.util.ModuleUtil.CONVERTER;
import static isv.cjl.module.util.RequestConverterConstants.VisaCheckout.AUTHORIZATION;
import static isv.cjl.module.util.RequestConverterConstants.VisaCheckout.AUTHORIZATION_REVERSAL;
import static isv.cjl.module.util.RequestConverterConstants.VisaCheckout.CAPTURE;
import static isv.cjl.module.util.RequestConverterConstants.VisaCheckout.ENROLLMENT;
import static isv.cjl.module.util.RequestConverterConstants.VisaCheckout.GET;
import static isv.cjl.module.util.RequestConverterConstants.VisaCheckout.REFUND;
import static isv.cjl.module.util.RequestConverterConstants.VisaCheckout.VALIDATE;
import static isv.cjl.module.util.RequestConverterConstants.VisaCheckout.VOID;

@EnableGuiceModules
public class VisaCheckoutRequestConverterHybrisModule extends VisaCheckoutRequestConverterModule
{
    @Resource(name = "isv.sap.payment.converter.visacheckout.voidRequestConverter")
    private Converter<PaymentServiceRequest, Request> voidRequestConverter;

    @Resource(name = "isv.sap.payment.converter.visacheckout.captureRequestConverter")
    private Converter<PaymentServiceRequest, Request> captureRequestConverter;

    @Resource(name = "isv.sap.payment.converter.visacheckout.getRequestConverter")
    private Converter<PaymentServiceRequest, Request> getRequestConverter;

    @Resource(name = "isv.sap.payment.converter.visacheckout.refundRequestConverter")
    private Converter<PaymentServiceRequest, Request> refundRequestConverter;

    @Resource(name = "isv.sap.payment.converter.visacheckout.authorizationRequestConverter")
    private Converter<PaymentServiceRequest, Request> authorizationRequestConverter;

    @Resource(name = "isv.sap.payment.converter.visacheckout.authorizationReversalRequestConverter")
    private Converter<PaymentServiceRequest, Request> authorizationReversalRequestConverter;

    @Resource(name = "isv.sap.payment.converter.visacheckout.enrollmentRequestConverter")
    private Converter<PaymentServiceRequest, Request> enrollmentRequestConverter;

    @Resource(name = "isv.sap.payment.converter.visacheckout.validateRequestConverter")
    private Converter<PaymentServiceRequest, Request> validateRequestConverter;

    @Override
    protected void configure()
    {
        bind(CONVERTER).annotatedWith(named(GET)).toInstance(getRequestConverter);
        bind(CONVERTER).annotatedWith(named(CAPTURE)).toInstance(captureRequestConverter);
        bind(CONVERTER).annotatedWith(named(REFUND)).toInstance(refundRequestConverter);
        bind(CONVERTER).annotatedWith(named(VOID)).toInstance(voidRequestConverter);
        bind(CONVERTER).annotatedWith(named(AUTHORIZATION)).toInstance(authorizationRequestConverter);
        bind(CONVERTER).annotatedWith(named(AUTHORIZATION_REVERSAL)).toInstance(authorizationReversalRequestConverter);
        bind(CONVERTER).annotatedWith(named(ENROLLMENT)).toInstance(enrollmentRequestConverter);
        bind(CONVERTER).annotatedWith(named(VALIDATE)).toInstance(validateRequestConverter);
    }
}
