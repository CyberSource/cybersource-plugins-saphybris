package isv.sap.payment.fulfilmentprocess.test;

import java.util.Collections;
import java.util.List;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.storelocator.model.PointOfServiceModel;
import isv.sap.payment.fulfilmentprocess.impl.DefaultCheckOrderService;
import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
public class DefaultCheckOrderServiceTest
{
    private final DefaultCheckOrderService defaultCheckOrderService = new DefaultCheckOrderService();

    private OrderModel order;

    @Before
    public void setUp()
    {
        order = mock(OrderModel.class);
        when(order.getCalculated()).thenReturn(Boolean.TRUE);
        when(order.getEntries()).thenReturn(List.of(mock(OrderEntryModel.class)));
        when(order.getDeliveryAddress()).thenReturn(mock(AddressModel.class));
        when(order.getPaymentInfo()).thenReturn(mock(PaymentInfoModel.class));
    }

    @Test
    public void testCheck()
    {
        Assertions.assertThat(defaultCheckOrderService.check(order)).isFalse();
    }

    @Test
    public void testNotCalculated()
    {
        when(order.getCalculated()).thenReturn(Boolean.FALSE);
        Assertions.assertThat(defaultCheckOrderService.check(order)).isFalse();
    }

    @Test
    public void testNoEntries()
    {
        when(order.getEntries()).thenReturn(Collections.EMPTY_LIST);
        Assertions.assertThat(defaultCheckOrderService.check(order)).isFalse();
    }

    @Test
    public void testNoPaymentInfo()
    {
        when(order.getPaymentInfo()).thenReturn(null);
        Assertions.assertThat(defaultCheckOrderService.check(order)).isFalse();
    }

    @Test
    public void testCheckTrueWhenDeliveryModePresent()
    {
        final DeliveryModeModel deliveryMode = mock(DeliveryModeModel.class);
        when(order.getDeliveryMode()).thenReturn(deliveryMode);
        Assertions.assertThat(defaultCheckOrderService.check(order)).isTrue();
    }

    @Test
    public void testCheckFalseWhenDeliveryAddressNotPresent()
    {
        //given
        when(order.getDeliveryAddress()).thenReturn(null);
        final DeliveryModeModel deliveryMode = mock(DeliveryModeModel.class);
        when(order.getDeliveryMode()).thenReturn(deliveryMode);


        final OrderEntryModel orderEntry = mock(OrderEntryModel.class);
        when(order.getEntries()).thenReturn(List.of(orderEntry));

        //expect
        Assertions.assertThat(defaultCheckOrderService.check(order)).isFalse();
    }

    @Test
    public void testCheckTrueWhenDeliveryAddressPresentInOrderEntry()
    {
        //given
        when(order.getDeliveryAddress()).thenReturn(null);
        when(order.getDeliveryMode()).thenReturn(mock(DeliveryModeModel.class));


        final AbstractOrderEntryModel orderEntry = mock(AbstractOrderEntryModel.class);
        when(order.getEntries()).thenReturn(List.of(orderEntry));
        when(orderEntry.getDeliveryPointOfService()).thenReturn(mock(PointOfServiceModel.class));
        when(orderEntry.getDeliveryAddress()).thenReturn(mock(AddressModel.class));

        //expect
        Assertions.assertThat(defaultCheckOrderService.check(order)).isTrue();
    }
}
