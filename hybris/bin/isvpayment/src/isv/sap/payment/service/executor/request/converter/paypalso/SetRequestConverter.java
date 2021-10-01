package isv.sap.payment.service.executor.request.converter.paypalso;

import java.math.BigDecimal;
import java.util.Optional;

import de.hybris.platform.core.model.c2l.RegionModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;

import isv.cjl.payment.configuration.transaction.PaymentTransaction;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.SET_TRANSACTION;
import static isv.cjl.payment.constants.PaymentConstants.PayPalServiceRequestFields.CANCEL_URL;
import static isv.cjl.payment.constants.PaymentConstants.PayPalServiceRequestFields.RETURN_URL;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.*;
import static isv.cjl.payment.constants.PaymentServiceConstants.PayPalSo.SET;
import static java.util.Optional.ofNullable;

/*
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for paypal payment set request.
 */
public class SetRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest request)
    {
        final AbstractOrderModel order = request.getRequiredParam(ORDER);
        final IsvPaymentTransactionEntryModel set = request.getParam(SET_TRANSACTION);

        final PaymentTransaction payPalSet = setRequiredFields(order, request);

        setPayPalFields(order, set, payPalSet);
        setBillingFields(order, payPalSet);
        setShippingFields(order, payPalSet);

        return payPalSet.request();
    }

    protected void setPayPalFields(final AbstractOrderModel order,
            final IsvPaymentTransactionEntryModel setTransactionEntry,
            final PaymentTransaction payPalSet)
    {
        payPalSet.addParam(PAYPAL_EC_SET_SERVICE_PAYPAL_CUSTOMER_EMAIL,
                order.getPaymentInfo().getBillingAddress().getEmail());

        final Optional<IsvPaymentTransactionEntryModel> entryOptional = ofNullable(setTransactionEntry);

        if (entryOptional.isPresent())
        {
            final IsvPaymentTransactionEntryModel transactionEntry = entryOptional.get();

            payPalSet.addParam(PAYPAL_EC_SET_SERVICE_PAYPAL_EC_SET_REQUEST_ID, transactionEntry.getRequestId())
                    .addParam(PAYPAL_EC_SET_SERVICE_PAYPAL_EC_SET_REQUEST_TOKEN, transactionEntry.getRequestToken())
                    .addParam(PAYPAL_EC_SET_SERVICE_PAYPAL_TOKEN,
                            transactionEntry.getProperties().get(PAYPAL_EC_SET_REPLY_PAYPAL_TOKEN));
        }
    }

    protected void setBillingFields(final AbstractOrderModel order, final PaymentTransaction payPalSet)
    {
        final Optional<AddressModel> addressOptional = ofNullable(order.getPaymentInfo())
                .map(PaymentInfoModel::getBillingAddress);

        if (addressOptional.isPresent())
        {
            final AddressModel address = addressOptional.get();

            payPalSet.addParam(BILL_TO_CITY, address.getTown())
                    .addParam(BILL_TO_COUNTRY, address.getCountry().getIsocode())
                    .addParam(BILL_TO_EMAIL, address.getEmail())
                    .addParam(BILL_TO_FIRST_NAME, address.getFirstname())
                    .addParam(BILL_TO_LAST_NAME, address.getLastname())
                    .addParam(BILL_TO_PHONE_NUMBER, address.getPhone1())
                    .addParam(BILL_TO_POSTAL_CODE, address.getPostalcode())
                    .addParam(BILL_TO_STATE,
                            ofNullable(address.getRegion()).map(RegionModel::getIsocodeShort).orElse(null))
                    .addParam(BILL_TO_STREET1, address.getLine1())
                    .addParam(BILL_TO_STREET2, address.getLine2());
        }
    }

    protected void setShippingFields(final AbstractOrderModel order, final PaymentTransaction payPalSet)
    {
        final Optional<AddressModel> addressOptional = ofNullable(order.getDeliveryAddress());

        if (addressOptional.isPresent())
        {
            final AddressModel address = addressOptional.get();

            payPalSet.addParam(SHIP_TO_FIRST_NAME, address.getFirstname())
                    .addParam(SHIP_TO_LAST_NAME, address.getLastname())
                    .addParam(SHIP_TO_SHIPPING_METHOD, order.getDeliveryMode().getName())
                    .addParam(SHIP_TO_PHONE_NUMBER, address.getPhone1())
                    .addParam(SHIP_TO_CITY, address.getTown())
                    .addParam(SHIP_TO_COMPANY, address.getCompany())
                    .addParam(SHIP_TO_COUNTRY, address.getCountry().getIsocode())
                    .addParam(SHIP_TO_COUNTY, address.getDistrict())
                    .addParam(SHIP_TO_POSTAL_CODE, address.getPostalcode())
                    .addParam(SHIP_TO_STATE,
                            ofNullable(address.getRegion()).map(RegionModel::getIsocodeShort).orElse(null))
                    .addParam(SHIP_TO_STREET1, address.getLine1())
                    .addParam(SHIP_TO_STREET2, address.getLine2())
                    .addParam(SHIP_TO_STREET3, address.getBuilding());
        }
    }

    protected PaymentTransaction setRequiredFields(final AbstractOrderModel order, final PaymentServiceRequest request)
    {
        return requestFactory.request(SET)
                .addParam(MERCHANT_ID, request.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(PAYPAL_EC_SET_SERVICE_RUN, true)
                .addParam(PURCHASE_TOTALS_CURRENCY, order.getCurrency().getIsocode())
                .addParam(PAYPAL_EC_SET_SERVICE_PAYPAL_RETURN, request.getRequiredParam(RETURN_URL))
                .addParam(PAYPAL_EC_SET_SERVICE_PAYPAL_CANCEL_RETURN, request.getRequiredParam(CANCEL_URL))
                .addParam(PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT, BigDecimal.valueOf(order.getTotalPrice()));
    }
}
