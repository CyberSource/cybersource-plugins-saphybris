package isv.sap.payment.service.executor.request.converter.alternative;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import de.hybris.platform.core.model.c2l.RegionModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;

import isv.cjl.payment.configuration.transaction.PaymentTransaction;
import isv.cjl.payment.enums.SessionsType;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;
import isv.cjl.payment.utils.PaymentParamUtils;
import isv.sap.payment.utils.OrderUtils;

import static isv.cjl.payment.constants.PaymentConstants.AlternativePaymentsRequestFields.CreateSession.CANCEL_URL;
import static isv.cjl.payment.constants.PaymentConstants.AlternativePaymentsRequestFields.CreateSession.FAILURE_URL;
import static isv.cjl.payment.constants.PaymentConstants.AlternativePaymentsRequestFields.CreateSession.SUCCESS_URL;
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
import static isv.cjl.payment.constants.PaymentServiceConstants.AlternativePayment.UPDATE_SESSION;
import static isv.sap.payment.enums.AlternativePaymentMethod.KLI;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static org.apache.commons.lang.StringUtils.abbreviate;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for Klarna create session request.
 */
public class UpdateSessionRequestConverter extends AbstractRequestConverter
{
    private static final BigDecimal ITEMS_TAX_AMOUNT = ZERO;

    private static final int PRODUCT_NAME_MAX_LENGTH = 255;

    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel cart = source.getRequiredParam(ORDER);

        final PaymentTransaction target = populateBaseFields(source, cart);

        addProductFields(target, cart);
        addShippingField(target, cart);

        addOptionalFields(target, source);

        return target.request();
    }

    protected PaymentTransaction populateBaseFields(final PaymentServiceRequest source, final AbstractOrderModel cart)
    {
        final AddressModel billingAddress = cart.getPaymentInfo().getBillingAddress();
        final AddressModel deliveryAddress = cart.getDeliveryAddress();
        final PaymentTransactionEntryModel transaction = source.getRequiredParam(TRANSACTION);

        return requestFactory.request(UPDATE_SESSION)
                .addParam(AP_PAYMENT_TYPE, KLI.getCode())
                .addParam(AP_SESSIONS_SERVICE_RUN, true)
                .addParam(AP_SESSIONS_SERVICE_SESSIONS_TYPE, SessionsType.U)
                .addParam(AP_SESSIONS_SERVICE_CANCEL_URL, source.getRequiredParam(CANCEL_URL))
                .addParam(AP_SESSIONS_SERVICE_FAILURE_URL, source.getRequiredParam(FAILURE_URL))
                .addParam(AP_SESSIONS_SERVICE_SUCCESS_URL, source.getRequiredParam(SUCCESS_URL))
                .addParam(AP_SESSIONS_SERVICE_REQUEST_ID, transaction.getRequestId())
                .addParam(MERCHANT_ID, source.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, cart.getGuid())

                .addParam(BILL_TO_CITY, billingAddress.getTown())
                .addParam(BILL_TO_COUNTRY, billingAddress.getCountry().getIsocode())
                .addParam(BILL_TO_EMAIL, billingAddress.getEmail())
                .addParam(BILL_TO_FIRST_NAME, billingAddress.getFirstname())
                .addParam(BILL_TO_LAST_NAME, billingAddress.getLastname())
                .addParam(BILL_TO_LANGUAGE, source.getRequiredParam(BILL_TO_LANGUAGE))
                .addParam(BILL_TO_POSTAL_CODE, billingAddress.getPostalcode())
                .addParam(BILL_TO_STREET1, billingAddress.getLine1())

                .addParam(SHIP_TO_CITY, deliveryAddress.getTown())
                .addParam(SHIP_TO_COUNTRY, deliveryAddress.getCountry().getIsocode())
                // Let take email from billing address
                // as OOTB logic doesn't keep email in shipping address.
                .addParam(SHIP_TO_EMAIL, billingAddress.getEmail())
                .addParam(SHIP_TO_FIRST_NAME, deliveryAddress.getFirstname())
                .addParam(SHIP_TO_LAST_NAME, deliveryAddress.getLastname())
                .addParam(SHIP_TO_POSTAL_CODE, deliveryAddress.getPostalcode())
                .addParam(SHIP_TO_STREET1, deliveryAddress.getLine1())

                .addParam(PURCHASE_TOTALS_CURRENCY, cart.getCurrency().getIsocode())
                .addParam(PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT, BigDecimal.valueOf(cart.getTotalPrice()))

                .addParam(BILL_TO_STATE,
                        PaymentParamUtils.getValue(billingAddress.getRegion(), RegionModel::getIsocodeShort))
                .addParam(BILL_TO_DISTRICT,
                        PaymentParamUtils.getValue(billingAddress.getRegion(), RegionModel::getIsocodeShort))

                .addParam(SHIP_TO_STATE,
                        PaymentParamUtils.getValue(deliveryAddress.getRegion(), RegionModel::getIsocodeShort))
                .addParam(SHIP_TO_DISTRICT,
                        PaymentParamUtils.getValue(deliveryAddress.getRegion(), RegionModel::getIsocodeShort));
    }

    protected void addProductFields(final PaymentTransaction target, final AbstractOrderModel cart)
    {
        final List<AbstractOrderEntryModel> orderEntries = cart.getEntries();

        for (int i = 0; i < orderEntries.size(); i++)
        {
            final AbstractOrderEntryModel orderEntry = orderEntries.get(i);
            target.addParam(format(ITEM_ID, i), BigDecimal.valueOf(i))
                    .addParam(format(ITEM_PRODUCT_NAME, i),
                            abbreviate(orderEntry.getProduct().getName(), PRODUCT_NAME_MAX_LENGTH))
                    .addParam(format(ITEM_QUANTITY, i), orderEntry.getQuantity().intValue())
                    .addParam(format(ITEM_UNIT_PRICE, i), OrderUtils.getUnitPrice(orderEntry))
                    .addParam(format(ITEM_TAX_AMOUNT, i), ITEMS_TAX_AMOUNT)
                    .addParam(format(ITEM_TOTAL_AMOUNT, i), BigDecimal.valueOf(orderEntry.getTotalPrice()));
        }
    }

    protected void addShippingField(final PaymentTransaction target, final AbstractOrderModel cart)
    {
        final int index = cart.getEntries().size();
        final BigDecimal deliveryCost = BigDecimal.valueOf(cart.getDeliveryCost());

        target.addParam(format(ITEM_ID, index), BigInteger.valueOf(index))
                .addParam(format(ITEM_PRODUCT_NAME, index), SHIPPING_PRODUCT_NAME)
                .addParam(format(ITEM_QUANTITY, index), 1)
                .addParam(format(ITEM_UNIT_PRICE, index), deliveryCost)
                .addParam(format(ITEM_TAX_AMOUNT, index), ITEMS_TAX_AMOUNT)
                .addParam(format(ITEM_TOTAL_AMOUNT, index), deliveryCost);
    }

    protected void addOptionalFields(final PaymentTransaction target, final PaymentServiceRequest source)
    {
        target.addParam(PURCHASE_TOTALS_DISCOUNT_AMOUNT, source.getParam(PURCHASE_TOTALS_DISCOUNT_AMOUNT))
                .addParam(PURCHASE_TOTALS_TAX_AMOUNT, source.getParam(PURCHASE_TOTALS_TAX_AMOUNT));
    }
}
