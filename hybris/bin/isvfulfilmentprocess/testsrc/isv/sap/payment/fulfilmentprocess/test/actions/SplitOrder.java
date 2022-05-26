package isv.sap.payment.fulfilmentprocess.test.actions;

import java.util.Arrays;

import de.hybris.platform.core.Registry;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.processengine.enums.ProcessState;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.processengine.model.BusinessProcessParameterModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import isv.sap.payment.fulfilmentprocess.constants.IsvfulfilmentprocessConstants;

public class SplitOrder extends TestActionTemp
{
    private static final Logger LOG = LoggerFactory.getLogger(SplitOrder.class);

    private int subprocessCount = 1;

    @Override
    public String execute(final BusinessProcessModel process) throws Exception //NOPMD
    {
        LOG.info("Process: " + process.getCode() + " in step " + getClass());

        final BusinessProcessParameterModel warehouseCounter = new BusinessProcessParameterModel();
        warehouseCounter.setName(IsvfulfilmentprocessConstants.CONSIGNMENT_COUNTER);
        warehouseCounter.setProcess(process);
        warehouseCounter.setValue(Integer.valueOf(subprocessCount));
        save(warehouseCounter);

        final BusinessProcessParameterModel params = new BusinessProcessParameterModel();
        params.setName(IsvfulfilmentprocessConstants.PARENT_PROCESS);
        params.setValue(process.getCode());

        for (int i = 0; i < subprocessCount; i++)
        {
            final ConsignmentProcessModel consProcess = modelService.create(ConsignmentProcessModel.class);
            consProcess.setParentProcess((OrderProcessModel) process);
            consProcess.setCode(process.getCode() + "_" + i);
            consProcess.setProcessDefinitionName("consignment-process-test");
            params.setProcess(consProcess);
            consProcess.setContextParameters(Arrays.asList(params));
            consProcess.setState(ProcessState.CREATED);
            modelService.save(consProcess);
            getBusinessProcessService().startProcess(consProcess);
            LOG.info("Subprocess: " + process.getCode() + "_" + i + " started");
        }

        return "OK";
    }

    @Override
    public BusinessProcessService getBusinessProcessService()
    {
        return (BusinessProcessService) Registry.getApplicationContext().getBean("businessProcessService");
    }
}
