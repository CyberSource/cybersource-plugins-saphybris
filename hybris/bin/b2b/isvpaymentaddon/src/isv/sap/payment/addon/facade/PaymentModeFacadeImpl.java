package isv.sap.payment.addon.facade;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Resource;

import de.hybris.platform.acceleratorservices.payment.data.PaymentModeData;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.order.payment.PaymentModeModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.store.services.BaseStoreService;

import isv.sap.payment.enums.AlternativePaymentMethod;
import isv.sap.payment.enums.PaymentType;
import isv.sap.payment.model.IsvPaymentModeModel;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.ListUtils.EMPTY_LIST;

/**
 * Encapsulates an implementation of {@link PaymentModeFacade} interface used at addon level.
 * <p>
 * This component is consumed through storefront checkout controllers.
 */
public class PaymentModeFacadeImpl implements PaymentModeFacade
{
    private static final Integer DEFAULT_PAYMENT_CODE_PREFIX = 999;

    @Resource
    private Converter<PaymentModeModel, PaymentModeData> paymentModeConverter;

    @Resource
    private BaseStoreService baseStoreService;

    @Resource
    private CartService cartService;

    @Override
    public List<PaymentModeData> getPaymentModes()
    {
        final List<IsvPaymentModeModel> modes = baseStoreService.getCurrentBaseStore().getAllowedIsvPaymentModes();
        return modes == null
                ? EMPTY_LIST
                : modes.stream()
                .map(mode -> paymentModeConverter.convert(mode))
                .sorted(new PaymentModeDataComparator())
                .collect(toList());
    }

    @Override
    public boolean isPaymentModeSupported(final PaymentType paymentType,
            final AlternativePaymentMethod paymentSubType)
    {
        ServicesUtil.validateParameterNotNullStandardMessage("paymentType", paymentType);

        return baseStoreService.getCurrentBaseStore().getAllowedIsvPaymentModes().stream().
                anyMatch(paymentMode -> paymentType.equals(paymentMode.getPaymentType())
                        && (paymentSubType == null ? paymentMode.getPaymentSubType() == null
                        : paymentSubType.equals(paymentMode.getPaymentSubType())));
    }

    @Override
    public String getPaymentCountryCode()
    {
        return Optional.of(cartService.getSessionCart())
                .map(CartModel::getPaymentInfo)
                .map(PaymentInfoModel::getBillingAddress)
                .map(AddressModel::getCountry)
                .map(CountryModel::getIsocode)
                .orElse(null);
    }

    public class PaymentModeDataComparator implements Comparator<PaymentModeData>
    {
        @Override
        public int compare(final PaymentModeData payment1, final PaymentModeData payment2)
        {
            final Integer prefix1 = getPrefix(payment1.getCode());
            final Integer prefix2 = getPrefix(payment2.getCode());
            return prefix1.compareTo(prefix2);
        }

        private Integer getPrefix(final String code)
        {
            return convertToInt(code.split("_")[0]);
        }

        private Integer convertToInt(final String prefix)
        {
            try
            {
                return Integer.valueOf(prefix);
            }
            catch (final NumberFormatException ex)
            {
                return DEFAULT_PAYMENT_CODE_PREFIX;
            }
        }
    }
}
