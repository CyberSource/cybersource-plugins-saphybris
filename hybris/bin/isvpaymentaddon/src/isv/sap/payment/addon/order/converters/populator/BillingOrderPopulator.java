package isv.sap.payment.addon.order.converters.populator;

import java.util.Optional;

import de.hybris.platform.commercefacades.order.converters.populator.OrderPopulator;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;

public class BillingOrderPopulator extends OrderPopulator
{
    @Override
    public void populate(final OrderModel source, final OrderData target)
    {
        callSuperPopulate(source, target);

        addBillingAddress(source, target);
    }

    private void addBillingAddress(final AbstractOrderModel source, final AbstractOrderData target)
    {
        Optional.ofNullable(source.getPaymentAddress())
                .map(address -> getAddressConverter().convert(address))
                .ifPresent(target::setBillingAddress);
    }

    protected void callSuperPopulate(final OrderModel source, final OrderData target)
    {
        super.populate(source, target);
    }

    @Override
    protected void addPaymentInformation(final AbstractOrderModel source, final AbstractOrderData prototype)
    {
        Optional.ofNullable(source.getPaymentInfo())
                .map(PaymentInfoModel::getBillingAddress)
                .map(addressModel -> getAddressConverter().convert(addressModel))
                .map(addressData -> {
                    final CCPaymentInfoData paymentInfo = new CCPaymentInfoData();
                    paymentInfo.setBillingAddress(addressData);
                    return paymentInfo;
                })
                .ifPresent(prototype::setPaymentInfo);
    }
}
