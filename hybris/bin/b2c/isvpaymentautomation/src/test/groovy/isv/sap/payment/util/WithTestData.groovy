package isv.sap.payment.util

import isv.sap.payment.data.TestData

import static io.qala.datagen.RandomShortApi.english
import static isv.sap.payment.data.constants.TestDataConstants.BaseStore.APPAREL_DE
import static isv.sap.payment.data.constants.TestDataConstants.BaseStore.APPAREL_UK
import static isv.sap.payment.data.constants.TestDataConstants.BaseStore.POWERTOOLS
import static isv.sap.payment.data.constants.TestDataConstants.Country.DE
import static isv.sap.payment.data.constants.TestDataConstants.Country.UK
import static isv.sap.payment.data.constants.TestDataConstants.Country.US
import static isv.sap.payment.data.constants.TestDataConstants.CurrencyIsoCode.EUR
import static isv.sap.payment.data.constants.TestDataConstants.CurrencyIsoCode.GBP
import static isv.sap.payment.data.constants.TestDataConstants.CurrencyIsoCode.USD
import static isv.sap.payment.data.constants.TestDataConstants.Product.PRODUCT_ID_B2B
import static isv.sap.payment.data.constants.TestDataConstants.Product.PRODUCT_ID_B2C

trait WithTestData implements WithCredentials
{
    private static final DATE_FORMAT = 'dd-MM-yyyy'
    private static final EMAIL_HOST = '@testemail.xyz'
    private static final DEFAULT_TITLE = 'mr'
    private static final B2B_REGION = 'US-AL'
    private static final UK_POSTCODE = 'W1T 5AG'
    private static final DIGITAL_POSTCODE = '94108'
    private static final LENGTH = 10

    private final deData = new TestData(country: DE,
                                        baseStore: APPAREL_DE,
                                        currency: EUR,
                                        postCode: DIGITAL_POSTCODE,
                                        email: "${english(LENGTH)}$EMAIL_HOST".toLowerCase(),
                                        title: DEFAULT_TITLE,
                                        firstName: english(LENGTH),
                                        lastName: english(LENGTH),
                                        address: english(LENGTH),
                                        city: english(LENGTH),
                                        password: english(LENGTH),
                                        dateOfBirth: '12/12/2000',
                                        today: new Date().toLocalDate().format(DATE_FORMAT),
                                        paymentCode: english(LENGTH),
                                        cartGuid: english(LENGTH),
                                        product: PRODUCT_ID_B2C,)

    private final ukData = new TestData(country: UK,
                                        baseStore: APPAREL_UK,
                                        currency: GBP,
                                        postCode: UK_POSTCODE,
                                        phoneNumber: '7911 123456',
                                        email: "${english(LENGTH)}$EMAIL_HOST".toLowerCase(),
                                        title: DEFAULT_TITLE,
                                        firstName: english(LENGTH),
                                        lastName: english(LENGTH),
                                        address: english(LENGTH),
                                        city: english(LENGTH),
                                        password: english(LENGTH),
                                        dateOfBirth: '12/12/2000',
                                        today: new Date().toLocalDate().format(DATE_FORMAT),
                                        paymentCode: english(LENGTH),
                                        cartGuid: english(LENGTH),
                                        product: PRODUCT_ID_B2C,)

    private final b2bData = new TestData(email: credentials.b2b.email,
                                         password: credentials.b2b.password,
                                         baseStore: POWERTOOLS,
                                         currency: USD,
                                         country: US,
                                         region: B2B_REGION,
                                         title: DEFAULT_TITLE,
                                         firstName: english(LENGTH),
                                         lastName: english(LENGTH),
                                         address: english(LENGTH),
                                         city: english(LENGTH),
                                         postCode: DIGITAL_POSTCODE,
                                         product: PRODUCT_ID_B2B,)

    private final visaData = new TestData(firstName: english(LENGTH),
                                         lastName: english(LENGTH),
                                         address: english(LENGTH),
                                         postCode: UK_POSTCODE,
                                         city: english(LENGTH),
                                         phoneNumber: '7911 123456',
                                         email: "${english(LENGTH)}$EMAIL_HOST".toLowerCase(),)

    private final dataMap = ['uk' : ukData,
                       'de' : deData,
                       'b2b': b2bData,
                       'vco': visaData,]

    TestData getData(String site)
    {
        dataMap[site]
    }
}
