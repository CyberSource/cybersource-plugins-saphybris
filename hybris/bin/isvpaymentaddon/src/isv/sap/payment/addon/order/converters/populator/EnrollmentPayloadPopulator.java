package isv.sap.payment.addon.order.converters.populator;

import java.util.stream.Collectors;
import javax.annotation.Resource;

import de.hybris.platform.commerceservices.customer.CustomerEmailResolutionService;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import isv.cjl.payment.data.enrollment.AddressData;
import isv.cjl.payment.data.enrollment.CartItemData;
import isv.cjl.payment.data.enrollment.ConsumerData;
import isv.cjl.payment.data.enrollment.OrderData;
import isv.cjl.payment.data.enrollment.OrderDetailsData;

public class EnrollmentPayloadPopulator implements Populator<AbstractOrderModel, OrderData>
{
    @Resource
    private Populator<AddressModel, AddressData> enrollmentAddressPopulator;

    @Resource
    private CustomerEmailResolutionService customerEmailResolutionService;

    @Override
    public void populate(final AbstractOrderModel source, final OrderData target) throws ConversionException
    {
        populateOrderDetails(source, target);
        populateItems(source, target);
        populateConsumer(source, target);
    }

    private void populateOrderDetails(final AbstractOrderModel source, final OrderData target)
    {
        final OrderDetailsData orderDetails = new OrderDetailsData();
        orderDetails.setOrderNumber(source.getCode());
        orderDetails.setAmount((int) (source.getTotalPrice() * 100));
        orderDetails.setCurrencyCode(source.getCurrency().getIsocode());

        target.setOrderDetails(orderDetails);
    }

    private void populateItems(final AbstractOrderModel source, final OrderData target)
    {
        target.setCart(source.getEntries().stream()
                .map(this::createCartItem)
                .collect(Collectors.toList()));
    }

    private void populateConsumer(final AbstractOrderModel source, final OrderData target)
    {
        final ConsumerData consumer = new ConsumerData();
        if (source.getUser() instanceof CustomerModel)
        {
            consumer.setEmail1(customerEmailResolutionService.getEmailForCustomer((CustomerModel) source.getUser()));
        }

        if (source.getDeliveryAddress() != null)
        {
            final AddressData shippingAddress = new AddressData();
            enrollmentAddressPopulator.populate(source.getDeliveryAddress(), shippingAddress);
            consumer.setShippingAddress(shippingAddress);
        }

        if (source.getPaymentAddress() != null)
        {
            final AddressData billingAddress = new AddressData();
            enrollmentAddressPopulator.populate(source.getPaymentAddress(), billingAddress);
            consumer.setBillingAddress(billingAddress);
        }

        target.setConsumer(consumer);
    }

    private CartItemData createCartItem(final AbstractOrderEntryModel entry)
    {
        final CartItemData item = new CartItemData();
        item.setSKU(entry.getProduct().getCode());
        item.setName(entry.getProduct().getName());
        item.setQuantity(entry.getQuantity().intValue());

        return item;
    }
}
