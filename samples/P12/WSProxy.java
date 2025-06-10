package isv.cjl.payment.ws;

import com.google.common.base.Preconditions;
import isv.cjl.payment.configuration.service.ConfigurationService;
import isv.cjl.payment.schemas.ITransactionProcessor;
import isv.cjl.payment.schemas.ReplyMessage;
import isv.cjl.payment.schemas.RequestMessage;
import isv.cjl.payment.schemas.TransactionProcessor;
import isv.cjl.payment.utils.BinarySecurityTokenHandler;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;

public class WSProxy implements ITransactionProcessor {
    private TransactionProcessor ivsWebService = new TransactionProcessor();
    private ConfigurationService configurationService;

    @Inject
    public WSProxy(@Named("defaultConfigurationService") ConfigurationService configurationService) {
        this.ivsWebService.setHandlerResolver((portInfo) -> {
            List<Handler> handlerList = new ArrayList();
            handlerList.add(new BinarySecurityTokenHandler(configurationService));
            return handlerList;
        });
        this.configurationService = configurationService;
    }

    public ReplyMessage runTransaction(RequestMessage requestMessage) {
        ITransactionProcessor wsCallable = this.ivsWebService.getPortXML();
        this.prepareWsCallable(wsCallable);
        return wsCallable.runTransaction(requestMessage);
    }

    private void prepareWsCallable(ITransactionProcessor wsCallable) {
        String endpointAddress = this.configurationService.getRequiredString("isv.payment.ws.endpoint.address");
        Preconditions.checkNotNull(endpointAddress, "isv.payment.ws.endpoint.address has to be set in configuration");
        BindingProvider bp = (BindingProvider)wsCallable;
        bp.getRequestContext().put("javax.xml.ws.service.endpoint.address", endpointAddress);
    }
}