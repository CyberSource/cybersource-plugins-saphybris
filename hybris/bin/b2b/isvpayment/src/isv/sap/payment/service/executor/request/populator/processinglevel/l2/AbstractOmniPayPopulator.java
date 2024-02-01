package isv.sap.payment.service.executor.request.populator.processinglevel.l2;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;

import isv.cjl.payment.configuration.transaction.PaymentTransaction;
import isv.cjl.payment.enums.PaymentProcessor;
import isv.cjl.payment.enums.ProcessingLevel;
import isv.sap.payment.service.executor.request.populator.processinglevel.AbstractPopulator;

/**
 * Encapsulates common logic related to 2nd processing level for OmniPay Direct payment processor
 * <p>
 * Note: Please replace dummy data for concrete implementation.
 */
public abstract class AbstractOmniPayPopulator extends AbstractPopulator
{
    @Override
    protected void populateOrderData(final AbstractOrderModel order, final PaymentTransaction target)
    {
        populateOrderDataInternal(order, target);
    }

    @Override
    protected void populateEntryData(final AbstractOrderEntryModel entry, final PaymentTransaction target)
    {
        populateEntryDataInternal(entry, target);
    }

    @Override
    protected void populateShippingItem(final AbstractOrderModel order, final PaymentTransaction target)
    {
        populateShippingItemInternal(order, target);
    }

    @Override
    public ProcessingLevel getLevel()
    {
        return ProcessingLevel.L2;
    }

    @Override
    protected PaymentProcessor getPaymentProcessor()
    {
        return PaymentProcessor.OMNIPAY_DIRECT;
    }
}
