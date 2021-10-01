package isv.sap.payment.addon.constants;

public final class IsvPaymentAddonConstants extends GeneratedIsvPaymentAddonConstants //NOSONAR
{
    public static final String EXTENSIONNAME = "isvpaymentaddon";

    private IsvPaymentAddonConstants()
    {
        // EMPTY
    }

    public static final class AlternativePayments
    {
        public static final String PAYMENT_OPTION_ID = "paymentOptionId";

        public static final String KLARNA_AUTH_TOKEN = "klarnaAuthToken";

        public static final String KLARNA_LANGUAGE = "klarna.language";

        public static final String KLARNA_SESSION_ID = "klarnaSessionId";

        public static final String RELATIVE_RETURN_URL = "isv.payment.alternativepayment.return.url";

        public static final String RELATIVE_CANCEL_URL = "isv.payment.alternativepayment.cancel.url";

        public static final String RELATIVE_FAILED_URL = "isv.payment.alternativepayment.failed.url";

        public static final String CHECK_STATUS_RECONCILIATION_ID = "isv.alternativepayment.CHECK_STATUS.request.reconciliationID";

        public static final String WECHAT_SALE_TRANSACTION_TIMEOUT = "isv.alternativepayment.weChat.SALE.timeout";

        private AlternativePayments()
        {
            //empty
        }
    }
}
