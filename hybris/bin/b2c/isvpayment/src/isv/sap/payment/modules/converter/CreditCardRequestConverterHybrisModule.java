package isv.sap.payment.modules.converter;

import javax.annotation.Resource;

import org.springframework.guice.annotation.EnableGuiceModules;

import isv.cjl.module.converter.CreditCardRequestConverterModule;
import isv.cjl.payment.data.Converter;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.request.Request;

import static com.google.inject.name.Names.named;
import static isv.cjl.module.util.ModuleUtil.CONVERTER;
import static isv.cjl.module.util.RequestConverterConstants.CreditCard.AUTHORIZATION;
import static isv.cjl.module.util.RequestConverterConstants.CreditCard.AUTHORIZATION_REVERSAL;
import static isv.cjl.module.util.RequestConverterConstants.CreditCard.CAPTURE;
import static isv.cjl.module.util.RequestConverterConstants.CreditCard.ENROLLMENT;
import static isv.cjl.module.util.RequestConverterConstants.CreditCard.REFUND_FOLLOW_ON;
import static isv.cjl.module.util.RequestConverterConstants.CreditCard.REFUND_STANDALONE;
import static isv.cjl.module.util.RequestConverterConstants.CreditCard.SA_AUTHORIZATION;
import static isv.cjl.module.util.RequestConverterConstants.CreditCard.TAX;
import static isv.cjl.module.util.RequestConverterConstants.CreditCard.TOKEN_CREATE;
import static isv.cjl.module.util.RequestConverterConstants.CreditCard.TOKEN_DELETE;
import static isv.cjl.module.util.RequestConverterConstants.CreditCard.VALIDATE;
import static isv.cjl.module.util.RequestConverterConstants.CreditCard.VOID;

@EnableGuiceModules
public class CreditCardRequestConverterHybrisModule extends CreditCardRequestConverterModule
{
    @Resource(name = "isv.sap.payment.converter.creditcard.voidRequestConverter")
    private Converter<PaymentServiceRequest, Request> voidRequestConverter;

    @Resource(name = "isv.sap.payment.converter.creditcard.captureRequestConverter")
    private Converter<PaymentServiceRequest, Request> captureRequestConverter;

    @Resource(name = "isv.sap.payment.converter.creditcard.paymentTokenCreateRequestConverter")
    private Converter<PaymentServiceRequest, Request> paymentTokenCreateRequestConverter;

    @Resource(name = "isv.sap.payment.converter.creditcard.paymentTokenDeleteRequestConverter")
    private Converter<PaymentServiceRequest, Request> paymentTokenDeleteRequestConverter;

    @Resource(name = "isv.sap.payment.converter.creditcard.refundFollowOnRequestConverter")
    private Converter<PaymentServiceRequest, Request> refundFollowOnRequestConverter;

    @Resource(name = "isv.sap.payment.converter.creditcard.saAuthorizationRequestConverter")
    private Converter<PaymentServiceRequest, Request> saAuthorizationRequestConverter;

    @Resource(name = "isv.sap.payment.converter.creditcard.authorizationRequestConverter")
    private Converter<PaymentServiceRequest, Request> authorizationRequestConverter;

    @Resource(name = "isv.sap.payment.converter.creditcard.authorizationReversalRequestConverter")
    private Converter<PaymentServiceRequest, Request> authorizationReversalRequestConverter;

    @Resource(name = "isv.sap.payment.converter.creditcard.refundStandaloneRequestConverter")
    private Converter<PaymentServiceRequest, Request> refundStandaloneRequestConverter;

    @Resource(name = "isv.sap.payment.converter.creditcard.enrollmentRequestConverter")
    private Converter<PaymentServiceRequest, Request> enrollmentRequestConverter;

    @Resource(name = "isv.sap.payment.converter.creditcard.validateRequestConverter")
    private Converter<PaymentServiceRequest, Request> validateRequestConverter;

    @Resource(name = "isv.sap.payment.converter.creditcard.taxRequestConverter")
    private Converter<PaymentServiceRequest, Request> taxRequestConverter;

    @Override
    protected void configure()
    {
        bind(CONVERTER).annotatedWith(named(VOID)).toInstance(voidRequestConverter);
        bind(CONVERTER).annotatedWith(named(AUTHORIZATION)).toInstance(authorizationRequestConverter);
        bind(CONVERTER).annotatedWith(named(AUTHORIZATION_REVERSAL)).toInstance(authorizationReversalRequestConverter);
        bind(CONVERTER).annotatedWith(named(SA_AUTHORIZATION)).toInstance(saAuthorizationRequestConverter);
        bind(CONVERTER).annotatedWith(named(CAPTURE)).toInstance(captureRequestConverter);
        bind(CONVERTER).annotatedWith(named(TOKEN_CREATE)).toInstance(paymentTokenCreateRequestConverter);
        bind(CONVERTER).annotatedWith(named(TOKEN_DELETE)).toInstance(paymentTokenDeleteRequestConverter);
        bind(CONVERTER).annotatedWith(named(REFUND_FOLLOW_ON)).toInstance(refundFollowOnRequestConverter);
        bind(CONVERTER).annotatedWith(named(REFUND_STANDALONE)).toInstance(refundStandaloneRequestConverter);
        bind(CONVERTER).annotatedWith(named(ENROLLMENT)).toInstance(enrollmentRequestConverter);
        bind(CONVERTER).annotatedWith(named(VALIDATE)).toInstance(validateRequestConverter);
        bind(CONVERTER).annotatedWith(named(TAX)).toInstance(taxRequestConverter);
    }
}
