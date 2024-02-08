package isv.sap.payment.service.transaction;

import javax.annotation.Resource;

import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import isv.cjl.payment.service.executor.PaymentServiceResult;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;

public class TransientPaymentTransactionCreator implements PaymentTransactionCreator
{
    @Resource(name = "isv.sap.payment.converter.paymentResultToTransactionEntryConverter")
    private Converter<PaymentServiceResult, IsvPaymentTransactionEntryModel> paymentResultToTransactionEntryConverter;

    @Resource
    private EnumerationService enumerationService;

    @Override
    public IsvPaymentTransactionEntryModel createTransactionEntry(final PaymentServiceRequest request,
            final PaymentServiceResult isvResponse)
    {
        final IsvPaymentTransactionEntryModel transactionEntry = paymentResultToTransactionEntryConverter
                .convert(isvResponse);

        transactionEntry.setType(enumerationService
                .getEnumerationValue(PaymentTransactionType.class, request.getPaymentTransactionType().getCode()));

        return transactionEntry;
    }
}
