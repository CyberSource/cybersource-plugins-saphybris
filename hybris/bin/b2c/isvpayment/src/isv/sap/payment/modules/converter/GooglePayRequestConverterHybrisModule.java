package isv.sap.payment.modules.converter;

import javax.annotation.Resource;

import com.google.inject.AbstractModule;
import org.springframework.guice.annotation.EnableGuiceModules;

import isv.cjl.payment.data.Converter;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.request.Request;

import static com.google.inject.name.Names.named;
import static isv.cjl.module.util.ModuleUtil.CONVERTER;
import static isv.cjl.module.util.RequestConverterConstants.GooglePay.AUTHORIZATION;
import static isv.cjl.module.util.RequestConverterConstants.GooglePay.AUTHORIZATION_REVERSAL;
import static isv.cjl.module.util.RequestConverterConstants.GooglePay.CAPTURE;
import static isv.cjl.module.util.RequestConverterConstants.GooglePay.REFUND_FOLLOW_ON;
import static isv.cjl.module.util.RequestConverterConstants.GooglePay.SALE;

@EnableGuiceModules
public class GooglePayRequestConverterHybrisModule extends AbstractModule
{
    @Resource(name = "isv.sap.payment.service.executor.request.converter.googlepay.authorizationRequestConverter")
    private Converter<PaymentServiceRequest, Request> authorizationRequestConverter;

    @Resource(name = "isv.sap.payment.converter.creditcard.captureRequestConverter")
    private Converter<PaymentServiceRequest, Request> captureRequestConverter;

    @Resource(name = "isv.sap.payment.converter.creditcard.authorizationReversalRequestConverter")
    private Converter<PaymentServiceRequest, Request> authorizationReversalRequestConverter;

    @Resource(name = "isv.sap.payment.converter.creditcard.refundFollowOnRequestConverter")
    private Converter<PaymentServiceRequest, Request> refundFollowOnRequestConverter;

    @Resource(name = "isv.sap.payment.service.executor.request.converter.googlepay.saleRequestConverter")
    private Converter<PaymentServiceRequest, Request> saleRequestConverter;

    @Override
    protected void configure()
    {
        bind(CONVERTER).annotatedWith(named(AUTHORIZATION)).toInstance(authorizationRequestConverter);
        bind(CONVERTER).annotatedWith(named(CAPTURE)).toInstance(captureRequestConverter);
        bind(CONVERTER).annotatedWith(named(AUTHORIZATION_REVERSAL)).toInstance(authorizationReversalRequestConverter);
        bind(CONVERTER).annotatedWith(named(REFUND_FOLLOW_ON)).toInstance(refundFollowOnRequestConverter);
        bind(CONVERTER).annotatedWith(named(SALE)).toInstance(saleRequestConverter);
    }
}
