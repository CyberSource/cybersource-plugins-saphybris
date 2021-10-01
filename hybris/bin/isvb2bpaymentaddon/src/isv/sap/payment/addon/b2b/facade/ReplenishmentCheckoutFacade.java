package isv.sap.payment.addon.b2b.facade;

import isv.sap.payment.addon.b2b.ReplenishmentInfoData;

/**
 * An interfaces that defines basic operations on order replenishment information.
 * <p>
 * These methods allow to schedule a replenishment for an order or to disable order replenishment.
 */
public interface ReplenishmentCheckoutFacade
{
    /**
     * Links replenishment with cart object.
     *
     * @param replenishment an instance of {@link ReplenishmentInfoData} which schedules future replenishment
     */
    void add(final ReplenishmentInfoData replenishment);

    /**
     * Removes replenishment information from cart, if it's available.
     */
    void removeReplenishment();
}
