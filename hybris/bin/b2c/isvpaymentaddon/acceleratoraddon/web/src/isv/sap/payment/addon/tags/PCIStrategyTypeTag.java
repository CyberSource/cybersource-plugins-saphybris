package isv.sap.payment.addon.tags;

import java.util.Objects;
import java.util.Optional;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import de.hybris.platform.acceleratorservices.checkout.pci.impl.ConfiguredCheckoutPciStrategy;
import de.hybris.platform.acceleratorservices.enums.CheckoutPciOptionEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class PCIStrategyTypeTag extends TagSupport
{
    private String type;

    @Override
    public int doStartTag() throws JspException
    {
        final CheckoutPciOptionEnum pciType = Optional.ofNullable(type)
                .filter(StringUtils::isNotBlank)
                .map(CheckoutPciOptionEnum::valueOf)
                .orElse(CheckoutPciOptionEnum.DEFAULT);

        return Objects.equals(getConfiguredCheckoutPciStrategy().getSubscriptionPciOption(), pciType)
                ? Tag.EVAL_BODY_INCLUDE
                : Tag.SKIP_BODY;
    }

    protected ConfiguredCheckoutPciStrategy getConfiguredCheckoutPciStrategy()
    {
        final WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(
                pageContext.getServletContext());

        return (ConfiguredCheckoutPciStrategy) applicationContext.getBean("checkoutPciStrategy");
    }

    public void setType(final String type)
    {
        this.type = type;
    }
}
