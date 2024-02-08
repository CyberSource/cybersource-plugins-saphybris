<%@ attribute name="apiKey" required="true" type="java.lang.String"%>
<%@ attribute name="currency" required="true" type="java.lang.String"%>
<%@ attribute name="total" required="true" type="java.lang.String"%>
<%@ attribute name="skipAcceptance" required="false" type="java.lang.Boolean"%>

<script type="application/javascript">

  function onVisaCheckoutReady() {
    V.init({
      apikey: "${apiKey}",
      paymentRequest: {
        currencyCode: "${currency}",
        subtotal: "${total}",
        total: "${total}"
      },
      settings: {
        review: {
          buttonAction: (ACC.cartPage ? "Continue" : "Pay")
        },
        displayName: "${siteName}"
      }
    });
  }

  function vcButtonHandler(event) {
    ACC.secureacceptance.vcTermsAndConditions(event, ${skipAcceptance});
  }

</script>