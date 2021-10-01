package isv.sap.payment.addon.facade;

import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;

import isv.sap.payment.model.IsvPaymentInfoModel;

public interface PaymentInfoFacade
{
    PaymentInfoModel resolvePaymentInfo(CartModel sessionCart, CustomerModel customer);

    IsvPaymentInfoModel createPaymentInfo(AddressModel billingAddress, CustomerModel customerModel,
            boolean saveInAccount);
}
