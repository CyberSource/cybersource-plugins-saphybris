package isv.sap.payment.service.transaction;

import java.util.Collections;
import java.util.List;
import javax.annotation.Resource;

import com.google.common.collect.Lists;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.payment.strategy.TransactionCodeGenerator;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.lang3.StringUtils;

import isv.cjl.payment.enums.PaymentType;
import isv.cjl.payment.service.executor.PaymentServiceResult;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.sap.payment.enums.AlternativePaymentMethod;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;
import isv.sap.payment.model.IsvPaymentTransactionModel;

import static java.util.Objects.nonNull;

public class PersistentPaymentTransactionCreator extends TransientPaymentTransactionCreator
{
    @Resource
    private ModelService modelService;

    @Resource
    private TransactionCodeGenerator transactionCodeGenerator;

    private static String getEntryCode(final PaymentTransactionModel transaction)
    {
        return String.format("%s-%d", transaction.getCode(), transaction.getEntries().size() + 1);
    }

    @Override
    public IsvPaymentTransactionEntryModel createTransactionEntry(final PaymentServiceRequest request,
            final PaymentServiceResult isvResponse)
    {
        final IsvPaymentTransactionEntryModel entry = superCreateTransactionEntry(request, isvResponse);
        final IsvPaymentTransactionModel transaction = getOrCreatePaymentTransaction(request, isvResponse);

        persist(entry, transaction);

        return entry;
    }

    protected IsvPaymentTransactionEntryModel superCreateTransactionEntry(final PaymentServiceRequest request,
            final PaymentServiceResult isvResponse)
    {
        return super.createTransactionEntry(request, isvResponse);
    }

    private void persist(final IsvPaymentTransactionEntryModel entry, final PaymentTransactionModel transaction)
    {
        modelService.attach(entry);

        entry.setCode(getEntryCode(transaction));
        entry.setPaymentTransaction(transaction);

        modelService.saveAll(entry, transaction);
    }

    private IsvPaymentTransactionModel getOrCreatePaymentTransaction(final PaymentServiceRequest request,
            final PaymentServiceResult response)
    {
        final AbstractOrderModel order = request.getRequiredParam("order");
        final List<PaymentTransactionModel> paymentTransactions = Lists.newArrayList(order.getPaymentTransactions());
        Collections.reverse(paymentTransactions);

        final String paymentProvider = getPaymentProvider(request);

        return paymentTransactions.stream().map(IsvPaymentTransactionModel.class::cast)
                .filter(transaction -> StringUtils.equals(paymentProvider, transaction.getPaymentProvider())
                        && matchesAlternativePaymentMethod(transaction, request))
                .findFirst().orElseGet(() -> createTransaction(request, order, response));
    }

    private boolean matchesAlternativePaymentMethod(final IsvPaymentTransactionModel transaction,
            final PaymentServiceRequest request)
    {
        final AlternativePaymentMethod requestAlternativePaymentMethod = getAlternativePaymentMethod(request);

        return transaction.getAlternativePaymentMethod() == null
                || requestAlternativePaymentMethod == null
                || transaction.getAlternativePaymentMethod().equals(requestAlternativePaymentMethod);
    }

    protected IsvPaymentTransactionModel createTransaction(final PaymentServiceRequest request,
            final AbstractOrderModel order,
            final PaymentServiceResult isvResponse)
    {
        final IsvPaymentTransactionModel transaction = modelService.create(IsvPaymentTransactionModel.class);

        transaction.setOrder(order);
        transaction.setCode(transactionCodeGenerator.generateCode(order.getCode()));
        transaction.setMerchantId(request.getParam("merchantId"));
        transaction.setRequestId(isvResponse.getData("requestID"));
        transaction.setRequestToken(isvResponse.getData("requestToken"));

        transaction.setPaymentProvider(getPaymentProvider(request));
        transaction.setAlternativePaymentMethod(getAlternativePaymentMethod(request));

        modelService.saveAll(transaction, order);

        return transaction;
    }

    protected String getPaymentProvider(final PaymentServiceRequest request)
    {
        final PaymentType paymentType = request.getPaymentType();

        return nonNull(paymentType) ? paymentType.name() : null;
    }

    private AlternativePaymentMethod getAlternativePaymentMethod(final PaymentServiceRequest request)
    {
        final Object altPaymentMethod = request.getParam("apPaymentType");

        return altPaymentMethod == null ? null : AlternativePaymentMethod.valueOf(altPaymentMethod.toString());
    }

    public void setModelService(final ModelService modelService)
    {
        this.modelService = modelService;
    }

    public void setTransactionCodeGenerator(final TransactionCodeGenerator transactionCodeGenerator)
    {
        this.transactionCodeGenerator = transactionCodeGenerator;
    }
}
