package isv.sap.payment.fulfilmentprocess.strategy.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.ordersplitting.strategy.SplittingStrategy;
import de.hybris.platform.ordersplitting.strategy.impl.OrderEntryGroup;

public class SplitByWarehouse implements SplittingStrategy
{
    private static final String WAREHOUSE_LIST_NAME = "WAREHOUSE_LIST";

    protected List<OrderEntryGroup> splitForWarehouses(final OrderEntryGroup orderEntryList)
    {
        final List<OrderEntryGroup> result = new ArrayList<>();

        final OrderEntryGroup todoEntryList = orderEntryList.getEmpty();

        OrderEntryGroup workingOrderEntryList = sortOrderEntryBeforeWarehouseSplitting(orderEntryList);

        final OrderEntryGroup emptyOrderEntryList = orderEntryList.getEmpty();

        do
        {
            todoEntryList.clear();

            List<WarehouseModel> tmpWarehouseResult = null;
            final OrderEntryGroup tmpOrderEntryResult = orderEntryList.getEmpty();

            tmpWarehouseResult = prepareWarehouses(todoEntryList, workingOrderEntryList, emptyOrderEntryList,
                    tmpWarehouseResult, tmpOrderEntryResult);

            if (!tmpOrderEntryResult.isEmpty())
            {
                tmpOrderEntryResult.setParameter(WAREHOUSE_LIST_NAME, tmpWarehouseResult);
                result.add(tmpOrderEntryResult);
            }
            workingOrderEntryList = todoEntryList.getEmpty();
            workingOrderEntryList.addAll(todoEntryList);
        }
        while (!todoEntryList.isEmpty());

        if (!emptyOrderEntryList.isEmpty())
        {
            result.add(emptyOrderEntryList);
        }

        return result;
    }

    protected List<WarehouseModel> prepareWarehouses(final OrderEntryGroup todoEntryList,
            final OrderEntryGroup workingOrderEntryList, final OrderEntryGroup emptyOrderEntryList,
            final List<WarehouseModel> tmpWarehouseResult, final OrderEntryGroup tmpOrderEntryResult)
    {
        List<WarehouseModel> results = tmpWarehouseResult;
        for (final AbstractOrderEntryModel orderEntry : workingOrderEntryList)
        {
            final List<WarehouseModel> currentPossibleWarehouses = getPossibleWarehouses(orderEntry);

            if (currentPossibleWarehouses.isEmpty())
            {
                emptyOrderEntryList.add(orderEntry);
            }
            else
            {
                if (results != null)
                {
                    currentPossibleWarehouses.retainAll(results);
                }

                if (currentPossibleWarehouses.isEmpty())
                {
                    todoEntryList.add(orderEntry);
                }
                else
                {
                    results = currentPossibleWarehouses;
                    tmpOrderEntryResult.add(orderEntry);
                }
            }
        }
        return results;
    }

    protected List<WarehouseModel> getPossibleWarehouses(final AbstractOrderEntryModel orderEntry)
    {
        final List<WarehouseModel> possibleWarehouses = new ArrayList<WarehouseModel>();

        if (orderEntry.getOrder().getStore() != null)
        {
            possibleWarehouses.addAll(orderEntry.getDeliveryPointOfService() == null ? orderEntry.getOrder().getStore()
                    .getWarehouses() : orderEntry.getDeliveryPointOfService().getWarehouses());
        }

        return possibleWarehouses;
    }

    /**
     * Choose best warehouse this function is called by getWarehouseList after we have set of possible warehouses.
     *
     * @param orderEntries
     *           the order entries
     *
     * @return the warehouse model
     */
    @SuppressWarnings("unchecked")
    protected WarehouseModel chooseBestWarehouse(final OrderEntryGroup orderEntries)
    {
        final List<WarehouseModel> warehouses = (List<WarehouseModel>) orderEntries.getParameter(WAREHOUSE_LIST_NAME);
        if (warehouses == null || warehouses.isEmpty())
        {
            return null;
        }
        final Random rnd = new Random(new Date().getTime());

        return warehouses.get(rnd.nextInt(warehouses.size()));
    }

    /**
     * Sort order entry before warehouse splitting.
     *
     * @param listOrderEntry
     *           the list order entry
     *
     * @return the list< order entry model>
     */
    protected OrderEntryGroup sortOrderEntryBeforeWarehouseSplitting(final OrderEntryGroup listOrderEntry)
    {
        return listOrderEntry;
    }

    @Override
    public List<OrderEntryGroup> perform(final List<OrderEntryGroup> orderEntryGroup)
    {
        final List<OrderEntryGroup> result = new ArrayList<>();

        for (final OrderEntryGroup orderEntry : orderEntryGroup)
        {
            for (final OrderEntryGroup tmpOrderEntryGroup : splitForWarehouses(orderEntry))
            {
                result.add(tmpOrderEntryGroup);
            }
        }

        return result;
    }

    @Override
    public void afterSplitting(final OrderEntryGroup group, final ConsignmentModel createdOne)
    {
        createdOne.setWarehouse(chooseBestWarehouse(group));
    }
}
