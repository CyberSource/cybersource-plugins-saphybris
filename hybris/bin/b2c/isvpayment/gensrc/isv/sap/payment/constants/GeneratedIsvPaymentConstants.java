/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * --- Generated at Nov 23, 2022, 6:51:10 PM                    ---
 * ----------------------------------------------------------------
 */
package isv.sap.payment.constants;

/**
 * @deprecated since ages - use constants in Model classes instead
 */
@Deprecated(since = "ages", forRemoval = false)
@SuppressWarnings({"unused","cast"})
public class GeneratedIsvPaymentConstants
{
	public static final String EXTENSIONNAME = "isvpayment";
	public static class TC
	{
		public static final String ALTERNATIVEPAYMENTMETHOD = "AlternativePaymentMethod".intern();
		public static final String ISVALTERNATIVEPAYMENTINFO = "IsvAlternativePaymentInfo".intern();
		public static final String ISVALTERNATIVEPAYMENTOPTION = "IsvAlternativePaymentOption".intern();
		public static final String ISVALTERNATIVEPAYMENTOPTIONSCRONJOB = "IsvAlternativePaymentOptionsCronJob".intern();
		public static final String ISVALTERNATIVEPAYMENTSTATUS = "IsvAlternativePaymentStatus".intern();
		public static final String ISVALTERNATIVEPAYMENTUPDATEORDERSTATUSJOB = "IsvAlternativePaymentUpdateOrderStatusJob".intern();
		public static final String ISVCHECKALTERNATIVEPAYMENTSTATUSCONFIGURATION = "IsvCheckAlternativePaymentStatusConfiguration".intern();
		public static final String ISVCONFIGURATIONTYPE = "IsvConfigurationType".intern();
		public static final String ISVCONVERSIONREPORTPOLLINGCRONJOB = "IsvConversionReportPollingCronJob".intern();
		public static final String ISVMERCHANT = "IsvMerchant".intern();
		public static final String ISVMERCHANTPAYMENTCONFIGURATION = "IsvMerchantPaymentConfiguration".intern();
		public static final String ISVMERCHANTPROFILE = "IsvMerchantProfile".intern();
		public static final String ISVPAYMENTCHANNEL = "IsvPaymentChannel".intern();
		public static final String ISVPAYMENTCONFIGURATION = "IsvPaymentConfiguration".intern();
		public static final String ISVPAYMENTINFO = "IsvPaymentInfo".intern();
		public static final String ISVPAYMENTMODE = "IsvPaymentMode".intern();
		public static final String ISVPAYMENTSOURCE = "IsvPaymentSource".intern();
		public static final String ISVPAYMENTTRANSACTION = "IsvPaymentTransaction".intern();
		public static final String ISVPAYMENTTRANSACTIONENTRY = "IsvPaymentTransactionEntry".intern();
		public static final String ISVPAYPALPAYMENTINFO = "IsvPayPalPaymentInfo".intern();
		public static final String ISVVISACHECKOUTPAYMENTINFO = "IsvVisaCheckoutPaymentInfo".intern();
		public static final String MERCHANTPROFILETYPE = "MerchantProfileType".intern();
		public static final String PAYMENTTYPE = "PaymentType".intern();
	}
	public static class Attributes
	{
		public static class AbstractOrder
		{
			public static final String FINGERPRINTSESSIONID = "fingerPrintSessionID".intern();
		}
	}
	public static class Enumerations
	{
		public static class AlternativePaymentMethod
		{
			public static final String IDL = "IDL".intern();
			public static final String MCH = "MCH".intern();
			public static final String SOF = "SOF".intern();
			public static final String APY = "APY".intern();
			public static final String AYM = "AYM".intern();
			public static final String KLI = "KLI".intern();
			public static final String PPL = "PPL".intern();
			public static final String APP = "APP".intern();
			public static final String GGP = "GGP".intern();
			public static final String WQR = "WQR".intern();
		}
		public static class CheckoutPciOptionEnum
		{
			public static final String FLEX = "FLEX".intern();
		}
		public static class CreditCardType
		{
			public static final String DISCOVER = "discover".intern();
			public static final String CARTE_BANCAIRE = "carte_bancaire".intern();
		}
		public static class IsvAlternativePaymentStatus
		{
			public static final String PENDING = "PENDING".intern();
			public static final String SETTLED = "SETTLED".intern();
			public static final String CAPTURED = "CAPTURED".intern();
			public static final String AUTHORIZED = "AUTHORIZED".intern();
			public static final String ABANDONED = "ABANDONED".intern();
			public static final String FAILED = "FAILED".intern();
			public static final String REJECTED = "REJECTED".intern();
			public static final String COMPLETED = "COMPLETED".intern();
			public static final String REFUNDED = "REFUNDED".intern();
			public static final String AUTH_REVERSED = "AUTH_REVERSED".intern();
			public static final String CREATED = "CREATED".intern();
			public static final String EXPIRED = "EXPIRED".intern();
			public static final String DISPUTED = "DISPUTED".intern();
		}
		public static class IsvConfigurationType
		{
			public static final String MERCHANT = "MERCHANT".intern();
			public static final String MERCHANT_CONFIG = "MERCHANT_CONFIG".intern();
			public static final String PAYMENT_CONFIG = "PAYMENT_CONFIG".intern();
			public static final String ALTERNATIVE_PAYMENT_CONFIG = "ALTERNATIVE_PAYMENT_CONFIG".intern();
		}
		public static class IsvPaymentChannel
		{
			public static final String WEB = "WEB".intern();
			public static final String BACKOFFICE = "BACKOFFICE".intern();
		}
		public static class IsvPaymentSource
		{
			public static final String SECURE_ACCEPTANCE = "SECURE_ACCEPTANCE".intern();
			public static final String SERVICE_API = "SERVICE_API".intern();
			public static final String REPORTING = "REPORTING".intern();
		}
		public static class MerchantProfileType
		{
			public static final String SOP = "SOP".intern();
			public static final String HOP = "HOP".intern();
			public static final String VCO = "VCO".intern();
		}
		public static class OrderStatus
		{
			public static final String FRAUD = "FRAUD".intern();
			public static final String FRAUD_DECISION = "FRAUD_DECISION".intern();
			public static final String WAITING_FOR_PAYMENT = "WAITING_FOR_PAYMENT".intern();
		}
		public static class PaymentTransactionType
		{
			public static final String OPTIONS = "OPTIONS".intern();
			public static final String ENROLLMENT = "ENROLLMENT".intern();
			public static final String VALIDATE = "VALIDATE".intern();
			public static final String ORDER_SETUP = "ORDER_SETUP".intern();
			public static final String SET = "SET".intern();
			public static final String GET = "GET".intern();
			public static final String INITIATE = "INITIATE".intern();
			public static final String SALE = "SALE".intern();
			public static final String VOID = "VOID".intern();
			public static final String AUTHORIZATION_REVERSAL = "AUTHORIZATION_REVERSAL".intern();
			public static final String ORDER_SETUP_REVERSAL = "ORDER_SETUP_REVERSAL".intern();
			public static final String CHECK_STATUS = "CHECK_STATUS".intern();
			public static final String TAX = "TAX".intern();
			public static final String REFUND = "REFUND".intern();
			public static final String ACCOUNT_TAKEOVER_PROTECTION = "ACCOUNT_TAKEOVER_PROTECTION".intern();
			public static final String ADVANCED_FRAUD_SCREEN = "ADVANCED_FRAUD_SCREEN".intern();
			public static final String CREATE_SESSION = "CREATE_SESSION".intern();
			public static final String UPDATE_SESSION = "UPDATE_SESSION".intern();
			public static final String DELIVERY_ADDRESS_VERIFICATION = "DELIVERY_ADDRESS_VERIFICATION".intern();
			public static final String EXPORT_COMPLIANCE = "EXPORT_COMPLIANCE".intern();
			public static final String BILLING_AGREEMENT = "BILLING_AGREEMENT".intern();
			public static final String CREATE_BILLING_AGREEMENT_SESSION = "CREATE_BILLING_AGREEMENT_SESSION".intern();
			public static final String CANCEL_ORDER = "CANCEL_ORDER".intern();
		}
		public static class PaymentType
		{
			public static final String CREDIT_CARD = "CREDIT_CARD".intern();
			public static final String PAY_PAL = "PAY_PAL".intern();
			public static final String ALTERNATIVE_PAYMENT = "ALTERNATIVE_PAYMENT".intern();
			public static final String TAX_CALCULATION = "TAX_CALCULATION".intern();
			public static final String FRAUD = "FRAUD".intern();
			public static final String VISA_CHECKOUT = "VISA_CHECKOUT".intern();
			public static final String VERIFICATION = "VERIFICATION".intern();
			public static final String GIFT = "GIFT".intern();
			public static final String APPLE_PAY = "APPLE_PAY".intern();
			public static final String GOOGLE_PAY = "GOOGLE_PAY".intern();
		}
	}
	public static class Relations
	{
		public static final String MERCHANTTOPROFILE = "MerchantToProfile".intern();
	}
	
	protected GeneratedIsvPaymentConstants()
	{
		// private constructor
	}
	
	
}
