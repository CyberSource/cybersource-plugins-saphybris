package isv.sap.payment.modules.converter;

import javax.annotation.Resource;

import org.springframework.guice.annotation.EnableGuiceModules;

import isv.cjl.module.converter.AlternativePaymentRequestConverterModule;
import isv.cjl.payment.data.Converter;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.request.Request;

import static com.google.inject.name.Names.named;
import static isv.cjl.module.util.ModuleUtil.CONVERTER;
import static isv.cjl.module.util.RequestConverterConstants.AlternativePayment.CHECK_STATUS;
import static isv.cjl.module.util.RequestConverterConstants.AlternativePayment.CREATE_SESSION;
import static isv.cjl.module.util.RequestConverterConstants.AlternativePayment.INITIATE;
import static isv.cjl.module.util.RequestConverterConstants.AlternativePayment.OPTIONS;
import static isv.cjl.module.util.RequestConverterConstants.AlternativePayment.REFUND;
import static isv.cjl.module.util.RequestConverterConstants.AlternativePayment.SALE;
import static isv.cjl.module.util.RequestConverterConstants.AlternativePayment.UPDATE_SESSION;

@EnableGuiceModules
public class AlternativePaymentRequestConverterHybrisModule extends AlternativePaymentRequestConverterModule
{
    @Resource(name = "isv.sap.payment.converter.alternative.saleRequestConverter")
    private Converter<PaymentServiceRequest, Request> saleRequestConverter;

    @Resource(name = "isv.sap.payment.converter.alternative.refundRequestConverter")
    private Converter<PaymentServiceRequest, Request> refundRequestConverter;

    @Resource(name = "isv.sap.payment.converter.alternative.optionsRequestConverter")
    private Converter<PaymentServiceRequest, Request> optionsRequestConverter;

    @Resource(name = "isv.sap.payment.converter.alternative.initiateRequestConverter")
    private Converter<PaymentServiceRequest, Request> initiateRequestConverter;

    @Resource(name = "isv.sap.payment.converter.alternative.checkStatusRequestConverter")
    private Converter<PaymentServiceRequest, Request> checkStatusRequestConverter;

    @Resource(name = "isv.sap.payment.converter.alternative.createSessionRequestConverter")
    private Converter<PaymentServiceRequest, Request> createSessionRequestConverter;

    @Resource(name = "isv.sap.payment.converter.alternative.updateSessionRequestConverter")
    private Converter<PaymentServiceRequest, Request> updateSessionRequestConverter;

    @Override
    protected void configure()
    {
        bind(CONVERTER).annotatedWith(named(SALE)).toInstance(saleRequestConverter);
        bind(CONVERTER).annotatedWith(named(REFUND)).toInstance(refundRequestConverter);
        bind(CONVERTER).annotatedWith(named(OPTIONS)).toInstance(optionsRequestConverter);
        bind(CONVERTER).annotatedWith(named(INITIATE)).toInstance(initiateRequestConverter);
        bind(CONVERTER).annotatedWith(named(CHECK_STATUS)).toInstance(checkStatusRequestConverter);
        bind(CONVERTER).annotatedWith(named(CREATE_SESSION)).toInstance(createSessionRequestConverter);
        bind(CONVERTER).annotatedWith(named(UPDATE_SESSION)).toInstance(updateSessionRequestConverter);
    }
}
