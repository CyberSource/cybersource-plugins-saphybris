package isv.sap.payment.fulfilmentprocess.strategy.context;

import java.util.Collection;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Required;

import isv.cjl.payment.enums.PaymentType;
import isv.sap.payment.enums.AlternativePaymentMethod;
import isv.sap.payment.fulfilmentprocess.strategy.PaymentOperationStrategy;
import isv.sap.payment.model.IsvPaymentTransactionModel;

import static java.util.Objects.nonNull;

/**
 * A context component that decides what logic to apply for take payment operation.
 */
public class PaymentOperationContext
{
    private Collection<PaymentOperationStrategy> strategies;

    /**
     * Returns a concrete implementation of {@link PaymentOperationStrategy} applicable for provided
     * {@link isv.sap.payment.model.IsvPaymentTransactionModel}
     *
     * @param transaction
     * @return an instance of  {@link PaymentOperationStrategy} that supports payment type and alternative method
     */
    public PaymentOperationStrategy strategy(final IsvPaymentTransactionModel transaction)
    {
        final PaymentType type = PaymentType.valueOf(transaction.getPaymentProvider());
        final AlternativePaymentMethod paymentMethod = transaction.getAlternativePaymentMethod();

        return strategies.stream()
                .filter(strategy -> Objects.equals(type, strategy.getPaymentType()) && Objects
                        .equals(paymentMethod, transformPaymentMethod(strategy.getPaymentMethod())))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported payment type and alternative method."));
    }

    protected AlternativePaymentMethod transformPaymentMethod(
            final isv.cjl.payment.enums.AlternativePaymentMethod paymentMethod)
    {
        return nonNull(paymentMethod) ? AlternativePaymentMethod.valueOf(paymentMethod.getCode()) : null;
    }

    @Required
    public void setStrategies(final Collection<PaymentOperationStrategy> strategies)
    {
        this.strategies = strategies;
    }
}
