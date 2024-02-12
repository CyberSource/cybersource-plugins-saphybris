package isv.sap.payment.addon.tags

import javax.servlet.jsp.tagext.Tag

import de.hybris.bootstrap.annotations.UnitTest
import de.hybris.platform.acceleratorservices.checkout.pci.impl.ConfiguredCheckoutPciStrategy
import de.hybris.platform.acceleratorservices.enums.CheckoutPciOptionEnum
import org.junit.Test
import spock.lang.Specification

@UnitTest
class PCIStrategyTypeTagSpec extends Specification
{
    def configuredCheckoutPciStrategy = Mock([useObjenesis: false], ConfiguredCheckoutPciStrategy)

    def tag = Spy(PCIStrategyTypeTag, constructorArgs: [])

    def setup()
    {
        tag.configuredCheckoutPciStrategy >> configuredCheckoutPciStrategy
        configuredCheckoutPciStrategy.subscriptionPciOption >> CheckoutPciOptionEnum.HOP
    }

    @Test
    def 'tag should evaluate and include body'()
    {
        when:
        tag.type = CheckoutPciOptionEnum.HOP

        then:
        tag.doStartTag() == Tag.EVAL_BODY_INCLUDE
    }

    @Test
    def 'tag should evaluate and skip body'()
    {
        when:
        tag.type = CheckoutPciOptionEnum.SOP

        then:
        tag.doStartTag() == Tag.SKIP_BODY
    }
}
