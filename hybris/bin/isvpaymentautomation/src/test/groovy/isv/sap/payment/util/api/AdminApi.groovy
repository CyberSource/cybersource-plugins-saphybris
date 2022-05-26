package isv.sap.payment.util.api

import isv.sap.payment.util.adminconsole.AdminConsole
import isv.sap.payment.data.TestData

class AdminApi
{
    private static final GET_PAYMENT_STATUS_SCRIPT = 'getPaymentStatus.groovy'
    private static final CART_IMPEX = 'cart.impex'
    private static final ORDER_NUMBER_FXS = 'orderNumberFromCronjob.fxs'
    private static final ORDER_STATUS_FXS = 'orderStatusFromOrder.fxs'
    private static final PAYMENT_PROVIDER_FXS = 'paymentProviderByOrderAndType.fxs'
    private static final ENTRY_STATUS_FXS = 'entryStatusByOrderAndEntry.fxs'
    private static final TRANSACTION_FROM_CART_FXS = 'transactionStatusFromCartByEmail.fxs'

    private static final PCI_STRATEGY_KEY = 'site.pci.strategy'
    private static final ALI_PAY_RECONCILATION_ID_KEY = 'isv.payment.alternativepayment.alipay.reconcilationId'

    private static final HOP = 'HOP'
    private static final SOP = 'SOP'
    private static final FLEX = 'FLEX'

    AdminConsole adminConsole

    AdminApi(AdminConsole adminConsole)
    {
        this.adminConsole = adminConsole
    }

    void setPciStrategyHop()
    {
        adminConsole.updateConfig().setHybrisProperty(PCI_STRATEGY_KEY, HOP)
    }

    void setPciStrategySop()
    {
        adminConsole.updateConfig().setHybrisProperty(PCI_STRATEGY_KEY, SOP)
    }

    void setPciStrategyFlexMicroforms()
    {
        adminConsole.updateConfig().setHybrisProperty(PCI_STRATEGY_KEY, FLEX)
    }

    void setAliPayStatus(String reconciliationId)
    {
        adminConsole.updateConfig().setHybrisProperty(ALI_PAY_RECONCILATION_ID_KEY, reconciliationId)
    }

    void importCart(TestData data)
    {
        adminConsole.runImpex().fromTemplate(CART_IMPEX, data.properties)
    }

    String getOrderNumber(String cronJob)
    {
        List<Map> responce = adminConsole.runFlexibleSearch().fromTemplate(ORDER_NUMBER_FXS, [cronJob: cronJob])
        responce[0]?.P_CODE
    }

    String getOrderStatus(String orderCode)
    {
        List<Map> responce = adminConsole.runFlexibleSearch().fromTemplate(ORDER_STATUS_FXS, [orderCode: orderCode])
        responce[0]?.CODE
    }

    String getTransactionPaymentProvider(String orderCode, String entryType = 'AUTHORIZATION')
    {
        List<Map> responce = adminConsole.runFlexibleSearch().fromTemplate(PAYMENT_PROVIDER_FXS, [
                orderCode: orderCode,
                entryType: entryType,
        ])
        responce[0]?.P_PAYMENTPROVIDER
    }

    String getTransactionEntryStatus(String orderCode, String entryType)
    {
        List<Map> responce = adminConsole.runFlexibleSearch().fromTemplate(ENTRY_STATUS_FXS, [
                orderCode: orderCode,
                entryType: entryType,
        ])
        responce[0]?.P_TRANSACTIONSTATUS
    }

    Object getCartTransactionStatus(String userEmail, String entryType = 'AUTHORIZATION')
    {
        List<Map> responce = adminConsole.runFlexibleSearch().fromTemplate(TRANSACTION_FROM_CART_FXS, [
                userEmail: userEmail,
                entryType: entryType,
        ])
        responce[0]?.P_TRANSACTIONSTATUS
    }

    String getTransactionEntryPaymentStatus(String storeCode, String orderNumber, String transactionType)
    {
        Map<String, String> requestMap = [
                orderNumber    : orderNumber,
                storeCode      : storeCode,
                transactionType: transactionType,
        ]

        adminConsole.runScript().fromTemplate(GET_PAYMENT_STATUS_SCRIPT, requestMap)
    }
}
