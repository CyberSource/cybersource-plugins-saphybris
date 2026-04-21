package isv.sap.payment.option.service;

import java.util.List;
import jakarta.annotation.Resource;

import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import isv.sap.payment.cronjob.UpdateAlternativePaymentOptionsJob;
import isv.sap.payment.model.IsvAlternativePaymentOptionModel;

import java.util.Collections;

/**
 * Encapsulates the default implementation of {@link PaymentOptionService} interface.
 * <p>
 * This implementation is based on locally-stored alternative payment options fetched on
 * scheduled basis through {@link UpdateAlternativePaymentOptionsJob}.
 */
public class DefaultPaymentOptionService implements PaymentOptionService
{
    public static final String OPTIONS_QUERY = "SELECT {PK} FROM {" + IsvAlternativePaymentOptionModel._TYPECODE + "}";

    @Resource
    private ModelService modelService;

    @Resource
    private FlexibleSearchService flexibleSearchService;

    @Override
    public List<IsvAlternativePaymentOptionModel> getPaymentOptions()
    {
        final SearchResult<IsvAlternativePaymentOptionModel> options = flexibleSearchService.search(OPTIONS_QUERY);
        return options.getResult() == null ? Collections.emptyList() : options.getResult();
    }

    @Override
    public void updatePaymentOptions(final List<IsvAlternativePaymentOptionModel> options)
    {
        modelService.removeAll(getPaymentOptions());
        modelService.saveAll(options);
    }
}
