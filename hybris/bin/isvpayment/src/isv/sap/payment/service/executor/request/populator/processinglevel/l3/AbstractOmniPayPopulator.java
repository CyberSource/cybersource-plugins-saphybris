package isv.sap.payment.service.executor.request.populator.processinglevel.l3;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;

import isv.cjl.payment.configuration.transaction.PaymentTransaction;
import isv.cjl.payment.enums.PaymentProcessor;
import isv.cjl.payment.enums.ProcessingLevel;
import isv.sap.payment.service.executor.request.populator.processinglevel.AbstractPopulator;
import isv.sap.payment.service.executor.request.populator.processinglevel.ProcessingLevelOperation;

import static isv.cjl.payment.constants.PaymentRequestParamConstants.CC_CAPTURE_SERVICE_PURCHASING_LEVEL;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.CC_CREDIT_SERVICE_PURCHASING_LEVEL;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_DISCOUNT_AMOUNT;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_TAX_RATE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_DISCOUNT_AMOUNT;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_TAX_AMOUNT;
import static isv.cjl.payment.enums.ProcessingLevel.L3;
import static isv.sap.payment.service.executor.request.populator.processinglevel.ProcessingLevelOperation.CAPTURE;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;

/**
 * Encapsulates common logic related to 3rd processing level for OmniPay Direct payment processor.
 * <p>
 * Note: Please replace dummy data for concrete implementation.
 */
public abstract class AbstractOmniPayPopulator extends AbstractPopulator
{
    @Override
    protected void populatePurchasingLevelData(final ProcessingLevelOperation processingLevelOperation,
            final PaymentTransaction target)
    {
        if (CAPTURE.equals(processingLevelOperation))
        {
            target.addParam(CC_CAPTURE_SERVICE_PURCHASING_LEVEL, L3.getName());
        }
        else
        {
            target.addParam(CC_CREDIT_SERVICE_PURCHASING_LEVEL, L3.getName());
        }
    }

    @Override
    protected void populateOrderData(final AbstractOrderModel order, final PaymentTransaction target)
    {
        populateOrderDataInternal(order, target);
        target
                .addParam(PURCHASE_TOTALS_TAX_AMOUNT, ZERO)
                .addParam(PURCHASE_TOTALS_DISCOUNT_AMOUNT, ZERO);
    }

    @Override
    protected void populateEntryData(final AbstractOrderEntryModel entry, final PaymentTransaction target)
    {
        populateEntryDataInternal(entry, target);

        populateTaxAndDiscountItemData(target, entry.getEntryNumber());
    }

    @Override
    protected void populateShippingItem(final AbstractOrderModel order, final PaymentTransaction target)
    {
        populateShippingItemInternal(order, target);

        populateTaxAndDiscountItemData(target, order.getEntries().size());
    }

    protected void populateTaxAndDiscountItemData(final PaymentTransaction target, final int index)
    {
        target
                .addParam(format(ITEM_TAX_RATE, index), ZERO)
                .addParam(format(ITEM_DISCOUNT_AMOUNT, index), ZERO);
    }

    @Override
    protected ProcessingLevel getLevel()
    {
        return L3;
    }

    @Override
    protected PaymentProcessor getPaymentProcessor()
    {
        return PaymentProcessor.OMNIPAY_DIRECT;
    }
}
