package isv.sap.payment.service.executor.request.converter.verification;

import de.hybris.platform.core.model.c2l.RegionModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.AddressModel;

import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.*;
import static isv.cjl.payment.constants.PaymentServiceConstants.Verification.EXPORT_COMPLIANCE;
import static java.util.Optional.ofNullable;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for Export Compliance request.
 */
public class ExportComplianceRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel cart = source.getRequiredParam(ORDER);

        final AddressModel billingAddress = cart.getPaymentInfo().getBillingAddress();
        final AddressModel deliveryAddress = cart.getDeliveryAddress();

        return requestFactory.request(EXPORT_COMPLIANCE)
                .addParam(EXPORT_SERVICE_RUN, true)
                .addParam(MERCHANT_ID, source.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, cart.getGuid())

                .addParam(BILL_TO_CITY, billingAddress.getTown())
                .addParam(BILL_TO_COUNTRY, billingAddress.getCountry().getIsocode())
                .addParam(BILL_TO_EMAIL, billingAddress.getEmail())
                .addParam(BILL_TO_STREET1, billingAddress.getLine1())
                .addParam(BILL_TO_STREET2, billingAddress.getLine2())
                .addParam(BILL_TO_FIRST_NAME, billingAddress.getFirstname())
                .addParam(BILL_TO_LAST_NAME, billingAddress.getLastname())
                .addParam(BILL_TO_POSTAL_CODE, billingAddress.getPostalcode())
                .addParam(BILL_TO_COMPANY, billingAddress.getCompany())
                .addParam(BILL_TO_STATE,
                        ofNullable(billingAddress.getRegion()).map(RegionModel::getIsocodeShort).orElse(null))

                .addParam(SHIP_TO_CITY, deliveryAddress.getTown())
                .addParam(SHIP_TO_COUNTRY, deliveryAddress.getCountry().getIsocode())
                .addParam(SHIP_TO_FIRST_NAME, deliveryAddress.getFirstname())
                .addParam(SHIP_TO_LAST_NAME, deliveryAddress.getLastname())
                .addParam(SHIP_TO_POSTAL_CODE, deliveryAddress.getPostalcode())
                .addParam(SHIP_TO_STATE,
                        ofNullable(deliveryAddress.getRegion()).map(RegionModel::getIsocodeShort).orElse(null))
                .addParam(SHIP_TO_STREET1, deliveryAddress.getLine1())
                .addParam(SHIP_TO_STREET2, deliveryAddress.getLine2())

                .addParam(EXPORT_SERVICE_ADDRESS_OPERATOR, source.getParam(EXPORT_SERVICE_ADDRESS_OPERATOR))
                .addParam(EXPORT_SERVICE_ADDRESS_WEIGHT, source.getParam(EXPORT_SERVICE_ADDRESS_WEIGHT))
                .addParam(EXPORT_SERVICE_COMPANY_WEIGHT, source.getParam(EXPORT_SERVICE_COMPANY_WEIGHT))
                .addParam(EXPORT_SERVICE_NAME_WEIGHT, source.getParam(EXPORT_SERVICE_NAME_WEIGHT))

                .request();
    }
}
