package isv.sap.payment.addon.facade;

import de.hybris.platform.core.model.order.AbstractOrderModel;

import isv.sap.payment.addon.enums.CheckStatusResponse;


/**
 * This interface defines methods related to alternative payments status checks.
 */
public interface AlternativePaymentStatusFacade
{
    /**
     * Returns payment status for orders places with alternative payments
     *
     * @return a check payment status {@link CheckStatusResponse} response for order.
     */
    CheckStatusResponse resolveCheckStatusResponse(final AbstractOrderModel orderModel);
}
