package isv.sap.payment.service.executor.request.converter.verification;

import de.hybris.platform.core.model.c2l.RegionModel;
import de.hybris.platform.core.model.user.AddressModel;

import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;

import static isv.cjl.payment.constants.PaymentRequestParamConstants.CITY;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.COUNTRY;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.DAV_SERVICE_RUN;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_ID;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.MERCHANT_REFERENCE_CODE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.POSTAL_CODE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.STATE;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.STREET1;
import static isv.cjl.payment.constants.PaymentServiceConstants.Verification.DELIVERY_ADDRESS_VERIFICATION;
import static org.apache.commons.lang.StringUtils.EMPTY;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for Delivery Address Verification service.
 */
public class DeliveryAddressVerificationRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AddressModel address = source.getRequiredParam("address");
        final RegionModel region = address.getRegion();

        return requestFactory.request(DELIVERY_ADDRESS_VERIFICATION)
                .addParam(MERCHANT_ID, source.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, source.getRequiredParam(MERCHANT_REFERENCE_CODE))
                .addParam(DAV_SERVICE_RUN, true)
                .addParam(CITY, address.getTown())
                .addParam(STREET1, address.getLine1())
                .addParam(COUNTRY, address.getCountry().getIsocode())
                .addParam(POSTAL_CODE, address.getPostalcode())
                .addParam(STATE, region == null ? EMPTY : region.getIsocode())
                .request();
    }
}
