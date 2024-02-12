package isv.sap.payment.modules.converter;

import javax.annotation.Resource;

import org.springframework.guice.annotation.EnableGuiceModules;

import isv.cjl.module.converter.PayPalRequestConverterModule;
import isv.cjl.payment.data.Converter;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.request.Request;

import static com.google.inject.name.Names.named;
import static isv.cjl.module.util.ModuleUtil.CONVERTER;
import static isv.cjl.module.util.RequestConverterConstants.PayPal.AUTHORIZATION;
import static isv.cjl.module.util.RequestConverterConstants.PayPal.AUTHORIZATION_REVERSAL;
import static isv.cjl.module.util.RequestConverterConstants.PayPal.BILLING_AGREEMENT;
import static isv.cjl.module.util.RequestConverterConstants.PayPal.CANCEL_ORDER;
import static isv.cjl.module.util.RequestConverterConstants.PayPal.CAPTURE;
import static isv.cjl.module.util.RequestConverterConstants.PayPal.CHECK_STATUS;
import static isv.cjl.module.util.RequestConverterConstants.PayPal.CREATE_BILLING_AGREEMENT_SESSION;
import static isv.cjl.module.util.RequestConverterConstants.PayPal.CREATE_SESSION;
import static isv.cjl.module.util.RequestConverterConstants.PayPal.ORDER_SETUP;
import static isv.cjl.module.util.RequestConverterConstants.PayPal.REFUND;
import static isv.cjl.module.util.RequestConverterConstants.PayPal.SALE;

@EnableGuiceModules
public class PayPalRequestConverterHybrisModule extends PayPalRequestConverterModule
{
    @Resource(name = "isv.sap.payment.converter.paypal.createSessionRequestConverter")
    private Converter<PaymentServiceRequest, Request> createSessionRequestConverter;

    @Resource(name = "isv.sap.payment.converter.paypal.createBillingAgreementSessionRequestConverter")
    private Converter<PaymentServiceRequest, Request> createBillingAgreementSessionRequestConverter;

    @Resource(name = "isv.sap.payment.converter.paypal.checkStatusRequestConverter")
    private Converter<PaymentServiceRequest, Request> checkStatusRequestConverter;

    @Resource(name = "isv.sap.payment.converter.paypal.authorizationRequestConverter")
    private Converter<PaymentServiceRequest, Request> authorizationRequestConverter;

    @Resource(name = "isv.sap.payment.converter.paypal.authorizationReversalRequestConverter")
    private Converter<PaymentServiceRequest, Request> authorizationReversalRequestConverter;

    @Resource(name = "isv.sap.payment.converter.paypal.orderSetupRequestConverter")
    private Converter<PaymentServiceRequest, Request> orderSetupRequestConverter;

    @Resource(name = "isv.sap.payment.converter.paypal.captureRequestConverter")
    private Converter<PaymentServiceRequest, Request> captureRequestConverter;

    @Resource(name = "isv.sap.payment.converter.paypal.saleRequestConverter")
    private Converter<PaymentServiceRequest, Request> saleRequestConverter;

    @Resource(name = "isv.sap.payment.converter.paypal.refundRequestConverter")
    private Converter<PaymentServiceRequest, Request> refundRequestConverter;

    @Resource(name = "isv.sap.payment.converter.paypal.cancelOrderRequestConverter")
    private Converter<PaymentServiceRequest, Request> cancelOrderRequestConverter;

    @Resource(name = "isv.sap.payment.converter.paypal.billingAgreementRequestConverter")
    private Converter<PaymentServiceRequest, Request> billingAgreementRequestConverter;

    @Override
    protected void configure()
    {
        bind(CONVERTER).annotatedWith(named(CREATE_SESSION)).toInstance(createSessionRequestConverter);
        bind(CONVERTER).annotatedWith(named(CREATE_BILLING_AGREEMENT_SESSION))
                .toInstance(createBillingAgreementSessionRequestConverter);
        bind(CONVERTER).annotatedWith(named(CHECK_STATUS)).toInstance(checkStatusRequestConverter);
        bind(CONVERTER).annotatedWith(named(AUTHORIZATION)).toInstance(authorizationRequestConverter);
        bind(CONVERTER).annotatedWith(named(AUTHORIZATION_REVERSAL)).toInstance(authorizationReversalRequestConverter);
        bind(CONVERTER).annotatedWith(named(ORDER_SETUP)).toInstance(orderSetupRequestConverter);
        bind(CONVERTER).annotatedWith(named(CAPTURE)).toInstance(captureRequestConverter);
        bind(CONVERTER).annotatedWith(named(SALE)).toInstance(saleRequestConverter);
        bind(CONVERTER).annotatedWith(named(REFUND)).toInstance(refundRequestConverter);
        bind(CONVERTER).annotatedWith(named(CANCEL_ORDER)).toInstance(cancelOrderRequestConverter);
        bind(CONVERTER).annotatedWith(named(BILLING_AGREEMENT)).toInstance(billingAgreementRequestConverter);
    }
}
