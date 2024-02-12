package isv.sap.payment.service.executor.request.converter.paypal;

import java.math.BigDecimal;
import java.util.List;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;

import isv.cjl.payment.configuration.transaction.PaymentTransaction;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;
import isv.sap.payment.utils.OrderUtils;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.ItemRequestFields.SHIPPING_PRODUCT_NAME;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_PAYMENT_TYPE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_SESSIONS_SERVICE_CANCEL_URL;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_SESSIONS_SERVICE_RUN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.AP_SESSIONS_SERVICE_SUCCESS_URL;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_PRODUCT_NAME;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_QUANTITY;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_TAX_AMOUNT;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_TOTAL_AMOUNT;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_UNIT_PRICE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_CURRENCY;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_DISCOUNT_AMOUNT;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_SHIPPING_DISCOUNT_AMOUNT;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT;
import static isv.cjl.payment.constants.PaymentServiceConstants.PayPal.CREATE_SESSION;
import static isv.sap.payment.enums.AlternativePaymentMethod.PPL;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static org.apache.commons.lang3.StringUtils.abbreviate;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for PayPal create session request.
 */
public class CreateSessionRequestConverter extends AbstractRequestConverter
{
    private static final BigDecimal ITEMS_TAX_AMOUNT = ZERO;

    private static final int PRODUCT_NAME_MAX_LENGTH = 255;

    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel cart = source.getRequiredParam(ORDER);

        final PaymentTransaction sessions = populateBaseFields(source, cart);

        addProductFields(sessions, cart);
        addShippingField(sessions, cart);

        return sessions.request();
    }

    protected PaymentTransaction populateBaseFields(final PaymentServiceRequest source, final AbstractOrderModel cart)
    {
        return requestFactory.request(CREATE_SESSION)
                .addParam(AP_PAYMENT_TYPE, PPL.getCode())
                .addParam(AP_SESSIONS_SERVICE_RUN, true)
                .addParam(AP_SESSIONS_SERVICE_CANCEL_URL, source.getRequiredParam(AP_SESSIONS_SERVICE_CANCEL_URL))
                .addParam(AP_SESSIONS_SERVICE_SUCCESS_URL, source.getRequiredParam(AP_SESSIONS_SERVICE_SUCCESS_URL))
                .addParam(MERCHANT_ID, source.getRequiredParam("merchantId").toString())
                .addParam(MERCHANT_REFERENCE_CODE, cart.getGuid())
                .addParam(PURCHASE_TOTALS_CURRENCY, cart.getCurrency().getIsocode())
                .addParam(PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT, valueOf(cart.getTotalPrice()))
                .addParam(PURCHASE_TOTALS_SHIPPING_DISCOUNT_AMOUNT, valueOf(cart.getTotalDiscounts()));
    }

    protected void addProductFields(final PaymentTransaction sessions, final AbstractOrderModel cart)
    {
        final List<AbstractOrderEntryModel> orderEntries = cart.getEntries();

        for (int i = 0; i < orderEntries.size(); i++)
        {
            final AbstractOrderEntryModel orderEntry = orderEntries.get(i);
            final String productName = abbreviate(orderEntry.getProduct().getName(), PRODUCT_NAME_MAX_LENGTH);

            sessions.addParam(format(ITEM_ID, i), i)
                    .addParam(format(ITEM_PRODUCT_NAME, i), productName)
                    .addParam(format(ITEM_QUANTITY, i), orderEntry.getQuantity().intValue())
                    .addParam(format(ITEM_UNIT_PRICE, i), OrderUtils.getUnitPrice(orderEntry))
                    .addParam(format(ITEM_TAX_AMOUNT, i), ITEMS_TAX_AMOUNT)
                    .addParam(format(ITEM_TOTAL_AMOUNT, i), valueOf(orderEntry.getTotalPrice()));
        }
    }

    protected void addShippingField(final PaymentTransaction sessions, final AbstractOrderModel cart)
    {
        final int idx = cart.getEntries().size();
        final BigDecimal deliveryCost = BigDecimal.valueOf(cart.getDeliveryCost());

        sessions.addParam(format(ITEM_ID, idx), idx)
                .addParam(format(ITEM_PRODUCT_NAME, idx), SHIPPING_PRODUCT_NAME)
                .addParam(format(ITEM_QUANTITY, idx), 1)
                .addParam(format(ITEM_UNIT_PRICE, idx), deliveryCost)
                .addParam(format(ITEM_TAX_AMOUNT, idx), ITEMS_TAX_AMOUNT)
                .addParam(format(ITEM_TOTAL_AMOUNT, idx), deliveryCost);
    }
}
