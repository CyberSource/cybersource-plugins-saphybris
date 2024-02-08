package isv.sap.payment.constants;

/**
 * Defines constants for ISV payment extension
 */
public final class IsvPaymentConstants extends GeneratedIsvPaymentConstants //NOSONAR
{
    public static final String EXTENSIONNAME = "isvpayment";

    private IsvPaymentConstants()
    {
        // EMPTY
    }

    public static final class CreditCardRequestFields
    {
        public static final String FLEX_TOKEN = "flexToken";

        public static final String SUBSCRIPTION_ID = "subscriptionID";

        public static final String CARD_INFO = "card";

        private CreditCardRequestFields()
        {
            // EMPTY
        }
    }

    /**
     * Defines payment transactions status constants.
     */
    public static final class TransactionStatus
    {
        public static final String ACCEPT = "ACCEPT";

        public static final String ERROR = "ERROR";

        public static final String REJECT = "REJECT";

        public static final String REVIEW = "REVIEW";

        private TransactionStatus()
        {
            //empty
        }
    }

    /**
     * Defines the name of Secure Acceptance SOP/HOP response fields.
     */
    public static final class SAResponseFields
    {
        public static final String REFERENCE_NUMBER = "req_reference_number";

        public static final String DECISION = "decision";

        public static final String MERCHANT_ID = "req_merchant_defined_data99";

        public static final String PROFILE_TYPE = "req_merchant_defined_data100";

        public static final String REASON_CODE = "reason_code";

        public static final String CARD_TYPE = "req_card_type";

        private SAResponseFields()
        {
            //empty
        }
    }

    /**
     * Defines payment transactions reason code reply constants.
     */
    public static final class ReasonCode
    {
        public static final String OK = "100"; // NOPMD

        public static final String ERROR = "101";

        public static final String DUPLICATE_TRANSACTION = "104";

        public static final String NON_EXISTING_TRANSACTION = "150";

        public static final String NOT_ENROLLED_CODE = "100";

        public static final String ENROLLED_CODE = "475";

        private ReasonCode()
        {
            //empty
        }
    }

    /**
     * Defines the name of Secure Acceptance SOP/HOP request fields.
     */
    public static final class SARequestFields
    {
        public static final String ACCESS_KEY = "access_key";

        public static final String PROFILE_ID = "profile_id";

        public static final String TRANSACTION_UUID = "transaction_uuid";

        public static final String SIGNED_FIELD_NAMES = "signed_field_names";

        public static final String UNSIGNED_FIELD_NAMES = "unsigned_field_names";

        public static final String SIGNED_DATE_TIME = "signed_date_time";

        public static final String LOCALE = "locale";

        public static final String PAYMENT_METHOD = "payment_method";

        public static final String TRANSACTION_TYPE = "transaction_type";

        public static final String REFERENCE_NUMBER = "reference_number";

        public static final String AMOUNT = "amount";

        public static final String CURRENCY = "currency";

        public static final String BILL_TO_FORENAME = "bill_to_forename";

        public static final String BILL_TO_SURNAME = "bill_to_surname";

        public static final String BILL_TO_EMAIL = "bill_to_email";

        public static final String BILL_TO_ADDRESS_COUNTRY = "bill_to_address_country";

        public static final String BILL_TO_ADDRESS_STATE = "bill_to_address_state";

        public static final String BILL_TO_ADDRESS_CITY = "bill_to_address_city";

        public static final String BILL_TO_ADDRESS_LINE1 = "bill_to_address_line1";

        public static final String BILL_TO_ADDRESS_LINE2 = "bill_to_address_line2";

        public static final String BILL_TO_ADDRESS_POSTAL_CODE = "bill_to_address_postal_code";

        public static final String OVERRIDE_CUSTOM_CANCEL_PAGE = "override_custom_cancel_page";

        public static final String OVERRIDE_CUSTOM_RECEIPT_PAGE = "override_custom_receipt_page";

        public static final String OVERRIDE_CUSTOM_MERCHANT_POST_PAGE = "override_backoffice_post_url";

        public static final String SIGNATURE = "signature";

        public static final String CARD_TYPE = "card_type";

        public static final String CARD_NUMBER = "card_number";

        public static final String CARD_EXPIRE_DATE = "card_expiry_date";

        public static final String CARD_CVN = "card_cvn";

        public static final String PARTNER_SOLUTION_ID = "partner_solution_id";

        public static final String DEVELOPER_ID = "developer_id";

        public static final String MERCHANT_DEFINED_DATA_99 = "merchant_defined_data99";

        public static final String MERCHANT_DEFINED_DATA_100 = "merchant_defined_data100";

        public static final String DEVICE_FINGERPRINT_ID = "device_fingerprint_id";

        public static final String CARD_TYPE_SELECTION_INDICATOR = "card_type_selection_indicator";

        private SARequestFields()
        {
            //empty
        }
    }

    /**
     * Defines PayPal fields used in PaymentServiceRequest construction.
     */
    public static final class PayPalServiceRequestFields
    {
        public static final String RETURN_URL = "paypalReturnURL";

        public static final String CANCEL_URL = "paypalCancelURL";

        private PayPalServiceRequestFields()
        {
            //empty
        }
    }

    /**
     * Defines Alternative Payments response fields constants
     */
    public static final class AlternativePaymentsResponseFields
    {
        public static final String MERCHANT_URL = "merchantURL";

        private AlternativePaymentsResponseFields()
        {
            //empty
        }
    }

    /**
     * Defines Visa Checkout request fields constants
     */
    public static final class VCRequestFields
    {
        public static final String PAYMENT_SOLUTION = "visacheckout";

        public static final String VC_ORDER_ID = "vcOrderId";

        public static final String SHIP_TO_CITY = "shipToCity";

        public static final String SHIP_TO_COUNTRY = "shipToCountry";

        public static final String SHIP_TO_STATE = "shipToState";

        public static final String SHIP_TO_NAME = "shipToName";

        public static final String SHIP_TO_PHONE_NUMBER = "shipToPhoneNumber";

        public static final String SHIP_TO_POSTAL_CODE = "shipToPostalCode";

        public static final String SHIP_TO_STREET1 = "shipToStreet1";

        public static final String SHIP_TO_STREET2 = "shipToStreet2";

        public static final String BILL_TO_CITY = "billToCity";

        public static final String BILL_TO_COUNTRY = "billToCountry";

        public static final String BILL_TO_STATE = "billToState";

        public static final String BILL_TO_NAME = "billToName";

        public static final String BILL_TO_PHONE_NUMBER = "billToPhoneNumber";

        public static final String BILL_TO_POSTAL_CODE = "billToPostalCode";

        public static final String BILL_TO_STREET1 = "billToStreet1";

        public static final String BILL_TO_STREET2 = "billToStreet2";

        private VCRequestFields()
        {
            //empty
        }
    }

    public static final class ProcessingLevelFields
    {
        public static final String PROCESSING_LEVEL = "processingLevel";

        public static final String PAYMENT_PROCESSOR = "paymentProcessor";

        private ProcessingLevelFields()
        {
            //empty
        }
    }

    public static final class ItemRequestFields
    {
        public static final String SHIPPING_PRODUCT_NAME = "SHIPPING";

        public static final String SHIPPING_PRODUCT_SKU = "SHIPPING";

        private ItemRequestFields()
        {
            //empty
        }
    }

    public static final class ProductCodeProperties
    {
        public static final String SELLER_REGISTRATION_CODE = "isv.payment.customer.tax.code.seller.registration";

        public static final String TAX_SHIPPING_PRODUCT_CODE = "isv.payment.customer.product.code.tax.shipping";

        public static final String TAX_DEFAULT_PRODUCT_CODE = "isv.payment.customer.product.code.tax.default";

        public static final String SHIPPING_PRODUCT_CODE = "isv.payment.customer.product.code.shipping";

        public static final String DEFAULT_PRODUCT_CODE = "isv.payment.customer.product.code.default";

        private ProductCodeProperties()
        {
            //empty
        }
    }

    public static final class PaymentRequestPopulatorValues
    {
        public static final String ITEM_COMMODITY_CODE_VALUE = "45648997";

        private PaymentRequestPopulatorValues()
        {
            //empty
        }
    }
}
