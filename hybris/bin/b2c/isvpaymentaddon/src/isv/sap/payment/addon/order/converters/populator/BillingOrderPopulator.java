package isv.sap.payment.addon.order.converters.populator;

import java.util.Optional;
import javax.annotation.Resource;

import de.hybris.platform.commercefacades.order.converters.populator.OrderPopulator;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.order.data.CardTypeData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;

import isv.sap.payment.addon.facade.PaymentModeFacade;
import isv.sap.payment.model.IsvPaymentTransactionModel;

import static isv.cjl.payment.enums.PaymentType.GOOGLE_PAY;
import static isv.sap.payment.enums.AlternativePaymentMethod.GGP;
import static isv.sap.payment.enums.PaymentType.ALTERNATIVE_PAYMENT;
import static isv.sap.payment.utils.PaymentTransactionUtils.getTransactionWithTheLatestEntry;

public class BillingOrderPopulator extends OrderPopulator
{
    @Resource
    private PaymentModeFacade paymentModeFacade;

    @Override
    public void populate(final OrderModel source, final OrderData target)
    {
        callSuperPopulate(source, target);

        addBillingAddress(source, target);
    }

    private void addBillingAddress(final AbstractOrderModel source, final AbstractOrderData target)
    {
        Optional.ofNullable(source.getPaymentAddress())
                .map(address -> getAddressConverter().convert(address))
                .ifPresent(target::setBillingAddress);
    }

    protected void callSuperPopulate(final OrderModel source, final OrderData target)
    {
        super.populate(source, target);
    }

    @Override
    protected void addPaymentInformation(final AbstractOrderModel source, final AbstractOrderData prototype)
    {
        Optional.ofNullable(source.getPaymentInfo())
                .map(PaymentInfoModel::getBillingAddress)
                .map(addressModel -> getAddressConverter().convert(addressModel))
                .map(addressData -> {
                    final CCPaymentInfoData paymentInfo = new CCPaymentInfoData();
                    paymentInfo.setBillingAddress(addressData);
                    getTransactionWithTheLatestEntry(source.getPaymentTransactions())
                            .flatMap(transaction -> {
                                final String provider = transaction.getPaymentProvider();
                                final String alternativeMethod = transaction instanceof IsvPaymentTransactionModel
                                        && ((IsvPaymentTransactionModel) transaction).getAlternativePaymentMethod() != null
                                        ? ((IsvPaymentTransactionModel) transaction)
                                            .getAlternativePaymentMethod().name()
                                        : null;
                                return paymentModeFacade.getPaymentModes().stream()
                                    .filter(mode -> !ALTERNATIVE_PAYMENT.name().equals(provider)
                                        && mode.getPaymentType().equals(provider)
                                        || ALTERNATIVE_PAYMENT.name().equals(provider)
                                        && mode.getPaymentSubType() != null
                                        && mode.getPaymentSubType().equals(alternativeMethod)
                                        || GOOGLE_PAY.name().equals(provider)
                                        && GGP.name().equals(mode.getPaymentSubType()))
                                        .findFirst();
                            })
                            .ifPresent(paymentMode -> {
                                final CardTypeData cardTypeData = new CardTypeData();
                                cardTypeData.setName(paymentMode.getName());
                                paymentInfo.setCardTypeData(cardTypeData);
                            });
                    return paymentInfo;
                })
                .ifPresent(prototype::setPaymentInfo);
    }
}
