package isv.sap.payment.modules.converter;

import javax.annotation.Resource;

import org.springframework.guice.annotation.EnableGuiceModules;

import isv.cjl.module.converter.ReportingTransactionRequestConverterModule;
import isv.cjl.payment.data.Converter;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.request.Request;

import static com.google.inject.name.Names.named;
import static isv.cjl.module.util.ModuleUtil.CONVERTER;
import static isv.cjl.module.util.RequestConverterConstants.ApplePay;
import static isv.cjl.module.util.RequestConverterConstants.CreditCard;
import static isv.cjl.module.util.RequestConverterConstants.PayPal;

@EnableGuiceModules
public class ReportingTransactionRequestConverterHybrisModule extends ReportingTransactionRequestConverterModule
{
    @Resource(name = "isv.sap.payment.converter.reporting.reportingTransactionConverter")
    private Converter<PaymentServiceRequest, Request> reportingTransactionConverter;

    @Override
    protected void configure()
    {
        bind(CONVERTER).annotatedWith(named(CreditCard.REPORTING_AUTHORIZATION))
                .toInstance(reportingTransactionConverter);
        bind(CONVERTER).annotatedWith(named(CreditCard.REPORTING_CAPTURE)).toInstance(reportingTransactionConverter);
        bind(CONVERTER).annotatedWith(named(PayPal.REPORTING_CAPTURE)).toInstance(reportingTransactionConverter);
        bind(CONVERTER).annotatedWith(named(PayPal.PAYPAL_SO_REPORTING_CAPTURE))
                .toInstance(reportingTransactionConverter);
        bind(CONVERTER).annotatedWith(named(ApplePay.APPLE_PAY_REPORTING_CAPTURE))
                .toInstance(reportingTransactionConverter);
    }
}
