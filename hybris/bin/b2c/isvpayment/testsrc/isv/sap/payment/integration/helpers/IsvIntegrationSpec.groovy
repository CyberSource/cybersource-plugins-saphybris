package isv.sap.payment.integration.helpers

import javax.annotation.Resource

import de.hybris.bootstrap.config.ConfigUtil
import de.hybris.platform.core.Registry
import de.hybris.platform.core.enums.CreditCardType
import de.hybris.platform.core.model.c2l.CountryModel
import de.hybris.platform.core.model.c2l.CurrencyModel
import de.hybris.platform.core.model.c2l.RegionModel
import de.hybris.platform.core.model.order.AbstractOrderEntryModel
import de.hybris.platform.core.model.order.AbstractOrderModel
import de.hybris.platform.core.model.order.delivery.DeliveryModeModel
import de.hybris.platform.core.model.order.payment.PaymentInfoModel
import de.hybris.platform.core.model.product.ProductModel
import de.hybris.platform.core.model.product.UnitModel
import de.hybris.platform.core.model.user.AddressModel
import de.hybris.platform.payment.dto.CardInfo
import de.hybris.platform.payment.enums.PaymentTransactionType
import de.hybris.platform.servicelayer.model.ModelService
import de.hybris.platform.task.impl.DefaultTaskService
import spock.lang.Retry

import isv.cjl.payment.enums.CardType
import isv.cjl.payment.service.executor.PaymentServiceExecutor
import isv.sap.payment.configuration.service.PaymentConfigurationService
import isv.sap.payment.enums.AlternativePaymentMethod
import isv.sap.payment.enums.IsvConfigurationType
import isv.sap.payment.enums.PaymentType
import isv.sap.payment.model.IsvMerchantModel
import isv.sap.payment.model.IsvPaymentTransactionEntryModel
import isv.sap.payment.model.IsvPaymentTransactionModel
import isv.sap.payment.service.IsvHybrisMerchantService
import isv.sap.payment.service.PaymentTransactionService
import isv.sap.payment.service.transaction.PersistentPaymentTransactionCreator

import static io.qala.datagen.RandomShortApi.alphanumeric
import static io.qala.datagen.RandomShortApi.english
import static io.qala.datagen.RandomShortApi.integer
import static io.qala.datagen.RandomShortApi.numeric
import static io.qala.datagen.RandomShortApi.specialSymbols
import static io.qala.datagen.RandomValue.length
import static io.qala.datagen.StringModifier.Impls.spaces

@Retry(count = 1)
@SuppressWarnings('MethodCount')
class IsvIntegrationSpec extends HybrisIntegrationSpec
{
    def configDir = ConfigUtil.getPlatformConfig(this.getClass()).systemConfig.getDir('HYBRIS_CONFIG_DIR')
    def testConfig = new TestConfig().getTestConfig(configDir)

    PayPalApTransactionsCreator ppApTransactionCreator
    PayPalSoTransactionsCreator ppSoTransactionCreator
    GooglePayTransactionsCreator googlePayTransactionsCreator
    AlternativePaymentTransactionsCreator apTransactionCreator
    VisaCheckoutTransactionsCreator vcTransactionCreator
    ApplePayTransactionsCreator applePayTransactionsCreator
    CreditCardTransactionsCreator ccTransactionCreator
    ImpexImporter impexImporter

    @Resource(name = 'isv.sap.payment.paymentServiceExecutor')
    PaymentServiceExecutor paymentServiceExecutor

    @Resource(name = 'isv.sap.payment.defaultPersistentPaymentTransactionCreator')
    PersistentPaymentTransactionCreator persistentPaymentTransactionCreator

    @Resource(name = 'isv.sap.payment.paymentTransactionService')
    PaymentTransactionService paymentTransactionService

    @Resource(name = 'isv.sap.payment.merchantService')
    IsvHybrisMerchantService merchantService

    def setup()
    {
        def paymentConfigurationService = Mock(PaymentConfigurationService)
        merchantService.paymentConfigurationService = paymentConfigurationService

        _ * paymentConfigurationService.getConfiguration(_ as IsvConfigurationType, _ as Map<String, Object>) >> new IsvMerchantModel(
                id: testConfig.merchant,
                userName: testConfig.userName,
                passwordToken: testConfig.token,
                profiles: []
        )

        stubModelService(persistentPaymentTransactionCreator)

        ccTransactionCreator = new CreditCardTransactionsCreator(
                paymentServiceExecutor: paymentServiceExecutor,
                testConfig: testConfig,
                paymentTransactionService: paymentTransactionService
        )

        ppApTransactionCreator = new PayPalApTransactionsCreator(
                paymentServiceExecutor: paymentServiceExecutor,
                testConfig: testConfig,
                paymentTransactionService: paymentTransactionService
        )

        ppSoTransactionCreator = new PayPalSoTransactionsCreator(
                paymentServiceExecutor: paymentServiceExecutor,
                testConfig: testConfig,
                paymentTransactionService: paymentTransactionService
        )

        apTransactionCreator = new AlternativePaymentTransactionsCreator(
                paymentServiceExecutor: paymentServiceExecutor,
                testConfig: testConfig,
                paymentTransactionService: paymentTransactionService
        )

        vcTransactionCreator = new VisaCheckoutTransactionsCreator(
                paymentServiceExecutor: paymentServiceExecutor,
                testConfig: testConfig,
                paymentTransactionService: paymentTransactionService
        )

        applePayTransactionsCreator = new ApplePayTransactionsCreator(
                paymentServiceExecutor: paymentServiceExecutor,
                testConfig: testConfig,
                paymentTransactionService: paymentTransactionService
        )

        googlePayTransactionsCreator = new GooglePayTransactionsCreator(
                paymentServiceExecutor: paymentServiceExecutor,
                testConfig: testConfig,
                paymentTransactionService: paymentTransactionService
        )

        impexImporter = new ImpexImporter()
        impexImporter.importCurrency()

        // Stopping the task engine, we do not need it during integration tests
        try
        {
            DefaultTaskService service = (DefaultTaskService) Registry.applicationContext.getBean('taskService')
            service.destroy()
        }
        catch (e)
        {
            e.printStackTrace()
        }
    }

    def switchMerchantConfig(String merchantRegion)
    {
        testConfig = new TestConfig().getTestConfig(configDir, merchantRegion)
        setup()
    }

    //region Stubs
    def stubModelService(paymentTransactionCreator)
    {
        def modelService = Mock(ModelService)
//        modelServiceBkp = paymentTransactionCreator.modelService
        paymentTransactionCreator.modelService = modelService

        _ * modelService.create(IsvPaymentTransactionModel) >> new IsvPaymentTransactionModel(entries: Mock(List))
        _ * modelService.saveAll(_ as IsvPaymentTransactionModel, _ as AbstractOrderModel)
        _ * modelService.create(IsvPaymentTransactionEntryModel) >> { args ->
            new IsvPaymentTransactionEntryModel()
        }
        _ * modelService.saveAll(_ as IsvPaymentTransactionEntryModel, _ as IsvPaymentTransactionModel)
    }

    //endregion

    //region DataGen

    def testCartUk(totalPrice = null)
    {
        def cart = testOrder(totalPrice)

        cart.deliveryAddress >> addressUk()
        cart.paymentAddress >> addressUk()
        cart.deliveryMode >> deliveryMode()
        cart.paymentInfo >> paymentInfo()
        cart.currency >> testCurrency('EUR')
        cart.entries.first().order >> cart

        cart
    }

    def testCartDe(totalPrice = null)
    {
        def cart = testOrder(totalPrice)

        cart.deliveryAddress >> DEAddress
        cart.paymentAddress >> DEAddress
        cart.deliveryMode >> deliveryMode()
        cart.paymentInfo >> paymentInfo('DE')
        cart.currency >> testCurrency('EUR')
        cart.entries.first().order >> cart

        cart
    }

    def testCartForReview()
    {
        def cart = testOrder()

        cart.deliveryAddress >> addressForReview()
        cart.deliveryMode >> deliveryMode()
        cart.paymentInfo >> paymentInfoForReview()
        cart.currency >> testCurrency('EUR')

        cart
    }

    def testCartWithErrorCode()
    {
        def order = testOrder(testConfig.processorErrorTrigger)

        order.paymentInfo >> paymentInfo()
        order.currency >> testCurrency('EUR')

        order
    }

    def testCartInvalidFields()
    {
        def order = testOrder()

        order.deliveryAddress >> invalidAddress()
        order.deliveryMode >> deliveryMode()
        order.paymentInfo >> paymentInfoInvalidFields()
        order.currency >> testCurrency('EUR')

        order
    }

    def testCartMissingFields()
    {
        def order = testOrder()

        order.paymentAddress >> addressWithEmptyRegion()
        order.deliveryAddress >> addressWithEmptyRegion()
        order.deliveryMode >> deliveryMode()
        order.paymentInfo >> paymentInfoMissingFields()
        order.currency >> testCurrency('EUR')

        order
    }

    AbstractOrderModel testCartWithoutSensitivePersonalData(String country)
    {
        String currency
        switch (country)
        {
            case 'DE':
                currency = 'EUR'
                break
            case 'US':
                currency = 'USD'
                break
            case 'UK':
            default:
                currency = 'GBP'
                break
        }

        def order = testOrder()
        order.currency >> testCurrency(currency)
        order.deliveryMode >> deliveryMode()
        order.paymentInfo >> paymentInfoWithoutSensitivePersonalData(country)
        order.entries.first().order >> order

        order
    }

    AbstractOrderModel testOrderFor(String country)
    {
        switch (country)
        {
            case ('DE'):
                testOrderDe()
                break
            case ('US'):
                testOrderUs()
                break
            case ('CA'):
                testOrderCa()
                break
            case ('UK'):
            default:
                testOrderUk()
                break
        }
    }

    AbstractOrderModel testOrderUk()
    {
        def order = testOrder()

        order.currency >> testCurrency('GBP')
        order.paymentAddress >> addressUk()
        order.deliveryAddress >> addressUk()
        order.deliveryMode >> deliveryMode()
        order.paymentInfo >> paymentInfo()
        order.entries.first().order >> order

        order
    }

    def testOrderUs()
    {
        def order = testOrder(null)

        order.currency >> testCurrency('USD')
        order.paymentAddress >> addressUs()
        order.deliveryAddress >> addressUs()
        order.deliveryMode >> deliveryMode()
        order.paymentInfo >> paymentInfo('US')
        order.entries.first().order >> order

        order
    }

    def testOrderDe()
    {
        def order = testOrder()

        order.currency >> testCurrency('EUR')
        order.paymentAddress >> DEAddress
        order.deliveryAddress >> DEAddress
        order.paymentInfo >> paymentInfo()
        order.entries.first().order >> order

        order
    }

    def testOrderCa()
    {
        def order = testOrder()

        order.currency >> testCurrency('USD')
        order.paymentAddress >> CAAddress
        order.deliveryAddress >> CAAddress
        order.paymentInfo >> paymentInfo()
        order.entries.first().order >> order

        order
    }

    def testOrderInvalidFields()
    {
        def order = testOrder()

        order.paymentAddress >> invalidAddress()
        order.deliveryAddress >> invalidAddress()
        order.deliveryMode >> deliveryMode()
        order.paymentInfo >> paymentInfo()
        order.currency >> testCurrency('EUR')

        order
    }

    def testOrderWithErrorCode()
    {
        def order = testOrder(testConfig.processorErrorTrigger)

        order.paymentAddress >> addressUk()
        order.currency >> testCurrency('EUR')

        order
    }

    def testOrderMissingFields()
    {
        def order = testOrder()

        order.paymentAddress >> addressWithEmptyRegion()
        order.deliveryAddress >> addressWithEmptyRegion()
        order.deliveryMode >> deliveryMode()
        order.paymentInfo >> paymentInfo()
        order.currency >> testCurrency('EUR')
        order.entries.first().order >> order

        order
    }

    def testOrderNonCompliantAddress()
    {
        def order = testOrder()

        order.deliveryAddress >> nonCompliantAddress
        order.paymentAddress >> nonCompliantAddress
        order.deliveryMode >> deliveryMode()
        order.paymentInfo >> paymentInfoNonCompliantAddress()
        order.currency >> testCurrency('EUR')

        order
    }

    def testOrderNonCompliantName()
    {
        def order = testOrder()

        order.deliveryAddress >> addressWithNonCompliantName
        order.deliveryMode >> deliveryMode()
        order.paymentInfo >> paymentInfoNonCompliantName()
        order.currency >> testCurrency('EUR')

        order
    }

    def testVisaCheckoutOrderId()
    {
        numeric(12)
    }

    def testKlarnaPreauthorizationToken()
    {
        alphanumeric(10, 25)
    }

    def testVisaCheckoutPARes()
    {
        def prefix = alphanumeric(50, 100)
        def suffix = specialSymbols(5, 10)
        prefix + suffix
    }

    def getUKAddress(addressType = 'VALID')
    {
        def firstName = testConfig.nameReviewTrigger
        def addressLine1 = getAddressLine('UK', addressType)
        def address = Mock([useObjenesis: false], AddressModel)

        address.town >> 'London'
        address.line1 >> addressLine1
        address.firstname >> firstName
        address.lastname >> 'LN' + english(10)
        address.email >> 'test@test.com'
        address.postalcode >> 'SW10 0UJ'
        address.country >> testCountry('UK')
        address.streetname >> addressLine1

        address
    }

    def getUSAddress(addressType = 'VALID')
    {
        def firstName = testConfig.nameReviewTrigger
        def zipCode = (addressType == 'INVALID') ? '99099' : '94108'
        def addressLine1 = getAddressLine('US', addressType)
        def address = Mock([useObjenesis: false], AddressModel)

        address.town >> 'San Fransisco'
        address.line1 >> addressLine1
        address.firstname >> firstName
        address.lastname >> 'LN' + english(10)
        address.region >> testRegion()
        address.email >> 'test@test.com'
        address.postalcode >> zipCode
        address.country >> testCountry('US')
        address.streetname >> addressLine1

        address
    }

    def getDEAddress(addressType = 'VALID')
    {
        def firstName = testConfig.nameReviewTrigger
        def addressLine1 = getAddressLine('DE', addressType)
        def address = Mock([useObjenesis: false], AddressModel)

        address.town >> 'Frankfurt'
        address.line1 >> addressLine1
        address.firstname >> firstName
        address.lastname >> 'LN' + english(10)
        address.email >> 'test@test.com'
        address.postalcode >> '60313'
        address.country >> testCountry('DE')
        address.streetname >> addressLine1

        address
    }

    def getCAAddress(addressType = 'VALID')
    {
        def firstName = testConfig.nameReviewTrigger
        def zipCode = (addressType == 'INVALID') ? '91032' : 'M5B 2G8'
        def addressLine1 = getAddressLine('CA', addressType)
        def address = Mock([useObjenesis: false], AddressModel)

        address.town >> 'Toronto'
        address.line1 >> addressLine1
        address.firstname >> firstName
        address.lastname >> 'LN' + english(10)
        address.email >> 'test@test.com'
        address.region >> testRegion('ON')
        address.postalcode >> zipCode
        address.country >> testCountry('CA')
        address.streetname >> addressLine1

        address
    }

    def getNonCompliantAddress()
    {
        def address = Mock([useObjenesis: false], AddressModel)

        address.town >> 'Mountain View'
        address.line1 >> '1234 First St.'
        address.firstname >> 'Maria'
        address.lastname >> 'Rodriguez'
        address.email >> 'maria@example.com'
        address.phone1 >> '650-965-6000'
        address.postalcode >> '94043'
        address.country >> testCountry('US')
        address.streetname >> '1234 First St.'
        address.region >> testRegion()

        address
    }

    def getAddressWithNonCompliantName()
    {
        def address = Mock([useObjenesis: false], AddressModel)

        address.town >> 'San Fransisco'
        address.line1 >> 'Maiden Lane 27'
        address.firstname >> 'Mohammed'
        address.lastname >> 'QASIM'
        address.email >> 'test@test.com'
        address.postalcode >> '94108'
        address.country >> testCountry('UK')
        address.streetname >> 'Maiden Lane 27'

        address
    }

    CardInfo testCard(cardType = CardType.VISA)
    {
        def card = Mock([useObjenesis: false], CardInfo)

        card.cardHolderFullName >> 'Name' + alphanumeric(10)
        card.expirationMonth >> 12
        card.expirationYear >> 2025
        switch (cardType)
        {
            case CardType.VISA:
                testVisaNumber(card)
                break
            case CardType.MASTERCARD_EUROCARD:
                testMasterCardNumber(card)
                break
            case CardType.AMEX:
                testAmexNumber(card)
                break
            default:
                break
        }
        card
    }

    def wrongTestCard()
    {
        def card = Mock([useObjenesis: false], CardInfo)

        card.cardHolderFullName >> 'Name' + alphanumeric(10)
        card.cardType >> CreditCardType.VISA
        card.cardNumber >> '4111111111111110'
        card.cv2Number >> '111'
        card.expirationMonth >> 12
        card.expirationYear >> 2025

        card
    }

    def testCreditCardTransaction(type = null)
    {
        def transaction = Mock([useObjenesis: false], IsvPaymentTransactionModel)

        transaction.paymentProvider >> PaymentType.CREDIT_CARD.name()
        transaction.code >> alphanumeric(10)
        transaction.entries >> [testTransactionEntry(type)]

        transaction
    }

    def testPayPalTransaction(type)
    {
        def transaction = Mock([useObjenesis: false], IsvPaymentTransactionModel)

        transaction.paymentProvider >> PaymentType.PAY_PAL.name()
        transaction.code >> alphanumeric(10)
        transaction.entries >> [testTransactionEntry(type)]

        transaction
    }

    def testAlternativePaymentTransaction(AlternativePaymentMethod method, PaymentTransactionType type)
    {
        def transaction = Mock([useObjenesis: false], IsvPaymentTransactionModel)

        transaction.paymentProvider >> PaymentType.ALTERNATIVE_PAYMENT.name()
        transaction.alternativePaymentMethod >> method
        transaction.code >> alphanumeric(10)
        transaction.entries >> [testTransactionEntry(type)]

        transaction
    }

    def randomSubscriptionId()
    {
        alphanumeric(10)
    }

    def addCardTypeToOrder(order, cardTypeCode)
    {
        order.paymentTransactions.first().entries[0].properties.put('req_card_type', cardTypeCode)
        order
    }

    private AbstractOrderModel testOrder(totalPrice = null)
    {
        def order = Mock([useObjenesis: false], AbstractOrderModel)
        order.paymentTransactions >> []
        order.code >> alphanumeric(10)
        order.guid >> alphanumeric(10)
        order.entries >> [testOrderItem()]
        order.deliveryCost >> 0.00
        order.totalDiscounts >> 0.00
        if (totalPrice)
        {
            order.totalPrice >> totalPrice
        }
        else
        {
            order.totalPrice >> order.entries.first().totalPrice
        }
        order
    }

    private addressUk()
    {
        def address = testAddress()

        address.country >> testCountry('UK')
        address.postalcode >> 'W1T 5AG'
        address.phone1 >> '+447911123456'

        address
    }
    private addressForReview()
    {
        def address = testAddress(true)

        address.country >> testCountry('UK')

        address
    }

    private addressUs()
    {
        def address = testAddress()

        address.country >> testCountry('US')
        address.region >> testRegion()
        address.postalcode >> '94111'

        address
    }

    private addressWithEmptyRegion()
    {
        def address = testAddress()

        address.postalcode >> '94111'
        address.country >> testCountry('US')

        address
    }

    private invalidAddress()
    {
        def address = testAddress()

        address.country >> testCountry('ZZ')

        address
    }

    private testRegion(state = 'CA')
    {
        def region = Mock([useObjenesis: false], RegionModel)

        region.isocode >> state
        region.isocodeShort >> state

        region
    }

    private testAddress(forReview = false)
    {
        def firstName = forReview ? testConfig.nameReviewTrigger : 'FN' + english(10)
        def address = Mock([useObjenesis: false], AddressModel)

        address.town >> 'town' + english(10)
        address.line1 >> 'Address' + length(5).with(spaces()).alphanumeric()
        address.firstname >> firstName
        address.lastname >> 'LN' + english(10)
        address.email >> 'test@test.com'
        address.streetname >> 'Address' + length(5).with(spaces()).alphanumeric()

        address
    }

    private getAddressLine(locale, addressType)
    {
        def addressLine1 = ''
        switch (addressType)
        {
            case 'VALID':
                switch (locale)
                {
                    case 'UK':
                        addressLine1 = '40 Gunter Grove'
                        break
                    case 'US':
                        addressLine1 = '27 Maiden Lane'
                        break
                    case 'DE':
                        addressLine1 = 'Große Friedberger Str. 14'
                        break
                    case 'CA':
                        addressLine1 = '65 Dundas Street'
                        break
                }
                break
            case 'INVALID':
                addressLine1 = '00 Non Existing Street'
                break
            case 'MISSING_FIELDS':
                addressLine1 = 'A'
                break
        }
        addressLine1
    }

    private testTransactionEntry(type)
    {
        def typeName = (type ?: PaymentTransactionType.AUTHORIZATION).name()
        def transactionEntry = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

        transactionEntry.requestId >> numeric(22)
        transactionEntry.currency >> testCurrency('GBP')
        transactionEntry.requestToken >> alphanumeric(10)
        transactionEntry.amount >> integer(1000)
        transactionEntry.transactionStatus >> 'ACCEPT'
        transactionEntry.type >> typeName
        transactionEntry.code >> alphanumeric(10)

        transactionEntry.properties >> payPalProperties()

        transactionEntry
    }

    private payPalProperties()
    {
        def properties = Mock(Map)

        properties.get('payPalEcSetReplyPaypalToken') >> alphanumeric(10)

        properties
    }

    private testCurrency(currencyCode = 'EUR')
    {
        def currency = Mock([useObjenesis: false], CurrencyModel)

        currency.isocode >> currencyCode
        currency.digits >> 2

        currency
    }

    private testCountry(country)
    {
        def testCountry = Mock([useObjenesis: false], CountryModel)

        testCountry.isocode >> country

        testCountry
    }

    private testOrderItem()
    {
        def orderItem = Mock([useObjenesis: false], AbstractOrderEntryModel)

        orderItem.product >> testProduct()
        orderItem.quantity >> integer(1, 10)
        orderItem.basePrice >> integer(10, 99)
        orderItem.totalPrice >> orderItem.quantity * orderItem.basePrice
        orderItem.entryNumber >> 0

        orderItem
    }

    private testProduct()
    {
        def product = Mock([useObjenesis: false], ProductModel)
        def unit = Mock([useObjenesis: false], UnitModel)

        unit.name >> 'unit'
        unit.code >> 'PBC'

        product.unit >> unit
        product.code >> alphanumeric(10)
        product.name >> alphanumeric(10)

        product
    }

    private deliveryMode()
    {
        def deliveryMode = Mock([useObjenesis: false], DeliveryModeModel)

        deliveryMode.name >> alphanumeric(10)

        deliveryMode
    }

    private paymentInfo(locale = 'UK')
    {
        def paymentInfo = Mock([useObjenesis: false], PaymentInfoModel)
        def address
        switch (locale)
        {
            case 'DE':
                address = DEAddress
                break
            case 'US':
                address = addressUs()
                break
            default:
                address = addressUk()
                break
        }
        paymentInfo.billingAddress >> address

        paymentInfo
    }

    private paymentInfoWithoutSensitivePersonalData(country)
    {
        def address = Mock([useObjenesis: false], AddressModel)
        address.country >> testCountry(country)

        if (country == 'US')
        {
            address.region >> testRegion()
            address.postalcode >> '94111'
        }

        def paymentInfo = Mock([useObjenesis: false], PaymentInfoModel)
        paymentInfo.billingAddress >> address

        paymentInfo
    }

    private paymentInfoNonCompliantAddress()
    {
        def paymentInfo = Mock([useObjenesis: false], PaymentInfoModel)

        paymentInfo.billingAddress >> nonCompliantAddress

        paymentInfo
    }

    private paymentInfoNonCompliantName()
    {
        def paymentInfo = Mock([useObjenesis: false], PaymentInfoModel)

        paymentInfo.billingAddress >> addressWithNonCompliantName

        paymentInfo
    }

    private paymentInfoForReview()
    {
        def paymentInfo = Mock([useObjenesis: false], PaymentInfoModel)

        paymentInfo.billingAddress >> addressForReview()

        paymentInfo
    }

    private paymentInfoInvalidFields()
    {
        def paymentInfo = Mock([useObjenesis: false], PaymentInfoModel)

        paymentInfo.billingAddress >> invalidAddress()

        paymentInfo
    }

    private paymentInfoMissingFields()
    {
        def paymentInfo = Mock([useObjenesis: false], PaymentInfoModel)

        paymentInfo.billingAddress >> addressWithEmptyRegion()

        paymentInfo
    }

    private testVisaNumber(card)
    {
        card.cardType >> CreditCardType.VISA
        card.cardNumber >> '4111111111111111'
        card.cv2Number >> '111'

        card
    }

    private testMasterCardNumber(card)
    {
        card.cardType >> CreditCardType.MASTERCARD_EUROCARD
        card.cardNumber >> '5555555555554444'
        card.cv2Number >> '111'

        card
    }

    private testAmexNumber(card)
    {
        card.cardType >> CreditCardType.AMEX
        card.cardNumber >> '378282246310005'
        card.cv2Number >> '1111'

        card
    }
    //endregion
}
