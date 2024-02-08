<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:choose>
    <c:when test="${is3dsEnabled && paymentPciType == 'FLEX'}">
        <input type="hidden" id="JWTContainer" value="${jwt}" />

        <script src="${songbirdUrl}"></script>

        <script type="application/javascript">

            Cardinal.defaultErrorPage = '/checkout/multi/summary/view/payment/error';

            Cardinal.on('payments.setupComplete', function(data) {
                CARDINAL_COMMERCE.initialized = true;

                //Store sessionId in preparation for check enroll request
                CARDINAL_COMMERCE.sessionID = data.sessionId;

                if (CARDINAL_COMMERCE.binNumber) {
                    CARDINAL_COMMERCE.updateBIN(CARDINAL_COMMERCE.binNumber);
                }

                if (CARDINAL_COMMERCE.flexToken) {
                    CARDINAL_COMMERCE.pay(CARDINAL_COMMERCE.flexToken)
                }
            });

            Cardinal.on("payments.validated", function (data, jwt) {
                if (data.ErrorNumber === 0) {
                    CARDINAL_COMMERCE.showSpinner();
                    //Validate payment and place order
                    $.ajax({
                        url: ACC.config.contextPath + '/checkout/payment/flex/payWithValidation',
                        cache: false,
                        dataType: 'json',
                        method: 'POST',
                        data: {
                            transientToken: $('#card_flexToken').val(),
                            authJwt: jwt
                        },
                        success: function (response) {
                            CARDINAL_COMMERCE.hideSpinner();
                            window.location.replace(ACC.config.contextPath + response.data.redirectUrl);
                        },
                        error: function (response) {
                            CARDINAL_COMMERCE.hideSpinner();
                            console.error("There was an error while placing the order", arguments)

                            var redirectUrl = Cardinal.defaultErrorPage
                            if (response.data && response.data.redirectUrl) {
                                redirectUrl = response.data.redirectUrl
                            }

                            window.location.replace(ACC.config.contextPath + redirectUrl);
                        }
                    });
                }
                else {
                    console.error("Error validating payment. Error code [" + data.ErrorNumber + "] Error description [" + data.ErrorDescription + "]");
                    window.location.replace(ACC.config.contextPath + Cardinal.defaultErrorPage);
                }
            });

            var CARDINAL_COMMERCE = {
                is3dsEnabled: true,
                initialized: false,
                sessionID: null,
                flexToken: null,
                binNumber: null,

                initialize: function() {
                    Cardinal.setup("init", {
                        jwt: document.getElementById("JWTContainer").value
                    });
                },

                pay: function (transientToken) {
                    CARDINAL_COMMERCE.showSpinner();
                    $.ajax({
                        url: ACC.config.contextPath + '/checkout/payment/flex/attemptPaymentWithoutValidation',
                        cache: false,
                        method: 'POST',
                        data: {
                            transientToken: transientToken,
                            referenceId: CARDINAL_COMMERCE.sessionID
                        },
                        success: function (response) {
                            CARDINAL_COMMERCE.hideSpinner();
                            if (response.data.responseCode === '475') {
                                //Card is enrolled in a payer authentication program. Proceed to validation
                                Cardinal.continue('cca',
                                    {
                                        "AcsUrl": response.data.acsUrl,
                                        "Payload": response.data.payload
                                    },
                                    {
                                        "OrderDetails": {
                                            "TransactionId": response.data.transactionId
                                        }
                                    }
                                );
                            } else {
                                //Card not enrolled. Payment was authorized and we can redirect to order confirmation
                                window.location.replace(ACC.config.contextPath + response.data.redirectUrl);
                            }
                        },
                        error: function (response) {
                            CARDINAL_COMMERCE.hideSpinner();
                            console.error("Error enrolling card", arguments)

                            var redirectUrl = Cardinal.defaultErrorPage;
                            if (response.data && response.data.redirectUrl) {
                                redirectUrl = response.data.redirectUrl
                            }

                            window.location.replace(ACC.config.contextPath + redirectUrl);
                        }
                    });
                },

                updateBIN: function (newBin) {
                    Cardinal.trigger("bin.process", newBin);
                },

                showSpinner: function() {
                    $('.spinner-wrapper').show();
                    $('#placeOrder').prop('disabled', true)
                },

                hideSpinner: function() {
                    $('.spinner-wrapper').hide();
                    $('#placeOrder').prop('disabled', false)
                }
            }

        </script>
    </c:when>
    <c:otherwise>
        <script type="application/javascript">
            var CARDINAL_COMMERCE = {
                is3dsEnabled: false
            }
        </script>
    </c:otherwise>
</c:choose>