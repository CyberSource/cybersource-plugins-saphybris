package isv.sap.payment.modules.converter;

import java.util.List;
import javax.annotation.Resource;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import org.springframework.guice.annotation.EnableGuiceModules;

import isv.cjl.payment.data.Converter;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.request.Request;
import isv.sap.payment.service.executor.request.converter.applepay.strategies.AmexMerchDecryptionStrategy;
import isv.sap.payment.service.executor.request.converter.applepay.strategies.AuthorizationRequestConverterStrategy;
import isv.sap.payment.service.executor.request.converter.applepay.strategies.DecryptionAuthRequestConverterStrategy;
import isv.sap.payment.service.executor.request.converter.applepay.strategies.DiscoverMerchDecryptionStrategy;
import isv.sap.payment.service.executor.request.converter.applepay.strategies.MasterCardMerchDecryptionStrategy;
import isv.sap.payment.service.executor.request.converter.applepay.strategies.VisaMerchDecryptionStrategy;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.inject.name.Names.named;
import static isv.cjl.module.util.ModuleUtil.CONVERTER;
import static isv.cjl.module.util.RequestConverterConstants.ApplePay.APPLE_PAY_AUTHORIZATION_REQUEST_CONVERTER_STRATEGIES;
import static isv.cjl.module.util.RequestConverterConstants.ApplePay.AUTHORIZATION;
import static isv.cjl.module.util.RequestConverterConstants.ApplePay.AUTHORIZATION_REVERSAL;
import static isv.cjl.module.util.RequestConverterConstants.ApplePay.CAPTURE;
import static isv.cjl.module.util.RequestConverterConstants.ApplePay.REFUND_FOLLOW_ON;
import static isv.cjl.module.util.RequestConverterConstants.ApplePay.SALE;

@EnableGuiceModules
public class ApplePayRequestConverterHybrisModule extends AbstractModule
{
    @Resource(name = "isv.sap.payment.service.executor.request.converter.applepay.authorizationRequestConverter")
    private Converter<PaymentServiceRequest, Request> authorizationRequestConverter;

    @Resource(name = "isv.sap.payment.converter.creditcard.captureRequestConverter")
    private Converter<PaymentServiceRequest, Request> captureRequestConverter;

    @Resource(name = "isv.sap.payment.converter.creditcard.authorizationReversalRequestConverter")
    private Converter<PaymentServiceRequest, Request> authorizationReversalRequestConverter;

    @Resource(name = "isv.sap.payment.converter.creditcard.refundFollowOnRequestConverter")
    private Converter<PaymentServiceRequest, Request> refundFollowOnRequestConverter;

    @Resource(name = "isv.sap.payment.service.executor.request.converter.applepay.saleRequestConverter")
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

    @Provides
    @Named(APPLE_PAY_AUTHORIZATION_REQUEST_CONVERTER_STRATEGIES)
    @Singleton
    protected List<AuthorizationRequestConverterStrategy> getApplePayRequestConverterStrategies(final Injector injector)
    {
        final List<AuthorizationRequestConverterStrategy> strategies = newArrayList();
        addStrategy(new DecryptionAuthRequestConverterStrategy(), strategies, injector);
        addStrategy(new VisaMerchDecryptionStrategy(), strategies, injector);
        addStrategy(new MasterCardMerchDecryptionStrategy(), strategies, injector);
        addStrategy(new AmexMerchDecryptionStrategy(), strategies, injector);
        addStrategy(new DiscoverMerchDecryptionStrategy(), strategies, injector);

        return strategies;
    }

    private void addStrategy(final AuthorizationRequestConverterStrategy strategy,
            final List<AuthorizationRequestConverterStrategy> strategies, final Injector injector)
    {
        injector.injectMembers(strategy);
        strategies.add(strategy);
    }
}
