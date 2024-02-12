package isv.sap.payment.data.constants

class TestDataConstants
{
    private TestDataConstants()
    {
        // empty
    }

    static class BaseStore
    {
        public static final String APPAREL_UK = 'apparel-uk'
        public static final String APPAREL_DE = 'apparel-de'
        public static final String POWERTOOLS = 'powertools'
    }

    static class Country
    {
        public static final String UK = 'GB'
        public static final String DE = 'DE'
        public static final String US = 'US'
    }

    static class CurrencyIsoCode
    {
        public static final String GBP = 'GBP'
        public static final String EUR = 'EUR'
        public static final String USD = 'USD'
    }

    static class Product
    {
        public static final String PRODUCT_ID_B2C = '300613859'
        public static final String PRODUCT_ID_B2B = '3755219'
    }
}
