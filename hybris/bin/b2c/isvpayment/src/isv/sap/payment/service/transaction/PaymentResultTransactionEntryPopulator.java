package isv.sap.payment.service.transaction;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;

import com.google.common.collect.Maps;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.time.TimeService;

import isv.cjl.payment.service.executor.PaymentServiceResult;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;

public class PaymentResultTransactionEntryPopulator
        implements Populator<PaymentServiceResult, IsvPaymentTransactionEntryModel>
{
    @Resource
    private TimeService timeService;

    @Resource
    private CommonI18NService commonI18NService;

    @Override
    public void populate(final PaymentServiceResult source, final IsvPaymentTransactionEntryModel destination)
    {
        destination.setRequestId(source.getData("requestID"));
        destination.setRequestToken(source.getData("requestToken"));

        destination.setTransactionStatus(source.getData("decision"));
        destination.setTime(timeService.getCurrentTime());

        final String reasonCode = String.valueOf((Object) source.getData("reasonCode"));
        destination.setTransactionStatusDetails(reasonCode);

        final String currencyCode = source.getData("purchaseTotalsCurrency");
        destination.setCurrency(currencyCode != null ? commonI18NService.getCurrency(currencyCode) : null);

        final String amount = source.getData("amount");
        destination.setAmount(amount != null ? new BigDecimal(amount) : null);

        destination.setProperties(getProperties(source.getData()));
        destination.setSubscriptionID(source.getData("paySubscriptionCreateReplySubscriptionID"));
    }

    private Map<String, String> getProperties(final Map<String, Object> response)
    {
        return response.entrySet().stream().filter(entry -> entry.getValue() != null)
                .map(entry -> Maps.immutableEntry(entry.getKey(), entry.getValue().toString()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
