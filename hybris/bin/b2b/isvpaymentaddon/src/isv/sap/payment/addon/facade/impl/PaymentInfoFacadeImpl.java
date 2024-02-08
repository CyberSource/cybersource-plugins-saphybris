package isv.sap.payment.addon.facade.impl;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Resource;

import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.customer.CustomerEmailResolutionService;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;

import isv.sap.payment.addon.facade.PaymentInfoFacade;
import isv.sap.payment.model.IsvPaymentInfoModel;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static java.util.Objects.nonNull;

public class PaymentInfoFacadeImpl implements PaymentInfoFacade
{
    @Resource
    private CustomerEmailResolutionService customerEmailResolutionService;

    @Resource
    private ModelService modelService;

    @Resource
    private CartService cartService;

    @Resource
    private Converter<AddressModel, AddressData> addressConverter;

    public PaymentInfoModel resolvePaymentInfo(final CartModel sessionCart, final CustomerModel customer)
    {
        PaymentInfoModel paymentInfo = sessionCart.getPaymentInfo();
        if (nonNull(paymentInfo))
        {
            return paymentInfo;
        }

        final AddressModel billingAddress = modelService.create(AddressModel.class);
        final AddressModel deliveryAddress = modelService.create(AddressModel.class);

        paymentInfo = createPaymentInfo(billingAddress, customer, false);

        billingAddress.setOwner(paymentInfo);
        billingAddress.setEmail(customerEmailResolutionService.getEmailForCustomer(customer));
        deliveryAddress.setOwner(customer);

        sessionCart.setDeliveryAddress(deliveryAddress);
        sessionCart.setPaymentInfo(paymentInfo);
        modelService.saveAll(paymentInfo, billingAddress, sessionCart);

        return paymentInfo;
    }

    public IsvPaymentInfoModel createPaymentInfo(final AddressModel billingAddress,
            final CustomerModel customerModel, final boolean saveInAccount)
    {
        validateParameterNotNull(billingAddress, "billingAddress cannot be null");
        validateParameterNotNull(customerModel, "customerModel cannot be null");

        final IsvPaymentInfoModel paymentInfoModel = modelService.create(IsvPaymentInfoModel.class);
        paymentInfoModel.setBillingAddress(billingAddress);
        paymentInfoModel.setCode(customerModel.getUid() + "_" + UUID.randomUUID());
        paymentInfoModel.setUser(customerModel);
        paymentInfoModel.setSaved(saveInAccount);

        return paymentInfoModel;
    }

    @Override
    public Optional<AddressData> fetchAddressFromPaymentInfo()
    {
        return Optional.ofNullable(cartService.getSessionCart().getPaymentInfo())
            .map(PaymentInfoModel::getBillingAddress)
            .map(billingAddress -> addressConverter.convert(billingAddress));
    }

    public void setCustomerEmailResolutionService(final CustomerEmailResolutionService customerEmailResolutionService)
    {
        this.customerEmailResolutionService = customerEmailResolutionService;
    }

    public void setModelService(final ModelService modelService)
    {
        this.modelService = modelService;
    }
}
