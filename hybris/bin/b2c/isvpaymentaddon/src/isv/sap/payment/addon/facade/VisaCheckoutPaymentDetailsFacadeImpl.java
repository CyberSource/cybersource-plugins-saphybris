package isv.sap.payment.addon.facade;

import java.util.Optional;
import javax.annotation.Resource;

import de.hybris.platform.order.CartService;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import isv.sap.payment.addon.VisaCheckoutPaymentDetailsData;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;
import isv.sap.payment.service.PaymentTransactionService;

import static de.hybris.platform.payment.enums.PaymentTransactionType.GET;
import static isv.sap.payment.enums.PaymentType.VISA_CHECKOUT;
import static java.util.Optional.empty;

/**
 * Default implementation class of {@link VisaCheckoutPaymentDetailsFacade}
 */
public class VisaCheckoutPaymentDetailsFacadeImpl implements VisaCheckoutPaymentDetailsFacade
{
    @Resource
    private CartService cartService;

    @Resource(name = "isv.sap.payment.paymentTransactionService")
    private PaymentTransactionService paymentTransactionService;

    @Resource(name = "vcPaymentDetailsConverter")
    private Converter<IsvPaymentTransactionEntryModel, VisaCheckoutPaymentDetailsData> paymentDetailsConverter;

    @Override
    public Optional<VisaCheckoutPaymentDetailsData> getVCPaymentDetails()
    {
        final Optional<PaymentTransactionEntryModel> latestGetTransactionEntry = latestVCGetTransactionEntry();
        if (latestGetTransactionEntry.isPresent())
        {
            final VisaCheckoutPaymentDetailsData vcPaymentDetails = paymentDetailsConverter
                    .convert((IsvPaymentTransactionEntryModel) latestGetTransactionEntry.get());
            return Optional.of(vcPaymentDetails);
        }
        return empty();
    }

    private Optional<PaymentTransactionEntryModel> latestVCGetTransactionEntry()
    {
        final Optional<PaymentTransactionModel> latestVisaCheckoutTransaction = paymentTransactionService
                .getLatestTransaction(VISA_CHECKOUT, cartService.getSessionCart());
        if (latestVisaCheckoutTransaction.isPresent())
        {
            return paymentTransactionService
                    .getLatestAcceptedTransactionEntry(latestVisaCheckoutTransaction.get(), GET);
        }
        return empty();
    }
}
