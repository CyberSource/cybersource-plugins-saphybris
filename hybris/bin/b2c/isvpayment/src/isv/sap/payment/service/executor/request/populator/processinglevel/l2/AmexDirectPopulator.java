package isv.sap.payment.service.executor.request.populator.processinglevel.l2;

import java.math.BigDecimal;
import java.math.BigInteger;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;

import isv.cjl.payment.configuration.transaction.PaymentTransaction;
import isv.cjl.payment.enums.CardType;
import isv.cjl.payment.enums.PaymentProcessor;
import isv.cjl.payment.enums.ProcessingLevel;
import isv.sap.payment.service.executor.request.populator.processinglevel.AbstractPopulator;
import isv.sap.payment.utils.OrderUtils;

import static isv.cjl.payment.constants.PaymentRequestParamConstants.INVOICE_HEADER_AMEX_DATA_TAA1;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.INVOICE_HEADER_AMEX_DATA_TAA2;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.INVOICE_HEADER_AMEX_DATA_TAA3;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.INVOICE_HEADER_AMEX_DATA_TAA4;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_QUANTITY;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_TAX_AMOUNT;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_UNIT_PRICE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.SHIP_TO_POSTAL_CODE;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;

/**
 * Component that encapsulates 2nd processing level data population for American Express card and American Express Direct processor.
 * <p>
 * Note: Please replace dummy data for concrete implementation.
 */
public class AmexDirectPopulator extends AbstractPopulator
{
    @Override
    protected void populateOrderData(final AbstractOrderModel order, final PaymentTransaction target)
    {
        // TODO: use parameters instead of literals
        target
                .addParam(SHIP_TO_POSTAL_CODE, order.getDeliveryAddress().getPostalcode())
                .addParam(INVOICE_HEADER_AMEX_DATA_TAA1, "CPS item 1")
                .addParam(INVOICE_HEADER_AMEX_DATA_TAA2, "CPS item 2")
                .addParam(INVOICE_HEADER_AMEX_DATA_TAA3, "CPS item 3")
                .addParam(INVOICE_HEADER_AMEX_DATA_TAA4, "CPS item 4");
    }

    @Override
    protected void populateEntryData(final AbstractOrderEntryModel entry, final PaymentTransaction target)
    {
        final int index = entry.getEntryNumber();

        target
                .addParam(format(ITEM_ID, index), BigInteger.valueOf(index))
                .addParam(format(ITEM_QUANTITY, index), entry.getQuantity().intValue())
                .addParam(format(ITEM_TAX_AMOUNT, index), ZERO)
                .addParam(format(ITEM_UNIT_PRICE, index), OrderUtils.getUnitPrice(entry));
    }

    @Override
    protected void populateShippingItem(final AbstractOrderModel order, final PaymentTransaction target)
    {
        final int index = order.getEntries().size();

        target
                .addParam(format(ITEM_ID, index), BigInteger.valueOf(index))
                .addParam(format(ITEM_QUANTITY, index), 1)
                .addParam(format(ITEM_TAX_AMOUNT, index), ZERO)
                .addParam(format(ITEM_UNIT_PRICE, index), BigDecimal.valueOf(order.getDeliveryCost()));
    }

    @Override
    protected CardType getCardType()
    {
        return CardType.AMEX;
    }

    @Override
    protected ProcessingLevel getLevel()
    {
        return ProcessingLevel.L2;
    }

    @Override
    protected PaymentProcessor getPaymentProcessor()
    {
        return PaymentProcessor.AMERICAN_EXPRESS_DIRECT;
    }
}
