package isv.sap.payment.service.executor.request.converter.creditcard;

import java.math.BigDecimal;

import de.hybris.platform.core.model.c2l.RegionModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.payment.dto.CardInfo;

import isv.cjl.payment.configuration.transaction.PaymentTransaction;
import isv.cjl.payment.enums.CardType;
import isv.cjl.payment.exception.PaymentException;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.converter.AbstractRequestConverter;
import isv.cjl.payment.service.request.Request;
import isv.cjl.payment.utils.Assert;
import isv.cjl.payment.utils.PaymentParamUtils;

import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentRequestParamConstants.*;
import static isv.cjl.payment.constants.PaymentServiceConstants.CreditCard.SET_UP;
import static isv.sap.payment.constants.IsvPaymentConstants.CreditCardRequestFields.CARD_INFO;
import static isv.sap.payment.constants.IsvPaymentConstants.CreditCardRequestFields.FLEX_TOKEN;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * A component that encapsulates conversion logic from {@link PaymentServiceRequest} to
 * {@link Request} for payment setup request.
 */
public class SetUpRequestConverter extends AbstractRequestConverter
{
    @Override
    public Request convert(final PaymentServiceRequest source)
    {
        final AbstractOrderModel order = source.getRequiredParam(ORDER);
        final AddressModel billingAddress = order.getPaymentInfo().getBillingAddress();
        final AddressModel deliveryAddress = order.getDeliveryAddress();
        final String flexToken = source.getParam(FLEX_TOKEN);
        final PaymentTransaction setUpRequest = requestFactory.request(SET_UP)
                .addParam(MERCHANT_ID, source.getRequiredParam(MERCHANT_ID))
                .addParam(MERCHANT_REFERENCE_CODE, order.getGuid())
                .addParam(PAYER_AUTH_SET_UP_SERVICE_RUN, true)
                .addParam(TOKEN_SOURCE_TRANSIENT_TOKEN, flexToken);
        return setUpRequest.request();
    }
}
