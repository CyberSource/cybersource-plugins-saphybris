package isv.sap.payment.service.executor.request.converter.paypal;

import java.math.BigDecimal;
import java.util.List;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;

import isv.cjl.payment.configuration.transaction.PaymentTransaction;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;
import isv.sap.payment.utils.OrderUtils;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.cjl.payment.constants.PaymentConstants.ItemRequestFields.SHIPPING_PRODUCT_NAME;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.*;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_PRODUCT_NAME;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_QUANTITY;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_TAX_AMOUNT;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_TOTAL_AMOUNT;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_UNIT_PRICE;
import static isv.cjl.payment.constants.PaymentServiceConstants.PayPal.SALE;
import static isv.sap.payment.enums.AlternativePaymentMethod.PPL;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static org.apache.commons.lang.StringUtils.abbreviate;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for PayPal sale request.
 */
public class SaleRequestConverter extends AbstractRequestConverter
{
    private static final BigDecimal ITEMS_TAX_AMOUNT = ZERO;

    private static final int PRODUCT_NAME_MAX_LENGTH = 255;

    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel cart = source.getRequiredParam(ORDER);

        final PaymentTransaction sale = populateBaseFields(source, cart);

        addDeliveryFields(sale, cart);
        addProductFields(sale, cart);
        addShippingField(sale, cart);

        return sale.request();
    }

    protected void addDeliveryFields(final PaymentTransaction sale, final AbstractOrderModel cart)
    {
        final AddressModel address = cart.getDeliveryAddress();

        sale.addParam(SHIP_TO_CITY, address.getTown())
                .addParam(SHIP_TO_COUNTRY, address.getCountry().getIsocode())
                .addParam(SHIP_TO_FIRST_NAME, address.getFirstname())
                .addParam(SHIP_TO_LAST_NAME, address.getLastname())
                .addParam(SHIP_TO_POSTAL_CODE, address.getPostalcode())
                .addParam(SHIP_TO_STREET1, address.getLine1());
    }

    protected PaymentTransaction populateBaseFields(final PaymentServiceRequest source, final AbstractOrderModel cart)
    {
        final PaymentTransaction sale = requestFactory.request(SALE)
                .addParam(MERCHANT_ID, source.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, cart.getGuid())
                .addParam(AP_PAYMENT_TYPE, PPL.getCode())
                .addParam(AP_SALE_SERVICE_RUN, true)
                .addParam(PURCHASE_TOTALS_CURRENCY, cart.getCurrency().getIsocode())
                .addParam(PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT, BigDecimal.valueOf(cart.getTotalPrice()));

        final String billingAgreementID = resolveStringParam(source, AP_BILLING_AGREEMENT_ID);

        if (isBlank(billingAgreementID))
        {
            final PaymentTransactionEntryModel transactionEntry = source.getRequiredParam(TRANSACTION);
            sale.addParam(AP_SALE_SERVICE_ORDER_REQUEST_ID, transactionEntry.getRequestId());
        }
        else
        {
            sale.addParam(AP_BILLING_AGREEMENT_ID, billingAgreementID);
        }

        return sale;
    }

    protected void addProductFields(final PaymentTransaction sale, final AbstractOrderModel cart)
    {
        final List<AbstractOrderEntryModel> orderEntries = cart.getEntries();

        for (int i = 0; i < orderEntries.size(); i++)
        {
            final AbstractOrderEntryModel orderEntry = orderEntries.get(i);
            final String name = abbreviate(orderEntry.getProduct().getName(), PRODUCT_NAME_MAX_LENGTH);

            sale.addParam(format(ITEM_ID, i), i)
                    .addParam(format(ITEM_PRODUCT_NAME, i), name)
                    .addParam(format(ITEM_QUANTITY, i), orderEntry.getQuantity().intValue())
                    .addParam(format(ITEM_UNIT_PRICE, i), OrderUtils.getUnitPrice(orderEntry))
                    .addParam(format(ITEM_TAX_AMOUNT, i), ITEMS_TAX_AMOUNT)
                    .addParam(format(ITEM_TOTAL_AMOUNT, i), valueOf(orderEntry.getTotalPrice()));
        }
    }

    protected void addShippingField(final PaymentTransaction sale, final AbstractOrderModel cart)
    {
        final int idx = cart.getEntries().size();
        final BigDecimal deliveryCost = BigDecimal.valueOf(cart.getDeliveryCost());

        sale.addParam(format(ITEM_ID, idx), idx)
                .addParam(format(ITEM_PRODUCT_NAME, idx), SHIPPING_PRODUCT_NAME)
                .addParam(format(ITEM_QUANTITY, idx), 1)
                .addParam(format(ITEM_UNIT_PRICE, idx), deliveryCost)
                .addParam(format(ITEM_TAX_AMOUNT, idx), ITEMS_TAX_AMOUNT)
                .addParam(format(ITEM_TOTAL_AMOUNT, idx), deliveryCost);
    }
}

