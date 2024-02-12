package isv.sap.payment.data.constants

class PaymentConstants
{
    private PaymentConstants()
    {
        // empty
    }

    class PaymentMethod
    {
        public static final String PAY_PAL = 'PAY_PAL'
        public static final String CREDIT_CARD = 'CREDIT_CARD'
        public static final String VISA_CHECKOUT = 'VISA_CHECKOUT'
        public static final String ALTERNATIVE_PAYMENT = 'ALTERNATIVE_PAYMENT'
        public static final String GOOGLE_PAY = 'GOOGLE_PAY'
    }

    static class AliPay
    {
        public static final String ID_FOR_COMPLETED = '333333333401'
        public static final String ID_FOR_PENDING = '333333333402'
        public static final String ID_FOR_ABANDONED = '333333333403'
        public static final String ID_FOR_ERROR = '333333333429'
    }

    static class CreditCard
    {
        final static String VISA = '4111111111111111'
        final static String MASTERCARD = '5555555555554444'
        final static String AMEX = '378282246310005'
        final static String MAESTRO = '50339619890917'

        // Test card for 3DS 2.0
        final static String VISA_3DS2_VALID = '4000000000001091'
        final static String VISA_3DS2_NOSTEPUP_VALID = '4456530000001005'
        final static String VISA_3DS2_TIMEOUT_VALID = '4456530000001070'
        final static String VISA_3DS2_INVALID = '4000000000001109'
        final static String VISA_3DS2_NOSTEPUP_INVALID = '4456530000001047'
        final static String MASTERCARD_3DS2_VALID = '5200000000001096'
        final static String MASTERCARD_3DS2_NOSTEPUP_VALID = '5200000000001005'
        final static String MASTERCARD_3DS2_TIMEOUT_VALID = '5200000000001070'
        final static String MASTERCARD_3DS2_INVALID = '5200000000001104'
        final static String MASTERCARD_3DS2_NOSTEPUP_INVALID = '5200000000001013'
        final static String AMEX_3DS2_VALID = '340000000001098'
        final static String AMEX_3DS2_NOSTEPUP_VALID = '340000000001007'
        final static String AMEX_3DS2_TIMEOUT_VALID = '340000000001072'
        final static String AMEX_3DS2_INVALID = '340000000001106'
        final static String AMEX_3DS2_NOSTEPUP_INVALID = '340000000001015'
        final static String DISCOVER_3DS2_VALID = '6011000000001093'
        final static String DISCOVER_3DS2_NOSTEPUP_VALID = '6011000000001002'
        final static String DISCOVER_3DS2_TIMEOUT_VALID = '6011000000001077'
        final static String DISCOVER_3DS2_INVALID = '6011000000001101'
        final static String DISCOVER_3DS2_NOSTEPUP_INVALID = '6011000000001010'

        final static String HOP_SELECTOR_VISA = 'card_type_001'
        final static String HOP_SELECTOR_MASTERCARD = 'card_type_002'
        final static String HOP_SELECTOR_AMEX = 'card_type_003'
        final static String HOP_SELECTOR_MAESTRO = 'card_type_024'

        final static String SOP_SELECTOR_VISA = '001'
        final static String SOP_SELECTOR_MASTERCARD = '002'
        final static String SOP_SELECTOR_AMEX = '003'
        final static String SOP_SELECTOR_DINERS = '005'
        final static String SOP_SELECTOR_MAESTRO = '024'

        final static String PIN_3_DIGITS = '123'
        final static String PIN_4_DIGITS = '1234'

        static final String EXP_DATE = '12/25'
        static final String EXP_MONTH = '12'
        static final String EXP_YEAR = '2025'

        static final String CVV = '111'
    }

    static class Klarna
    {
        public static final String PENDING_ACCEPT_EMAIL = 'test+pend-accept-5@test.com'
        public static final String PENDING_REJECT_EMAIL = 'test+pend-reject-5@test.com'
        public static final String PAYMENT_NOT_AVAILABLE_EMAIL = 'test+red@test.com'
        public static final String DENIED_EMAIL = 'test+denied@test.com'
    }

    static class Sofort
    {
        public static final String BIC = '88888888'
        public static final String ACCOUNT_NUMBER = '1234'
        public static final String PIN = '123'
        public static final String TAN = '12345'
    }
}
