package isv.sap.payment.service.alternativepayment;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Resource;

import com.google.common.collect.Lists;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.servicelayer.model.ModelService;
import jersey.repackaged.com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import isv.cjl.payment.enums.AlternativePaymentMethod;
import isv.cjl.payment.enums.CheckStatusDecision;
import isv.cjl.payment.enums.PaymentTransactionType;
import isv.cjl.payment.service.alternativepayment.AlternativePaymentCheckStatusService;
import isv.sap.payment.configuration.service.PaymentConfigurationService;
import isv.sap.payment.enums.IsvAlternativePaymentStatus;
import isv.sap.payment.model.IsvCheckAlternativePaymentStatusConfigurationModel;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;
import isv.sap.payment.model.IsvPaymentTransactionModel;
import isv.sap.payment.service.PaymentTransactionService;

import static de.hybris.platform.payment.enums.PaymentTransactionType.AUTHORIZATION;
import static de.hybris.platform.payment.enums.PaymentTransactionType.CAPTURE;
import static de.hybris.platform.payment.enums.PaymentTransactionType.CHECK_STATUS;
import static de.hybris.platform.payment.enums.PaymentTransactionType.INITIATE;
import static de.hybris.platform.payment.enums.PaymentTransactionType.SALE;
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.ACCEPT;
import static isv.sap.payment.enums.IsvConfigurationType.ALTERNATIVE_PAYMENT_CONFIG;
import static isv.sap.payment.enums.PaymentType.ALTERNATIVE_PAYMENT;
import static isv.sap.payment.enums.PaymentType.PAY_PAL;
import static isv.sap.payment.model.IsvCheckAlternativePaymentStatusConfigurationModel.PAYMENTMETHOD;
import static isv.sap.payment.utils.Assert.isTrue;
import static java.lang.String.format;

/**
 * Encapsulates the default implementation of {@link AlternativePaymentOrderStatusService} interface.
 * <p>
 * Defines default implementation logic for alternative payment status like: check, count and update.
 */
public class DefaultAlternativePaymentOrderStatusService implements AlternativePaymentOrderStatusService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAlternativePaymentOrderStatusService.class);

    @Resource(name = "isv.sap.payment.paymentTransactionService")
    private PaymentTransactionService paymentTransactionService;

    @Resource(name = "isv.sap.payment.paymentConfigurationService")
    private PaymentConfigurationService paymentConfigurationService;

    @Resource(name = "isv.cjl.payment.service.alternativepayment.alternativePaymentCheckStatusService")
    private AlternativePaymentCheckStatusService alternativePaymentCheckStatusService;

    @Resource
    private ModelService modelService;

    @Override
    public CheckStatusDecision shouldRunCheckStatus(final IsvPaymentTransactionModel transaction)
    {
        final IsvPaymentTransactionEntryModel transactionEntry = getAlternativePaymentTransactionEntry(transaction);

        final int checkStatusAttempts = getExistingCheckStatusAttempts(transaction);

        return alternativePaymentCheckStatusService.shouldRun(
                transactionEntry.getCreationtime(), checkStatusAttempts,
                AlternativePaymentMethod.valueOf(transaction.getAlternativePaymentMethod().name()),
                PaymentTransactionType.valueOf(transactionEntry.getType().getCode()));
    }

    @Override
    public void updateOrderByPaymentStatus(final AbstractOrderModel order,
            final IsvAlternativePaymentStatus alternativePaymentStatus)
    {
        final IsvCheckAlternativePaymentStatusConfigurationModel configuration = getPaymentConfigurationOnOrder(order);

        final OrderStatus status = configuration.getStatusMap().get(alternativePaymentStatus);

        isTrue(status != null, () -> new IllegalArgumentException(
                format("No order status mapping exists for alternative payment status [%s]",
                        alternativePaymentStatus)));

        order.setStatus(status);
        modelService.save(order);

        LOGGER.info("Order [{}] status has been updated to {}", order.getCode(), status);
    }

    private IsvCheckAlternativePaymentStatusConfigurationModel getPaymentConfigurationOnOrder(
            final AbstractOrderModel order)
    {
        final IsvPaymentTransactionModel transaction = getAlternativePaymentTransaction(order);

        return getAlternativePaymentConfiguration(transaction);
    }

    @Override
    public IsvPaymentTransactionModel getAlternativePaymentTransaction(final AbstractOrderModel order)
    {
        return (IsvPaymentTransactionModel) paymentTransactionService.getLatestTransaction(ALTERNATIVE_PAYMENT, order)
                .orElseGet(() ->
                        paymentTransactionService.getLatestTransaction(PAY_PAL, order)
                                .orElseThrow(() -> new IllegalStateException(
                                        format("Missing Alternative Payment Transaction on Order [%s]",
                                                order.getGuid())))
                );
    }

    @Override
    public IsvPaymentTransactionEntryModel getAlternativePaymentTransactionEntry(
            final IsvPaymentTransactionModel transaction)
    {
        final List<PaymentTransactionEntryModel> reversedTransactionEntries = Lists
                .newArrayList(transaction.getEntries());

        Collections.reverse(reversedTransactionEntries);

        return reversedTransactionEntries
                .stream().map(IsvPaymentTransactionEntryModel.class::cast)
                .filter(AcceptedAlternativePaymentTransactionEntryPredicate.INSTANCE).findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                format("No accepted alternative payment transaction were found on order [%s]",
                                        transaction.getOrder().getGuid())));
    }

    @Override
    public int getExistingCheckStatusAttempts(final IsvPaymentTransactionModel transaction)
    {
        return (int) transaction.getEntries().stream().filter(entry -> CHECK_STATUS.equals(entry.getType())).count();
    }

    private IsvCheckAlternativePaymentStatusConfigurationModel getAlternativePaymentConfiguration(
            final IsvPaymentTransactionModel transaction)
    {
        return paymentConfigurationService.getConfiguration(ALTERNATIVE_PAYMENT_CONFIG,
                ImmutableMap.of(PAYMENTMETHOD, transaction.getAlternativePaymentMethod())
        );
    }

    public void setPaymentTransactionService(final PaymentTransactionService paymentTransactionService)
    {
        this.paymentTransactionService = paymentTransactionService;
    }

    public void setPaymentConfigurationService(final PaymentConfigurationService paymentConfigurationService)
    {
        this.paymentConfigurationService = paymentConfigurationService;
    }

    public void setAlternativePaymentCheckStatusService(
            final AlternativePaymentCheckStatusService alternativePaymentCheckStatusService)
    {
        this.alternativePaymentCheckStatusService = alternativePaymentCheckStatusService;
    }

    public void setModelService(final ModelService modelService)
    {
        this.modelService = modelService;
    }

    enum AcceptedAlternativePaymentTransactionEntryPredicate implements Predicate<IsvPaymentTransactionEntryModel>
    {
        INSTANCE;

        @Override
        public boolean test(final IsvPaymentTransactionEntryModel transactionEntry)
        {
            final boolean matchTransactionType = Stream.of(INITIATE, SALE, AUTHORIZATION, CAPTURE)
                    .anyMatch(type -> type.equals(transactionEntry.getType()));

            final boolean isAccepted = ACCEPT.equalsIgnoreCase(transactionEntry.getTransactionStatus());

            return matchTransactionType && isAccepted;
        }
    }
}
