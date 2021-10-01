package isv.sap.payment.addon.b2b.facade.impl;

import javax.annotation.Resource;

import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;

import isv.sap.payment.addon.b2b.ReplenishmentInfoData;
import isv.sap.payment.addon.b2b.facade.ReplenishmentCheckoutFacade;
import isv.sap.payment.addon.b2b.model.ReplenishmentInfoModel;

/**
 * Encapsulates the default implementation of {@link ReplenishmentCheckoutFacade} interface.
 * <p>
 * Current implementation works with current session cart which is resolved implicitly
 * through {@link CartService}.
 */
public class DefaultReplenishmentCheckoutFacade implements ReplenishmentCheckoutFacade
{
    @Resource
    private CartService cartService;

    @Resource
    private ModelService modelService;

    @Resource
    private Converter<ReplenishmentInfoData, ReplenishmentInfoModel> replenishmentInfoReverseConverter;

    @Override
    public void add(final ReplenishmentInfoData replenishment)
    {
        removeReplenishment();

        cart().setReplenishmentInfo(replenishmentInfoReverseConverter.convert(replenishment));

        modelService.save(cart());
    }

    @Override
    public void removeReplenishment()
    {
        if (cart().getReplenishmentInfo() != null)
        {
            modelService.remove(cart().getReplenishmentInfo());

            cart().setReplenishmentInfo(null);

            modelService.save(cart());
        }
    }

    private CartModel cart()
    {
        return cartService.getSessionCart();
    }
}
