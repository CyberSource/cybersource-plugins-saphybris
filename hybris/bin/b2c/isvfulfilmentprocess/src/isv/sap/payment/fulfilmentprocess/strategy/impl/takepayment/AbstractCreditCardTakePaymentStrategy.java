
package isv.sap.payment.fulfilmentprocess.strategy.impl.takepayment;

import javax.annotation.Resource;

import com.google.common.base.Optional;
import de.hybris.platform.core.model.order.OrderModel;

import isv.cjl.payment.enums.CardType;
import isv.cjl.payment.service.executor.request.PaymentServiceRequest;
import isv.cjl.payment.service.executor.request.builder.ProcessingLevelPaymentServiceRequestBuilder;
import isv.cjl.payment.service.executor.request.converter.processinglevel.param.ProcessingLevelParam;
import isv.cjl.payment.service.processinglevel.ProcessingLevelService;
import isv.sap.payment.fulfilmentprocess.strategy.impl.AbstractPaymentOperationStrategy;
import isv.sap.payment.model.IsvPaymentTransactionEntryModel;
import isv.sap.payment.model.IsvPaymentTransactionModel;

import static de.hybris.platform.payment.enums.PaymentTransactionType.AUTHORIZATION;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.ORDER;
import static isv.cjl.payment.constants.PaymentConstants.CommonFields.TRANSACTION;
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.ACCEPT;
import static isv.sap.payment.constants.IsvPaymentConstants.TransactionStatus.REVIEW;

public abstract class AbstractCreditCardTakePaymentStrategy extends AbstractPaymentOperationStrategy
{
    @Resource(name = "isv.cjl.payment.service.processinglevel.processingLevelService")
    private ProcessingLevelService processingLevelService;

    abstract ProcessingLevelPaymentServiceRequestBuilder getBuilder();

    @Override
    protected PaymentServiceRequest request(final OrderModel order, final IsvPaymentTransactionModel transaction)
    {
        final IsvPaymentTransactionEntryModel entryModel = (IsvPaymentTransactionEntryModel)
                getPaymentTransactionService().getLatestTransactionEntry(transaction, AUTHORIZATION, ACCEPT, REVIEW)
                        .get();

        final ProcessingLevelPaymentServiceRequestBuilder builder = (ProcessingLevelPaymentServiceRequestBuilder) getBuilder()
                .setMerchantId(transaction.getMerchantId())
                .addParam(ORDER, order)
                .addParam(TRANSACTION, entryModel);

        final java.util.Optional<CardType> type = getPaymentTransactionService().getTransactionCardTypeNew(transaction);

        if (param(type).isPresent())
        {
            builder.addParam("cardType", type.get());
            builder.setProcessingLevel(param(type).get().getLevel())
                    .setPaymentProcessor(param(type).get().getProcessor());
        }

        return builder.build();
    }

    protected Optional<ProcessingLevelParam> param(final java.util.Optional<CardType> type)
    {
        return type.isPresent() ? processingLevelService.getParam(type.get()) : Optional.absent();
    }

    public void setProcessingLevelService(final ProcessingLevelService processingLevelService)
    {
        this.processingLevelService = processingLevelService;
    }
}
