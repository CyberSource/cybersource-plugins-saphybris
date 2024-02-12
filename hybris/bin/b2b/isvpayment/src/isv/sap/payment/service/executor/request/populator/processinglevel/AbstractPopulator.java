package isv.sap.payment.service.executor.request.populator.processinglevel;

import java.math.BigDecimal;
import java.math.BigInteger;
import javax.inject.Named;

import com.google.inject.Inject;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;

import isv.cjl.payment.configuration.service.ConfigurationService;
import isv.cjl.payment.configuration.transaction.PaymentTransaction;
import isv.cjl.payment.enums.CardType;
import isv.cjl.payment.enums.PaymentProcessor;
import isv.cjl.payment.enums.PaymentTransactionType;
import isv.cjl.payment.enums.ProcessingLevel;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.processinglevel.param.ProcessingLevelParam;
import isv.cjl.payment.service.executor.request.populator.ProcessingLevelPopulator;
import isv.sap.payment.utils.OrderUtils;

import static isv.cjl.module.util.ConfigurationConstants.DEFAULT_CONFIGURATION_SERVICE;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.ItemRequestFields.SHIPPING_PRODUCT_NAME;
import static isv.cjl.payment.constants.PaymentConstants.ItemRequestFields.SHIPPING_PRODUCT_SKU;
import static isv.cjl.payment.constants.PaymentConstants.ProcessingLevelFields.OMNIPAY_DIRECT_MAX_PRODUCT_NAME_SIZE;
import static isv.cjl.payment.constants.PaymentConstants.ProductCodeProperties.DEFAULT_PRODUCT_CODE;
import static isv.cjl.payment.constants.PaymentConstants.ProductCodeProperties.SHIPPING_PRODUCT_CODE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.INVOICE_HEADER_MERCHANT_VAT_REGISTRATION_NUMBER;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.INVOICE_HEADER_SUPPLIER_ORDER_REFERENCE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.INVOICE_HEADER_USER_PO;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_PRODUCT_CODE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_PRODUCT_NAME;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_PRODUCT_SKU;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_QUANTITY;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_TAX_AMOUNT;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_TOTAL_AMOUNT;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_UNIT_PRICE;
import static isv.cjl.payment.enums.PaymentTransactionType.CAPTURE;
import static isv.cjl.payment.enums.PaymentTransactionType.REFUND;
import static isv.cjl.payment.enums.PaymentTransactionType.REFUND_FOLLOW_ON;
import static isv.cjl.payment.enums.PaymentTransactionType.REFUND_STANDALONE;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static org.apache.commons.lang3.StringUtils.length;

/**
 * Encapsulates common populator logic for 2nd & 3rd processing level.
 */
public abstract class AbstractPopulator implements ProcessingLevelPopulator
{
    @Inject
    @Named(DEFAULT_CONFIGURATION_SERVICE)
    private ConfigurationService configurationService;

    private int maxProductNameSize = OMNIPAY_DIRECT_MAX_PRODUCT_NAME_SIZE;

    @Override
    public void populate(final PaymentServiceRequest source, final PaymentTransaction target)
    {
        populatePurchasingLevelData(getProcessingLevelOperation(source), target);

        final AbstractOrderModel order = source.getRequiredParam(ORDER);

        populateOrderData(order, target);

        for (final AbstractOrderEntryModel entry : order.getEntries())
        {
            populateEntryData(entry, target);
        }

        populateShippingItem(order, target);
    }

    @Override
    public boolean supports(final ProcessingLevelParam param)
    {
        return param.getLevel() == getLevel()
                && param.getProcessor() == getPaymentProcessor()
                && param.getCardType() == getCardType();
    }

    protected abstract CardType getCardType();

    protected abstract ProcessingLevel getLevel();

    protected abstract PaymentProcessor getPaymentProcessor();

    protected abstract void populateOrderData(final AbstractOrderModel order, final PaymentTransaction target);

    protected abstract void populateEntryData(final AbstractOrderEntryModel entry, final PaymentTransaction target);

    protected abstract void populateShippingItem(final AbstractOrderModel order, final PaymentTransaction target);

    protected void populatePurchasingLevelData(final ProcessingLevelOperation processingLevelOperation,
            final PaymentTransaction target)
    {
        // empty
    }

    protected String shorten(final String value, final int maxSize)
    {
        return length(value) > maxSize ? value.substring(0, maxSize) : value;
    }

    protected ProcessingLevelOperation getProcessingLevelOperation(final PaymentServiceRequest source)
    {
        final PaymentTransactionType service = source.getPaymentTransactionType();

        if (CAPTURE.equals(service))
        {
            return ProcessingLevelOperation.CAPTURE;
        }
        else if (REFUND.equals(service) || REFUND_FOLLOW_ON.equals(service) || REFUND_STANDALONE.equals(service))
        {
            return ProcessingLevelOperation.CREDIT;
        }
        else
        {
            throw new IllegalArgumentException(
                    format("Processing level data cannot be applied for service request type [%s]", service));
        }
    }

    protected void populateOrderDataInternal(final AbstractOrderModel order, final PaymentTransaction target)
    {
        // TODO: use parameters instead of literals
        target
                .addParam(INVOICE_HEADER_USER_PO, order.getCode())
                .addParam(INVOICE_HEADER_SUPPLIER_ORDER_REFERENCE, "456456346")
                .addParam(INVOICE_HEADER_MERCHANT_VAT_REGISTRATION_NUMBER, "US-123445555");
    }

    protected void populateEntryDataInternal(final AbstractOrderEntryModel entry, final PaymentTransaction target)
    {
        final int index = entry.getEntryNumber();

        populateIdAndTaxItemData(target, index);

        // TODO: common logic - move to product items populator
        target
                .addParam(format(ITEM_QUANTITY, index), entry.getQuantity().intValue())
                .addParam(format(ITEM_PRODUCT_CODE, index), getItemProductCode())
                .addParam(format(ITEM_PRODUCT_NAME, index), shorten(entry.getProduct().getName(), maxProductNameSize))
                .addParam(format(ITEM_TOTAL_AMOUNT, index), BigDecimal.valueOf(entry.getTotalPrice()))
                .addParam(format(ITEM_UNIT_PRICE, index), OrderUtils.getUnitPrice(entry))
                .addParam(format(ITEM_PRODUCT_SKU, index), entry.getProduct().getCode());
    }

    protected void populateShippingItemInternal(final AbstractOrderModel order, final PaymentTransaction target)
    {
        final int index = order.getEntries().size();
        final BigDecimal deliveryCost = BigDecimal.valueOf(order.getDeliveryCost());

        populateIdAndTaxItemData(target, index);

        // TODO: could be a common logic - move to shipping items populator
        target
                .addParam(format(ITEM_QUANTITY, index), 1)
                .addParam(format(ITEM_PRODUCT_CODE, index), getShippingProductCode())
                .addParam(format(ITEM_PRODUCT_NAME, index), SHIPPING_PRODUCT_NAME)
                .addParam(format(ITEM_TOTAL_AMOUNT, index), deliveryCost)
                .addParam(format(ITEM_UNIT_PRICE, index), deliveryCost)
                .addParam(format(ITEM_PRODUCT_SKU, index), SHIPPING_PRODUCT_SKU);
    }

    protected void populateIdAndTaxItemData(final PaymentTransaction target, final int index)
    {
        target
                .addParam(format(ITEM_TAX_AMOUNT, index), ZERO)
                .addParam(format(ITEM_ID, index), BigInteger.valueOf(index));
    }

    /**
     * Returns the default product code.
     * Please override this method to return relevant product codes.
     *
     * @return
     */
    protected String getItemProductCode()
    {
        return configurationService.getRequiredString(DEFAULT_PRODUCT_CODE);
    }

    /**
     * Returns the item's shipping product code.
     * Please override this method to return relevant product code for your shipping item.
     *
     * @return
     */
    protected String getShippingProductCode()
    {
        return configurationService.getRequiredString(SHIPPING_PRODUCT_CODE);
    }

    public void setConfigurationService(final ConfigurationService configurationService)
    {
        this.configurationService = configurationService;
    }

    public void setMaxProductNameSize(final int maxProductNameSize)
    {
        this.maxProductNameSize = maxProductNameSize;
    }
}
