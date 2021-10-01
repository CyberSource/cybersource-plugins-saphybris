package isv.sap.payment.service.executor.request.populator.processinglevel.l3;

import de.hybris.platform.core.model.order.AbstractOrderModel;

import isv.cjl.payment.configuration.transaction.PaymentTransaction;
import isv.cjl.payment.enums.CardType;

import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_TAX_AMOUNT;
import static java.math.BigDecimal.ZERO;

/**
 * Component that encapsulates 3rd processing level data population for Mastercard and OmniPay Direct processor.
 * <p>
 * Note: Please replace dummy data for concrete implementation.
 */
public class OmniPayMastercardPopulator extends AbstractOmniPayPopulator
{
    @Override
    protected void populateOrderData(final AbstractOrderModel order, final PaymentTransaction target)
    {
        super.populateOrderData(order, target);

        target.addParam(PURCHASE_TOTALS_TAX_AMOUNT, ZERO);
    }

    @Override
    protected CardType getCardType()
    {
        return CardType.MASTERCARD_EUROCARD;
    }
}
