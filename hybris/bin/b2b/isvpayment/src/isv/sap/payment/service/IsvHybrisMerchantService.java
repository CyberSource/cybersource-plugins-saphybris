package isv.sap.payment.service;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.site.BaseSiteService;

import isv.cjl.payment.enums.MerchantProfileType;
import isv.cjl.payment.enums.PaymentType;
import isv.cjl.payment.model.Merchant;
import isv.cjl.payment.model.MerchantProfile;
import isv.cjl.payment.security.service.CredentialHolderFactory;
import isv.cjl.payment.service.DefaultMerchantService;
import isv.sap.payment.configuration.service.PaymentConfigurationService;
import isv.sap.payment.enums.IsvConfigurationType;
import isv.sap.payment.enums.IsvPaymentChannel;
import isv.sap.payment.model.IsvMerchantModel;
import isv.sap.payment.model.IsvMerchantPaymentConfigurationModel;
import isv.sap.payment.model.IsvMerchantProfileModel;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static isv.cjl.payment.enums.PaymentType.CREDIT_CARD;

/**
 * Implements merchant retrieval logic.
 */
public class IsvHybrisMerchantService extends DefaultMerchantService
{
    private static final String SELECT_ALL_MERCHANTS_QUERY =
            "SELECT {" + IsvMerchantModel.PK + "} FROM {" + IsvMerchantModel._TYPECODE + "}";

    @Resource(name = "baseSiteService")
    private BaseSiteService baseSiteService;

    @Resource(name = "commonI18NService")
    private CommonI18NService commonI18NService;

    @Resource(name = "isv.sap.payment.paymentConfigurationService")
    private PaymentConfigurationService paymentConfigurationService;

    @Resource(name = "flexibleSearchService")
    private FlexibleSearchService flexibleSearchService;

    @Resource(name = "enumerationService")
    private EnumerationService enumerationService;

    @Inject
    private CredentialHolderFactory credentialHolderFactory;

    @Override
    public Merchant getCurrentMerchant(final PaymentType paymentType)
    {
        final IsvMerchantPaymentConfigurationModel paymentConfiguration = paymentConfigurationService
                .getConfiguration(
                        IsvConfigurationType.MERCHANT_CONFIG,
                        ImmutableMap.of(
                                IsvMerchantPaymentConfigurationModel.SITE, getBaseSiteService().getCurrentBaseSite(),
                                IsvMerchantPaymentConfigurationModel.PAYMENTTYPE,
                                getEnumerationService().getEnumerationValue(
                                        isv.sap.payment.enums.PaymentType.class,
                                        paymentType.getCode()),
                                IsvMerchantPaymentConfigurationModel.PAYMENTCHANNEL, IsvPaymentChannel.WEB,
                                IsvMerchantPaymentConfigurationModel.CURRENCY, getCommonI18NService().getCurrentCurrency()
                        )
                );

        return createMerchant(paymentConfiguration.getMerchant());
    }

    @Override
    public Merchant getMerchant(final String merchantId)
    {
        validateParameterNotNull(merchantId, "merchant id must not be null");

        return createMerchant(
                (IsvMerchantModel) getPaymentConfigurationService().getConfiguration(IsvConfigurationType.MERCHANT,
                        ImmutableMap.of(IsvMerchantModel.ID, merchantId)));
    }

    @Override
    public MerchantProfile getCurrentMerchantProfile(final MerchantProfileType profileType)
    {
        return getMerchantProfileForPaymentType(CREDIT_CARD, profileType);
    }

    @Override
    public MerchantProfile getMerchantProfileForPaymentType(final PaymentType paymentType,
            final MerchantProfileType profileType)
    {
        final Merchant merchant = getCurrentMerchant(paymentType);

        return getMerchantProfile(merchant, profileType);
    }

    @Override
    public List<Merchant> getAllMerchants()
    {
        return getFlexibleSearchService()
                .<IsvMerchantModel>search(SELECT_ALL_MERCHANTS_QUERY)
                .getResult().stream().map(this::createMerchant).collect(Collectors.toList());
    }

    protected Merchant createMerchant(final IsvMerchantModel merchantModel)
    {
        final Merchant merchant = new Merchant(
                merchantModel.getId(),
                getCredentialHolderFactory()
                        .getCredentialHolder(merchantModel.getUserName(), merchantModel.getPasswordToken())
        );

        merchant.setProfiles(merchantModel.getProfiles().stream()
                .map(model -> createMerchantProfile(model, merchant))
                .collect(Collectors.toList()));

        return merchant;
    }

    protected MerchantProfile createMerchantProfile(final IsvMerchantProfileModel model,
            final Merchant merchant)
    {
        return new MerchantProfile.MerchantProfileBuilder()
                .setId(model.getId())
                .setProfileId(model.getProfileId())
                .setAccessKey(model.getAccessKey())
                .setSecretKey(model.getSecretKey())
                .setMerchant(merchant)
                .setProfileType(MerchantProfileType.valueOf(model.getProfileType().getCode()))
                .build();
    }

    public CredentialHolderFactory getCredentialHolderFactory()
    {
        return credentialHolderFactory;
    }

    @Override
    public void setCredentialHolderFactory(final CredentialHolderFactory credentialHolderFactory)
    {
        this.credentialHolderFactory = credentialHolderFactory;
    }

    protected BaseSiteService getBaseSiteService()
    {
        return baseSiteService;
    }

    protected CommonI18NService getCommonI18NService()
    {
        return commonI18NService;
    }

    protected PaymentConfigurationService getPaymentConfigurationService()
    {
        return paymentConfigurationService;
    }

    protected FlexibleSearchService getFlexibleSearchService()
    {
        return flexibleSearchService;
    }

    protected EnumerationService getEnumerationService()
    {
        return enumerationService;
    }
}
