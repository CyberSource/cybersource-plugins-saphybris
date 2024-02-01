package isv.sap.payment.addon.visacheckout.populator

import de.hybris.bootstrap.annotations.UnitTest
import org.junit.Test
import spock.lang.Specification

import isv.sap.payment.addon.VisaCheckoutPaymentDetailsData
import isv.sap.payment.model.IsvPaymentTransactionEntryModel

@UnitTest
class VisaCheckoutPaymentDetailsPopulatorSpec extends Specification
{
    def source = Mock([useObjenesis: false], IsvPaymentTransactionEntryModel)

    @Test
    def 'should populate visa checkout payment details data'()
    {
        given:
        def target = new VisaCheckoutPaymentDetailsData()
        source.properties >> [billToName            : 'bill_to_name',
                              vcReplyCardArtFileName: 'card_art',
                              cardSuffix            : 'card_suffix',
                              vcReplyCardType       : 'card_type',
                              cardExpirationMonth   : 'card_expiration_month',
                              cardExpirationYear    : 'card_expiration_year']
        def props = source.properties

        when:
        new VisaCheckoutPaymentDetailsPopulator().populate(source, target)

        then:
        target.with {
            billToName == props.billToName
            cardArt == props.vcReplyCardArtFileName
            cardSuffix == props.cardSuffix
            cardType == props.vcReplyCardType
            cardExpirationMonth == props.cardExpirationMonth
            cardExpirationYear == props.cardExpirationYear
        }
    }
}
