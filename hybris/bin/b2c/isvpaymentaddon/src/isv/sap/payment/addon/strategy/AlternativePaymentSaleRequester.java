package isv.sap.payment.addon.strategy;

import java.util.Collection;
import java.util.Map;

import de.hybris.platform.core.model.order.AbstractOrderModel;

import isv.cjl.payment.enums.AlternativePaymentMethod;
import isv.cjl.payment.service.executor.PaymentServiceResult;

public interface AlternativePaymentSaleRequester
{
    static AlternativePaymentSaleRequester getSaleRequester(
            final Collection<AlternativePaymentSaleRequester> saleRequesters,
            final AlternativePaymentMethod type)
    {
        return saleRequesters.stream()
                .filter(requester -> requester.supports(type))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "There is no AlternativePaymentSaleRequester for type: " + type));
    }

    PaymentServiceResult initiateSale(AbstractOrderModel cart, AlternativePaymentMethod paymentType,
            String merchantId,
            Map<String, Object> optionalParams);

    boolean supports(AlternativePaymentMethod paymentType);
}
