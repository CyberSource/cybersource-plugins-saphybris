/*
 * [y] hybris Platform
 *
 * Copyright (c) 2017 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package isv.sap.payment.addon.b2c.controllers.pages;

import javax.annotation.Resource;

import de.hybris.platform.acceleratorservices.controllers.page.PageType;
import de.hybris.platform.acceleratorstorefrontcommons.breadcrumb.ResourceBreadcrumbBuilder;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.forms.VoucherForm;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.util.Config;
import de.hybris.platform.yb2cacceleratorstorefront.controllers.pages.CartPageController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import isv.cjl.payment.enums.MerchantProfileType;
import isv.cjl.payment.enums.PaymentType;
import isv.cjl.payment.service.MerchantService;
import isv.sap.payment.addon.facade.PaymentModeFacade;

/**
 * Controller for cart page
 */
@RequestMapping(value = "/cart")
public class IsvCartPageController extends CartPageController
{
    @Resource(name = "simpleBreadcrumbBuilder")
    private ResourceBreadcrumbBuilder resourceBreadcrumbBuilder;

    @Resource
    private PaymentModeFacade paymentModeFacade;

    @Value("${isv.payment.clicktopay.checkout.image.url}")
    private String visaCheckoutImageUrl;

    @Value("${isv.payment.clicktopay.checkout.sdk.url}")
    private String visaCheckoutSDKUrl;

    @Resource(name = "isv.sap.payment.merchantService")
    private MerchantService merchantService;

    @Resource
    private I18NService i18NService;

    @Override
    protected void prepareDataForPage(final Model model) throws CMSItemNotFoundException
    {
        super.prepareDataForPage(model);

        if (!model.containsAttribute(VOUCHER_FORM))
        {
            model.addAttribute(VOUCHER_FORM, new VoucherForm());
        }

        // Because DefaultSiteConfigService.getProperty() doesn't set default boolean value for undefined property,
        // this property key was generated to use Config.getBoolean() method
        final String siteQuoteProperty = SITE_QUOTES_ENABLED.concat(getBaseSiteService().getCurrentBaseSite().getUid());
        model.addAttribute("siteQuoteEnabled", Config.getBoolean(siteQuoteProperty, Boolean.FALSE));
        model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("breadcrumb.cart"));
        model.addAttribute("pageType", PageType.CART.name());

        prepareVisaCheckoutData(model);
    }

    protected void prepareVisaCheckoutData(final Model model)
    {
        final boolean visaCheckoutEnabled = paymentModeFacade.isPaymentModeSupported(
                isv.sap.payment.enums.PaymentType.VISA_CHECKOUT, null);

        if (visaCheckoutEnabled && !getUserFacade().isAnonymousUser())
        {
            model.addAttribute("visaCheckoutEnabled", true);
            model.addAttribute("visaCheckoutImageUrl", visaCheckoutImageUrl);
            model.addAttribute("visaCheckoutAPIKey", merchantService.getMerchantProfileForPaymentType(
                    PaymentType.VISA_CHECKOUT, MerchantProfileType.VCO).getAccessKey());
            model.addAttribute("visaCheckoutSDKUrl", visaCheckoutSDKUrl);
            model.addAttribute("locale", i18NService.getCurrentLocale().toString());
        }
    }
}
