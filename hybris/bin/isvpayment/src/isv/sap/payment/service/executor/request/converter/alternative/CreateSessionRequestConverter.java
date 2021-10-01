package isv.sap.payment.service.executor.request.converter.alternative;

import java.math.BigDecimal;

import com.google.common.base.Function;
import de.hybris.platform.core.model.c2l.RegionModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.AddressModel;

import isv.cjl.payment.configuration.transaction.PaymentTransaction;
import isv.cjl.payment.enums.SessionsType;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;
import isv.cjl.payment.utils.PaymentParamUtils;
import isv.sap.payment.utils.OrderUtils;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.ItemRequestFields.SHIPPING_PRODUCT_NAME;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.*;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_PRODUCT_NAME;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_QUANTITY;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_TAX_AMOUNT;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_TOTAL_AMOUNT;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.Item.ITEM_UNIT_PRICE;
import static isv.cjl.payment.constants.PaymentServiceConstants.AlternativePayment.CREATE_SESSION;
import static isv.cjl.payment.enums.AlternativePaymentMethod.KLI;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static org.apache.commons.lang.StringUtils.abbreviate;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for Klarna create session request.
 */
public class CreateSessionRequestConverter extends AbstractRequestConverter
{
    private static final BigDecimal ITEMS_TAX_AMOUNT = ZERO;

    private static final int PRODUCT_NAME_MAX_LENGTH = 255;

    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel cart = source.getRequiredParam(ORDER);

        final AddressModel billingAddress = cart.getPaymentInfo().getBillingAddress();

        final PaymentTransaction session = requestFactory
                .request(CREATE_SESSION)
                .addParam(MERCHANT_ID, source.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, cart.getGuid())
                .addParam(AP_PAYMENT_TYPE, KLI.getCode())
                .addParam(AP_SESSIONS_SERVICE_RUN, true)
                .addParam(AP_SESSIONS_SERVICE_SESSIONS_TYPE, SessionsType.N)
                .addParam(AP_SESSIONS_SERVICE_CANCEL_URL, source.getRequiredParam(AP_SESSIONS_SERVICE_CANCEL_URL))
                .addParam(AP_SESSIONS_SERVICE_FAILURE_URL, source.getRequiredParam(AP_SESSIONS_SERVICE_FAILURE_URL))
                .addParam(AP_SESSIONS_SERVICE_SUCCESS_URL, source.getRequiredParam(AP_SESSIONS_SERVICE_SUCCESS_URL))
                .addParam(BILL_TO_COUNTRY, billingAddress.getCountry().getIsocode())
                .addParam(BILL_TO_POSTAL_CODE, billingAddress.getPostalcode())
                .addParam(BILL_TO_STATE,
                        PaymentParamUtils.getValue(billingAddress.getRegion(), createShortIsocodeFunction()))
                .addParam(PURCHASE_TOTALS_CURRENCY, cart.getCurrency().getIsocode())
                .addParam(PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT, valueOf(cart.getTotalPrice()))
                .addParam(PURCHASE_TOTALS_TAX_AMOUNT, source.getParam(PURCHASE_TOTALS_TAX_AMOUNT))
                .addParam(PURCHASE_TOTALS_DISCOUNT_AMOUNT, source.getParam(PURCHASE_TOTALS_DISCOUNT_AMOUNT));

        addProductFields(session, cart);
        addShippingField(session, cart);

        return session.request();
    }

    protected Function<RegionModel, String> createShortIsocodeFunction()
    {
        return new Function<RegionModel, String>()
        {
            @Override
            public String apply(final RegionModel input)
            {
                return input.getIsocodeShort();
            }
        };
    }

    protected void addProductFields(final PaymentTransaction session, final AbstractOrderModel cart)
    {
        for (int i = 0; i < cart.getEntries().size(); i++)
        {
            final AbstractOrderEntryModel orderEntry = cart.getEntries().get(i);
            final String productName = abbreviate(orderEntry.getProduct().getName(), PRODUCT_NAME_MAX_LENGTH);

            session.addParam(format(ITEM_ID, i), i)
                    .addParam(format(ITEM_PRODUCT_NAME, i), productName)
                    .addParam(format(ITEM_QUANTITY, i), orderEntry.getQuantity().intValue())
                    .addParam(format(ITEM_UNIT_PRICE, i), OrderUtils.getUnitPrice(orderEntry))
                    .addParam(format(ITEM_TAX_AMOUNT, i), ITEMS_TAX_AMOUNT)
                    .addParam(format(ITEM_TOTAL_AMOUNT, i), valueOf(orderEntry.getTotalPrice()));
        }
    }

    protected void addShippingField(final PaymentTransaction session, final AbstractOrderModel cart)
    {
        final int idx = cart.getEntries().size();
        final BigDecimal deliveryCost = valueOf(cart.getDeliveryCost());

        session.addParam(format(ITEM_ID, idx), idx)
                .addParam(format(ITEM_PRODUCT_NAME, idx), SHIPPING_PRODUCT_NAME)
                .addParam(format(ITEM_QUANTITY, idx), 1)
                .addParam(format(ITEM_UNIT_PRICE, idx), deliveryCost)
                .addParam(format(ITEM_TAX_AMOUNT, idx), ITEMS_TAX_AMOUNT)
                .addParam(format(ITEM_TOTAL_AMOUNT, idx), deliveryCost);
    }
}
