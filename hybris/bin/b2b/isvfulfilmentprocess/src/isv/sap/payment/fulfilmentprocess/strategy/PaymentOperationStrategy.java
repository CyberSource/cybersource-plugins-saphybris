package isv.sap.payment.fulfilmentprocess.strategy;

import javax.annotation.Nullable;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;

import isv.cjl.payment.enums.AlternativePaymentMethod;
import isv.cjl.payment.enums.PaymentType;
import isv.sap.payment.model.IsvPaymentTransactionModel;

/**
 * A strategy that defines how to execute different payment actions for different payment types.
 */
public interface PaymentOperationStrategy
{
    /**
     * Defines logic for executing different payment operations.
     *
     * @param order order to take payment for
     * @param transaction transaction used in the next payment operation
     * @return payment transaction entry model
     */
    PaymentTransactionEntryModel execute(final OrderModel order, final IsvPaymentTransactionModel transaction);

    /**
     * Returns supported payment type.
     *
     * @return supported {@link isv.sap.payment.enums.PaymentType}
     */
    PaymentType getPaymentType();

    /**
     * Returns supported alternative payment method. Null is expected for other than ALTERNATIVE_PAYMENT type.
     *
     * @return supported {@link isv.sap.payment.enums.AlternativePaymentMethod}
     */
    @Nullable
    AlternativePaymentMethod getPaymentMethod();
}
