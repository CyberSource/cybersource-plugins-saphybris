package isv.sap.payment.service.executor.request.converter.applepay;

import java.util.List;
import javax.inject.Named;

import com.google.inject.Inject;

import isv.cjl.payment.exception.PaymentException;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;
import isv.sap.payment.service.executor.request.converter.applepay.strategies.AuthorizationRequestConverterStrategy;

import static isv.cjl.module.util.RequestConverterConstants.ApplePay.APPLE_PAY_AUTHORIZATION_REQUEST_CONVERTER_STRATEGIES;

public class AuthorizationRequestConverter extends AbstractRequestConverter
{
    @Inject
    @Named(APPLE_PAY_AUTHORIZATION_REQUEST_CONVERTER_STRATEGIES)
    private List<AuthorizationRequestConverterStrategy> strategies;

    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        for (final AuthorizationRequestConverterStrategy strategy : strategies)
        {
            if (strategy.supports(source))
            {
                return strategy.convert(source);
            }
        }

        throw new PaymentException("Unable to find conversion strategy for " + source.toString());
    }
}
