<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:choose>
    <c:when test="${is3dsEnabled && paymentPciType == 'FLEX'}">

        <script type="application/javascript">


            var CARDINAL_COMMERCE = {
                is3dsEnabled: true,
                initialized: false,
                referenceID:null,
                transientToken:null,
                flexToken: null,
                defaultErrorPage : '/checkout/multi/summary/view/payment/error',
                
                hideStepUpForm: function(){
                    const modalContainerToNone = document.getElementById("modal-container");
                    modalContainerToNone.style.display = "none";
                },
                validation: function(transientToken, transactionId){
                    CARDINAL_COMMERCE.hideStepUpForm();
                    CARDINAL_COMMERCE.showSpinner();
                    //Validate payment and place order
                    if(transientToken && transactionId) {
                        $.ajax({
                            url: ACC.config.contextPath + '/checkout/payment/flex/payWithValidation',
                            cache: false,
                            dataType: 'json',
                            method: 'POST',
                            data: {
                                transientToken: transientToken,
                                transactionId: transactionId
                            },
                            success: function (response) {
                                CARDINAL_COMMERCE.hideSpinner();
                                window.location.replace(ACC.config.contextPath + response.data.redirectUrl);
                            },
                            error: function (response) {
                                CARDINAL_COMMERCE.hideSpinner();
                                console.error("There was an error while placing the order", arguments)
                                var redirectUrl =CARDINAL_COMMERCE.defaultErrorPage;
                                if (response.data && response.data.redirectUrl) {
                                    redirectUrl = response.data.redirectUrl
                                }
                                window.location.replace(ACC.config.contextPath + redirectUrl);
                            }
                        });
                    } else {
                        window.location.replace(ACC.config.contextPath + CARDINAL_COMMERCE.defaultErrorPage);
                    }
                },
                 setUp: function (transientToken) {

                    CARDINAL_COMMERCE.transientToken=transientToken
                    CARDINAL_COMMERCE.showSpinner();
                    $.ajax({
                        url: ACC.config.contextPath + '/checkout/payment/flex/attemptPaymentSetUp',
                        cache: false,
                        method: 'POST',
                        data: {
                            transientToken: transientToken
                        },
                        success: function (response) {
                            if('ACCEPT'== response.data.decision){
                                window.addEventListener("message", (event) => {
                                    if (event.origin === new URL(response.data.deviceDataCollectionURL).origin) {
                                        let data = JSON.parse(event.data);
                                        if (data != undefined && data.Status) {
                                            console.log('Data received successfully');
                                            CARDINAL_COMMERCE.pay(CARDINAL_COMMERCE.transientToken);
                                        }
                                        console.log('Merchant received a message:', data);
                                    }
                                }, false);
                                CARDINAL_COMMERCE.referenceID = response.data.referenceID;
                                var formElement = document.createElement("form");
                                var iframe = document.createElement('iframe');
                                iframe.id = 'ddc_iframe';
                                iframe.name = 'ddc_iframe';
                                iframe.height = '10';
                                iframe.width = '10';
                                iframe.style.display = 'none';
                                document.body.appendChild(iframe);

                                formElement.id = "ddc_form";
                                formElement.method = "post";
                                formElement.target = "ddc_iframe";
                                formElement.action = response.data.deviceDataCollectionURL;

                                var inputElement = document.createElement("input");

                                inputElement.id = "access_token";
                                inputElement.type = "hidden";
                                inputElement.name = "JWT";
                                inputElement.value = response.data.accessToken;
                                formElement.appendChild(inputElement);
                                document.body.appendChild(formElement);
                                var ddcForm = document.querySelector("#ddc_form");
                                if (ddcForm) {
                                    ddcForm.submit();
                                }
                            }else if(response.data.redirectUrl){
                                CARDINAL_COMMERCE.hideSpinner();
                                window.location.replace(ACC.config.contextPath + response.data.redirectUrl.redirectUrl);
                            }else{
                                CARDINAL_COMMERCE.hideSpinner();
                                window.location.replace(ACC.config.contextPath + redirectUrl);
                            }
                        },
                        error: function (response) {
                            CARDINAL_COMMERCE.hideSpinner();
                            window.location.replace(ACC.config.contextPath + redirectUrl);
                        }
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
                            referenceId:  CARDINAL_COMMERCE.referenceID,
                            browserCookieAccepted: navigator.cookieEnabled,
                            browserScreenHeight: screen.height,
                            browserScreenWidth: screen.width,
                            serviceReturnUrl:  ACC.config.contextPath
                        },
                        success: function (response) {
                            if (response.data.responseCode === '475') {
                                const challengeWindowSizeMap = {
                                    '01': { width: 250, height: 400 },
                                    '02': { width: 390, height: 400 },
                                    '03': { width: 500, height: 600 },
                                    '04': { width: 600, height: 400 },
                                    '05': { width: window.innerWidth, height: window.innerHeight},
                                    '06': { width: 400, height: 400 }
                                };
                                pareq = response.data.paReq;
                                var decodedPareqValue = window.atob(pareq);
                                var pareqJson = JSON.parse(decodedPareqValue);
                                var challengeWindowSize = pareqJson.challengeWindowSize;
                                const { width, height } = challengeWindowSizeMap[challengeWindowSize] || challengeWindowSizeMap['06'];

                                var modalContainer = document.createElement('div');
                                modalContainer.id = 'modal-container';
                                modalContainer.style.display = 'none';

                                var modalContent = document.createElement('div');
                                modalContent.id = 'modal-content';

                                var iframe = document.createElement('iframe');
                                iframe.id = 'step-up-iframe-id';
                                iframe.name = 'step-up-iframe';

                                var form = document.createElement('form');
                                form.id = 'step-up-form';
                                form.target = 'step-up-iframe';
                                form.method = 'post';

                                var accessTokenInput = document.createElement('input');
                                accessTokenInput.type = 'hidden';
                                accessTokenInput.id = 'accessToken';
                                accessTokenInput.name = 'JWT';

                                var merchantDataInput = document.createElement('input');
                                merchantDataInput.type = 'hidden';
                                merchantDataInput.id = 'merchantData';
                                merchantDataInput.name = 'MD';

                                form.appendChild(accessTokenInput);
                                form.appendChild(merchantDataInput);
                                modalContent.appendChild(iframe);
                                modalContent.appendChild(form);
                                modalContainer.appendChild(modalContent);
                                document.body.appendChild(modalContainer);

                                $('#step-up-iframe-id').attr({width, height});
                                $("#step-up-form").attr("action", response.data.stepUpUrl);
                                $("#accessToken").val(response.data.accessToken);
                                 var stepupForm = document.querySelector("#step-up-form");
                                if (stepupForm) {
                                    const modalContainerConst = document.getElementById("modal-container");
                                    modalContainerConst.style.display = "block";
                                    CARDINAL_COMMERCE.hideSpinner();
                                    stepupForm.submit();
                                }
                            } else {
                                //Card not enrolled. Payment was authorized and we can redirect to order confirmation
                                window.location.replace(ACC.config.contextPath + response.data.redirectUrl);
                            }
                        },
                        error: function (response) {
                            CARDINAL_COMMERCE.hideSpinner();
                            console.error("Error enrolling card", arguments)

                            var redirectUrl = CARDINAL_COMMERCE.defaultErrorPage;
                            if (response.data && response.data.redirectUrl) {
                                redirectUrl = response.data.redirectUrl
                            }

                            window.location.replace(ACC.config.contextPath + redirectUrl);
                        }
                    });
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