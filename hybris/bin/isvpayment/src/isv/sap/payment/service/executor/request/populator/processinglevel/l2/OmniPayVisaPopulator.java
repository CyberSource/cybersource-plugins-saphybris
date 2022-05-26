package isv.sap.payment.service.executor.request.populator.processinglevel.l2;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;

import isv.cjl.payment.configuration.transaction.PaymentTransaction;
import isv.cjl.payment.enums.CardType;

import static isv.cjl.payment.constants.PaymentRequestParamConstants.INVOICE_HEADER_SUMMARY_COMMODITY_CODE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_ALTERNATE_TAX_RATE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_COMMODITY_CODE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_TAX_RATE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.OTHER_TAX_NATIONAL_TAX_AMOUNT;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.OTHER_TAX_NATIONAL_TAX_INDICATOR;
import static isv.sap.payment.constants.IsvPaymentConstants.PaymentRequestPopulatorValues.ITEM_COMMODITY_CODE_VALUE;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;

/**
 * Component that encapsulates 2nd processing level data population for Visa and OmniPay Direct processor.
 * <p>
 * Note: Please replace dummy data for concrete implementation.
 */
public class OmniPayVisaPopulator extends AbstractOmniPayPopulator
{
    @Override
    protected void populateOrderData(final AbstractOrderModel order, final PaymentTransaction target)
    {
        super.populateOrderData(order, target);

        // TODO: use parameters instead of literals
        target
                .addParam(OTHER_TAX_NATIONAL_TAX_AMOUNT, ZERO)
                .addParam(OTHER_TAX_NATIONAL_TAX_INDICATOR, "1")
                .addParam(INVOICE_HEADER_SUMMARY_COMMODITY_CODE, "SUMM");
    }

    @Override
    protected void populateEntryData(final AbstractOrderEntryModel entry, final PaymentTransaction target)
    {
        super.populateEntryData(entry, target);

        final int index = entry.getEntryNumber();

        target
                .addParam(format(ITEM_TAX_RATE, index), ZERO)
                .addParam(format(ITEM_ALTERNATE_TAX_RATE, index), ZERO)
                .addParam(format(ITEM_COMMODITY_CODE, index), ITEM_COMMODITY_CODE_VALUE);
    }

    @Override
    protected void populateShippingItem(final AbstractOrderModel order, final PaymentTransaction target)
    {
        super.populateShippingItem(order, target);

        final int index = order.getEntries().size();

        target
                .addParam(format(ITEM_TAX_RATE, index), ZERO)
                .addParam(format(ITEM_ALTERNATE_TAX_RATE, index), ZERO)
                .addParam(format(ITEM_COMMODITY_CODE, index), ITEM_COMMODITY_CODE_VALUE);
    }

    @Override
    protected CardType getCardType()
    {
        return CardType.VISA;
    }
}
