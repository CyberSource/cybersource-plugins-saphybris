package isv.sap.payment.modules.converter;

import javax.annotation.Resource;

import org.springframework.guice.annotation.EnableGuiceModules;

import isv.cjl.module.converter.PayPalSORequestConverterModule;
import isv.cjl.payment.data.Converter;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.request.Request;

import static com.google.inject.name.Names.named;
import static isv.cjl.module.util.ModuleUtil.CONVERTER;
import static isv.cjl.module.util.RequestConverterConstants.PayPal.PAYPAL_SO_AUTHORIZATION;
import static isv.cjl.module.util.RequestConverterConstants.PayPal.PAYPAL_SO_AUTHORIZATION_REVERSAL;
import static isv.cjl.module.util.RequestConverterConstants.PayPal.PAYPAL_SO_CAPTURE;
import static isv.cjl.module.util.RequestConverterConstants.PayPal.PAYPAL_SO_GET;
import static isv.cjl.module.util.RequestConverterConstants.PayPal.PAYPAL_SO_ORDER_SETUP;
import static isv.cjl.module.util.RequestConverterConstants.PayPal.PAYPAL_SO_ORDER_SETUP_REVERSAL;
import static isv.cjl.module.util.RequestConverterConstants.PayPal.PAYPAL_SO_REFUND;
import static isv.cjl.module.util.RequestConverterConstants.PayPal.PAYPAL_SO_SET;

@EnableGuiceModules
public class PayPalSORequestConverterHybrisModule extends PayPalSORequestConverterModule
{
    @Resource(name = "isv.sap.payment.converter.paypalso.getRequestConverter")
    private Converter<PaymentServiceRequest, Request> getRequestConverter;

    @Resource(name = "isv.sap.payment.converter.paypalso.setRequestConverter")
    private Converter<PaymentServiceRequest, Request> setRequestConverter;

    @Resource(name = "isv.sap.payment.converter.paypalso.refundRequestConverter")
    private Converter<PaymentServiceRequest, Request> refundRequestConverter;

    @Resource(name = "isv.sap.payment.converter.paypalso.captureRequestConverter")
    private Converter<PaymentServiceRequest, Request> captureRequestConverter;

    @Resource(name = "isv.sap.payment.converter.paypalso.orderSetupRequestConverter")
    private Converter<PaymentServiceRequest, Request> orderSetupRequestConverter;

    @Resource(name = "isv.sap.payment.converter.paypalso.orderSetupReversalRequestConverter")
    private Converter<PaymentServiceRequest, Request> orderSetupReversalRequestConverter;

    @Resource(name = "isv.sap.payment.converter.paypalso.authorizationRequestConverter")
    private Converter<PaymentServiceRequest, Request> authorizationRequestConverter;

    @Resource(name = "isv.sap.payment.converter.paypalso.authorizationReversalRequestConverter")
    private Converter<PaymentServiceRequest, Request> authorizationReversalRequestConverter;

    @Override
    protected void configure()
    {
        bind(CONVERTER).annotatedWith(named(PAYPAL_SO_GET)).toInstance(getRequestConverter);
        bind(CONVERTER).annotatedWith(named(PAYPAL_SO_SET)).toInstance(setRequestConverter);
        bind(CONVERTER).annotatedWith(named(PAYPAL_SO_REFUND)).toInstance(refundRequestConverter);
        bind(CONVERTER).annotatedWith(named(PAYPAL_SO_CAPTURE)).toInstance(captureRequestConverter);
        bind(CONVERTER).annotatedWith(named(PAYPAL_SO_ORDER_SETUP)).toInstance(orderSetupRequestConverter);
        bind(CONVERTER).annotatedWith(named(PAYPAL_SO_ORDER_SETUP_REVERSAL))
                .toInstance(orderSetupReversalRequestConverter);
        bind(CONVERTER).annotatedWith(named(PAYPAL_SO_AUTHORIZATION)).toInstance(authorizationRequestConverter);
        bind(CONVERTER).annotatedWith(named(PAYPAL_SO_AUTHORIZATION_REVERSAL))
                .toInstance(authorizationReversalRequestConverter);
    }
}
