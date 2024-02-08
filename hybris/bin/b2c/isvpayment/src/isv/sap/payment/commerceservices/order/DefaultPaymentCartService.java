package isv.sap.payment.commerceservices.order;

import javax.annotation.Resource;

import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import isv.sap.payment.commerceservices.order.dao.PaymentCartDao;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Encapsulates the default implementation of {@link PaymentCartService}.
 * <p>
 * Provides basic cart operations logic invoked along with payment.
 */
public class DefaultPaymentCartService implements PaymentCartService
{
    private static final Logger LOG = LoggerFactory.getLogger(DefaultPaymentCartService.class);

    @Resource
    private ModelService modelService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource(name = "isv.sap.payment.paymentCartDao")
    private PaymentCartDao paymentCartDao;

    @Override
    public CartModel getCartForGuid(final String guid)
    {
        validateParameterNotNull(guid, "cart code must not be null");

        try
        {
            return paymentCartDao.getCartForGuid(guid);
        }
        catch (final Exception e)
        {
            LOG.warn("Cannot get cart for given code [{}], error message: {}", guid, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void executeWithCartLock(final CartModel cart, final Runnable body)
    {
        transactionTemplate.execute(new TransactionCallbackWithoutResult()
        {
            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus status)
            {
                try
                {
                    modelService.lock(cart.getPk());
                }
                catch (final UnsupportedOperationException e)
                {
                    LOG.warn("model lock is not supported by database", e);
                }
                catch (final Exception e)
                {
                    LOG.warn("cannot get cart lock, it is possible it was already converted to order", e);
                    return;
                }

                body.run();
            }
        });
    }
}
