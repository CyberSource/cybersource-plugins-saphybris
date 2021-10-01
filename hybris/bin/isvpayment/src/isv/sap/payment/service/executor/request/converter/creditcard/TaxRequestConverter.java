package isv.sap.payment.service.executor.request.converter.creditcard;

import java.math.BigInteger;

import com.google.common.base.Function;
import de.hybris.platform.core.model.c2l.RegionModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import org.apache.commons.lang3.StringUtils;

import isv.cjl.payment.configuration.transaction.PaymentTransaction;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;
import isv.cjl.payment.utils.PaymentParamUtils;
import isv.sap.payment.utils.OrderUtils;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.ItemRequestFields.SHIPPING_PRODUCT_NAME;
import static isv.cjl.payment.constants.PaymentConstants.ItemRequestFields.SHIPPING_PRODUCT_SKU;
import static isv.cjl.payment.constants.PaymentConstants.ProductCodeProperties.SELLER_REGISTRATION_CODE;
import static isv.cjl.payment.constants.PaymentConstants.ProductCodeProperties.TAX_DEFAULT_PRODUCT_CODE;
import static isv.cjl.payment.constants.PaymentConstants.ProductCodeProperties.TAX_SHIPPING_PRODUCT_CODE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.*;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_PRODUCT_CODE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_PRODUCT_NAME;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_PRODUCT_SKU;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_QUANTITY;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_UNIT_PRICE;
import static isv.cjl.payment.constants.PaymentServiceConstants.TaxCalculation.TAX;
import static java.lang.String.format;
import static java.math.BigDecimal.valueOf;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for payment tax request.
 */
public class TaxRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel cart = source.getRequiredParam(ORDER);

        final PaymentTransaction target = addBaseFields(source, cart);

        addProductFields(target, cart);
        addShippingField(target, cart);

        return target.request();
    }

    private PaymentTransaction addBaseFields(final PaymentServiceRequest request, final AbstractOrderModel cart)
    {
        final AddressModel shippingAddress = cart.getDeliveryAddress();
        final AddressModel billingAddress = getBillingAddress(cart);

        return requestFactory.request(TAX)
                .addParam(MERCHANT_ID, request.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, cart.getGuid())
                .addParam(PURCHASE_TOTALS_CURRENCY, cart.getCurrency().getIsocode())
                .addParam(TAX_SERVICE_RUN, true)
                .addParam(BILL_TO_COUNTRY, billingAddress.getCountry().getIsocode())
                .addParam(BILL_TO_CITY, billingAddress.getTown())
                .addParam(BILL_TO_COMPANY, billingAddress.getCompany())
                .addParam(BILL_TO_EMAIL, billingAddress.getEmail())
                .addParam(BILL_TO_FIRST_NAME, billingAddress.getFirstname())
                .addParam(BILL_TO_LAST_NAME, billingAddress.getLastname())
                .addParam(BILL_TO_PHONE_NUMBER, billingAddress.getPhone1())
                .addParam(BILL_TO_POSTAL_CODE, billingAddress.getPostalcode())
                .addParam(BILL_TO_STATE,
                        PaymentParamUtils.getValue(billingAddress.getRegion(), createShortIsocodeFunction()))
                .addParam(BILL_TO_STREET1, billingAddress.getLine1())
                .addParam(BILL_TO_STREET2, billingAddress.getLine2())
                .addParam(BILL_TO_STREET3, billingAddress.getBuilding())

                .addParam(SHIP_TO_BUILDING_NUMBER, shippingAddress.getBuilding())
                .addParam(SHIP_TO_CITY, shippingAddress.getTown())
                .addParam(SHIP_TO_COMPANY, shippingAddress.getCompany())
                .addParam(SHIP_TO_COUNTRY, shippingAddress.getCountry().getIsocode())
                .addParam(SHIP_TO_DISTRICT, shippingAddress.getDistrict())
                .addParam(SHIP_TO_FIRST_NAME, shippingAddress.getFirstname())
                .addParam(SHIP_TO_LAST_NAME, shippingAddress.getLastname())
                .addParam(SHIP_TO_PHONE_NUMBER, shippingAddress.getPhone1())
                .addParam(SHIP_TO_STATE,
                        PaymentParamUtils.getValue(shippingAddress.getRegion(), createShortIsocodeFunction()))
                .addParam(SHIP_TO_POSTAL_CODE, shippingAddress.getPostalcode())
                .addParam(SHIP_TO_STREET1, shippingAddress.getLine1())
                .addParam(SHIP_TO_STREET2, shippingAddress.getLine2())
                .addParam(SHIP_TO_STREET3, shippingAddress.getBuilding())
                .addParam(TAX_SERVICE_SELLER_REGISTRATION, getSellerRegistrationCode());
    }

    protected Function<RegionModel, String> createShortIsocodeFunction()
    {
        return input -> input.getIsocodeShort();
    }

    protected AddressModel getBillingAddress(final AbstractOrderModel cart)
    {
        return cart.getPaymentInfo().getBillingAddress();
    }

    /**
     * Returns the Seller Registration Code that ISV uses to accurately calculate the tax amount (relevant for Canada).
     *
     * @return VAT seller registration number
     */
    protected String getSellerRegistrationCode()
    {
        return configurationService.getString(SELLER_REGISTRATION_CODE);
    }

    protected void addProductFields(final PaymentTransaction target, final AbstractOrderModel cart)
    {
        for (int i = 0; i < cart.getEntries().size(); i++)
        {
            addProductField(target, i, cart.getEntries().get(i));
        }
    }

    protected void addProductField(final PaymentTransaction target, final int index,
            final AbstractOrderEntryModel entry)
    {
        target.addParam(format(ITEM_ID, index), BigInteger.valueOf(index));
        target.addParam(format(ITEM_PRODUCT_CODE, index), getItemProductCode(entry));
        target.addParam(format(ITEM_PRODUCT_NAME, index), StringUtils.abbreviate(entry.getProduct().getName(), 15));
        target.addParam(format(ITEM_PRODUCT_SKU, index), entry.getProduct().getCode());
        target.addParam(format(ITEM_QUANTITY, index), entry.getQuantity().intValue());
        target.addParam(format(ITEM_UNIT_PRICE, index), OrderUtils.getUnitPrice(entry));
    }

    /**
     * Returns the default product code used for tax calculation.
     * Please override this method to return relevant product codes.
     *
     * @param entry the order entry
     * @return
     */
    @SuppressWarnings("squid:S1172") // parameter to be used in actual implementation
    protected String getItemProductCode(final AbstractOrderEntryModel entry)
    {
        return configurationService.getRequiredString(TAX_DEFAULT_PRODUCT_CODE);
    }

    protected void addShippingField(final PaymentTransaction target, final AbstractOrderModel cart)
    {
        final int index = cart.getEntries().size();

        target.addParam(format(ITEM_ID, index), BigInteger.valueOf(index));
        target.addParam(format(ITEM_PRODUCT_CODE, index), getShippingProductCode(cart));
        target.addParam(format(ITEM_PRODUCT_NAME, index), SHIPPING_PRODUCT_NAME);
        target.addParam(format(ITEM_PRODUCT_SKU, index), SHIPPING_PRODUCT_SKU);
        target.addParam(format(ITEM_QUANTITY, index), 1);
        target.addParam(format(ITEM_UNIT_PRICE, index), valueOf(cart.getDeliveryCost()));
    }

    /**
     * Returns the item's shipping product code used for tax calculation.
     * Please override this method to return relevant product code for your shipping item.
     *
     * @param cart
     * @return
     */
    @SuppressWarnings("squid:S1172") // parameter to be used in actual implementation
    protected String getShippingProductCode(final AbstractOrderModel cart)
    {
        return configurationService.getRequiredString(TAX_SHIPPING_PRODUCT_CODE);
    }
}
