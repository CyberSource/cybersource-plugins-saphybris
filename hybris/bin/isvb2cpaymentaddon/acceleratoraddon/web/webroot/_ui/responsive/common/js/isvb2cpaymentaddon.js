ACC.cartPage = (document.location.pathname.indexOf('cart') > 0);

ACC.secureacceptance = {

    _autoload: ['attachHandlerForPlaceOrderBtn', 'initSOPIframeDialog', 'initDefaultPaymentMode', 'attachHandlerForPaymentModeRadioBtn', 'attachVCHandlers', 'initBasket', 'initApplePay'],

    handleCheckAltPaymentStatusResponse: function(checkStatusResponse, paymentType){
        switch (checkStatusResponse) {
            case "PAYMENT_SUCCESS":
                $(".checkout-paymentmethod").after('<form name="placeOrderAltPayment" action="' + ACC.config.contextPath
                        + '/checkout/payment/ap/return">' +
                        '<input type="hidden" name="type" value="'+paymentType+'">'+
                        '<input type="hidden" name="CSRFToken" value="' + ACC.config.CSRFToken + '" /> ' +
                        '</form>');

                $('[name ="placeOrderAltPayment"]').submit();
                break;
            case "PAYMENT_PENDING":
                break;
            case "CHECK_STATUS_TOO_MANY_ATTEMPTS":
            case "CHECK_STATUS_ERROR":
                ACC.secureacceptance.handleStatusCheckError();
                break;
        }
    },

    checkOrderStatusInterval: function (cartGuid, timeout) {
        var checkStatusUrl =  ACC.config.contextPath + '/checkout/payment/sa/isorderplaced';
        var successCallback =  ACC.secureacceptance.handleStatusCheckResponse;

        setInterval(function () {
            ACC.secureacceptance.checkOrderStatus(
                    cartGuid,
                    checkStatusUrl,
                    successCallback
            )
        }, timeout);
    },

    checkAlternativePaymentOrderStatusInterval: function (cartGuid, timeout, paymentType) {
        var checkStatusUrl = ACC.config.contextPath + '/checkout/payment/ap/checkstatus';
        var successCallback =   ACC.secureacceptance.handleCheckAltPaymentStatusResponse;

        setInterval(function () {
            ACC.secureacceptance.checkOrderStatus(
                    cartGuid,
                    checkStatusUrl,
                    successCallback,
                    paymentType
            )
        }, timeout);
    },

    checkOrderStatus: function (cartGuid, url, successCallback, paymentType) {
        $.ajax({
            url: url,
            cache: false,
            dataType: 'json',
            data: {
                cartGuid: cartGuid,
                paymentType: paymentType
            },
            success: function (response) {
                successCallback(response, paymentType);
            },
            error: function () {
                ACC.secureacceptance.handleStatusCheckError();
            }
        });
    },

    handleStatusCheckResponse: function(checkStatusResponse){
        if (checkStatusResponse) {
            $(".checkout-paymentmethod").after('<form id="placeOrder" action="' + ACC.config.contextPath
                    + '/checkout/orderConfirmation/'+orderCode +'" method="get"></form>');
            $("#placeOrder").submit();
        }
    },

    handleStatusCheckError: function(){
        $(".checkout-paymentmethod").after('<form id="paymentError" action="' + ACC.config.contextPath
                + '/checkout/multi/summary/view/payment/error" method="get"></form>');
        $("#paymentError").submit();
    },

    termsAndConditionsChecked: function(skipAcceptance) {
        if (typeof skipAcceptance !== 'undefined' && skipAcceptance === true) {
            return true;
        }
        if ($("input[name='termsCheck']").is(":checked")) {
            $(".tc-unchecked-alert").attr("hidden", "hidden");
            return true;
        } else {
            $(".tc-unchecked-alert").removeAttr("hidden");
            return false;
        }
    },

    initSOPIframeDialog: function () {
        var $sopIframeCbox = $('#sopIframeCbox');

        if ($sopIframeCbox) {
            $sopIframeCbox.dialog({
                title: "Payment Details",
                autoOpen: false,
                width: 390,
                dialogClass: 'no-close',
                modal: true
            });
        }
    },

    startSOPIframeTimeout: function ($sopRequestIframe) {
        var previousSOPIframeTimeout = null;

        $sopRequestIframe.on('load', function () {
            if (previousSOPIframeTimeout) {
                clearTimeout(previousSOPIframeTimeout);
            }

            previousSOPIframeTimeout = setTimeout(function () {
                $('#sopIframeCbox').dialog('open');
            }, 3000);
        });
    },

    initDefaultPaymentMode: function () {
        if (ACC.expressVisaCheckout == 'true') {
          var visaCheckoutMode = $("#paymentMode_7_visacheckout");
          if (visaCheckoutMode.length) {
            visaCheckoutMode.prop("checked", true);
            ACC.secureacceptance.selectPaymentMethod(visaCheckoutMode.attr("data-payment-type"));
            return;
          }
        }
        // By default, select Credit Card payment mode
        var creditCardMode = $("#paymentMode_1_creditcard");
        if (creditCardMode.length) {
            creditCardMode.prop("checked", true);
            ACC.secureacceptance.selectPaymentMethod(creditCardMode.attr("data-payment-type"));
        }
    },

    initBasket: function() {
        if (ACC.cartPage) {
          ACC.secureacceptance.enableCustomPaymentBtn(".visaCheckoutBtnDiv");
        }
    },

    attachHandlerForPaymentModeRadioBtn: function () {

        var paymentModeBtn = $('input[name=paymentMode]');

        if (paymentModeBtn) {
            paymentModeBtn.on('change', function (e) {
                var selectedPaymentType = ACC.secureacceptance.getPaymentType();

                ACC.secureacceptance.selectPaymentMethod(selectedPaymentType);
            });
        }
    },

    initCreditCardPaymentMode: function() {
        if (typeof MICROFORM !== 'undefined') {
            MICROFORM.setup();
        }
    },

    initKlarnaWidget: function() {
        if (typeof KLARNA !== 'undefined' && !KLARNA.loaded) {
            // Do not try to load it more than once.
            KLARNA.loaded = true;
            $.ajax({
                url: ACC.config.contextPath + '/checkout/payment/klarna/createSession',
                cache: false,
                dataType: 'json',
                data: {
                    CSRFToken: ACC.config.CSRFToken
                },
                method: 'POST',
                success: function (resp) {
                    if (resp.success) {
                        KLARNA.sessionId = resp.data.klarnaSessionId;
                        if (KLARNA.sessionId) {
                            // Only if session was created - initialize Klarna widget.
                            $.getScript(KLARNA.sdkURL, function (data, textStatus, jqxhr) {
                                console.log("Klarna SDK was loaded");

                                $("#klarna_load_progress").remove();
                            });
                        }
                    }
                }
            });
        }
    },

    selectPaymentMethod: function(selectedPaymentType) {
        var creditCardDetails = $(".creditCardDetails");
        var vcCardDetails = $(".vc_card_details");
        var klarnaContainer = $("#klarna_container");

        ACC.secureacceptance.disableCustomPaymentBtn(".visaCheckoutBtnDiv");
        ACC.secureacceptance.disableCustomPaymentBtn(".applePayBtnDiv");
        ACC.secureacceptance.disableCustomPaymentBtn(".googlePayBtnDiv");
        klarnaContainer.hide();
        creditCardDetails.hide();
        vcCardDetails.hide();

        switch (selectedPaymentType) {
            case "CREDIT_CARD":
                creditCardDetails.show();
                ACC.secureacceptance.initCreditCardPaymentMode();
                break;
            case "VISA_CHECKOUT":
                if (!ACC.expressVisaCheckout) {
                    ACC.secureacceptance.enableCustomPaymentBtn(".visaCheckoutBtnDiv");
                } else {
                    vcCardDetails.show();
                }
                break;
            case "KLI":
                klarnaContainer.show();
                ACC.secureacceptance.initKlarnaWidget();
                break;
            case "APP":
                ACC.secureacceptance.enableCustomPaymentBtn(".applePayBtnDiv");
                break;
            case "GGP":
                ACC.secureacceptance.enableCustomPaymentBtn(".googlePayBtnDiv");
                break;
        }
    },

    enableCustomPaymentBtn: function(btnSelector) {
        $(".placeOrderBtnDiv").hide();
        $(btnSelector).show();
    },

    disableCustomPaymentBtn: function(btnSelector) {
        $(".placeOrderBtnDiv").show();
        $(btnSelector).hide();
    },

    getPaymentType: function() {
        return $("input[type='radio'][name='paymentMode']:checked").attr("data-payment-type");
    },

    isVisaCheckoutPayment: function() {
        return ACC.secureacceptance.getPaymentType() === "VISA_CHECKOUT";
    },

    isVisaExpressCheckout: function() {
        return ACC.secureacceptance.isVisaCheckoutPayment() && ACC.expressVisaCheckout == 'true';
    },

    createApplePaySession: function(request) {
        var session = new ApplePaySession(3, request);

        session.onvalidatemerchant = function (event) {
            $.ajax({
                method: 'GET',
                url: ACC.config.contextPath + '/checkout/payment/ap/applepay/validate',
                data: {validationUrl: event.validationURL},
                success: function (merchSession) {
                    session.completeMerchantValidation(merchSession);
                }
            });
        }

        session.onpaymentauthorized = function (event) {
            var cartGuid = $('input[name=cartGuid]').val()
            ACC.secureacceptance.checkOrderStatusInterval(cartGuid, 5000)

            $.ajax({
                method: 'POST',
                url: ACC.config.contextPath + '/checkout/payment/ap/applepay/placeOrder',
                data: JSON.stringify(event.payment),
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                success: function (response) {
                    session.completePayment(ApplePaySession.STATUS_SUCCESS);
                    window.location.href = ACC.config.contextPath + response;
                },
                error: function (response) {
                    session.completePayment(ApplePaySession.STATUS_FAILURE);
                    window.location.href = ACC.config.contextPath + response.responseJSON;
                }
            });
        }

        return session;
    },

    attachHandlerForPlaceOrderBtn: function () {

        var isValidFormData = function(fieldsToValidate) {
            var isValid = true;

            fieldsToValidate.forEach(function (field) {
                if (Boolean(field.value)) {
                    hide(field.error)
                } else {
                    show(field.error);
                    isValid = false;
                }
            });

            return isValid;
        };

        var show = function (validationMsg) {
            validationMsg.removeAttr("hidden");
            validationMsg.closest(".form-group").addClass("has-error");
        };

        var hide = function (validationMsg) {
            validationMsg.attr("hidden", "hidden");
            validationMsg.closest(".form-group").removeClass("has-error");
        };

        var getCardType = function () {
            return $("#card_cardType option:selected").val();
        };

        var getCardNumber = function () {
            return $("#card_accountNumber").val();
        };

        var getCardExpireDate = function () {
            return $("#ExpiryMonth option:selected").text() + '-' + $("#ExpiryYear option:selected").text();
        };

        var getCardExpireYear = function () {
            return $("#ExpiryYear option:selected").val();
        };

        var getCardExpireMonth = function () {
            return $("#ExpiryMonth option:selected").val();
        };

        var getCardCVN = function () {
            return $("#card_cvNumber").val();
        };

        var submitHOPForm = function () {
            if (ACC.secureacceptance.termsAndConditionsChecked()) {
                var hopForm = $("#hopRequestForm");
                hopForm.submit();
            }
        };

        var submitSOPForm = function () {
            var formFieldsToValidate = [
                {value: getCardType(), error: $("#card_cardType_errors")},
                {value: getCardNumber(), error: $("#card_accountNumber_errors")},
                {value: getCardExpireYear(), error: $("#card_expirationYear_errors")},
                {value: getCardExpireMonth(), error: $("#card_expirationMonth_errors")}
            ];

            if (isValidFormData(formFieldsToValidate) && ACC.secureacceptance.termsAndConditionsChecked()) {
                var $sopRequestIframe = $('#sopRequestIframe');
                var sopForm = $sopRequestIframe.contents().find('#sopRequestForm');

                sopForm.find("#card_type").val(getCardType());
                sopForm.find("#card_number").val(getCardNumber());
                sopForm.find("#card_expiry_date").val(getCardExpireDate());
                sopForm.find("#card_cvn").val(getCardCVN());


                sopForm.submit();

                ACC.secureacceptance.startSOPIframeTimeout($sopRequestIframe);

                var cartGuid = $sopRequestIframe.contents().find("#cart_guid").val();
                ACC.secureacceptance.checkOrderStatusInterval(cartGuid, 10000);
            }
        };

        var submitFlexForm = function() {

            if (! MICROFORM.error) {
                var formFieldsToValidate = [
                    {value: getCardNumber(), error: $("#card_accountNumber_errors")},
                    {value: getCardExpireYear(), error: $("#card_expirationYear_errors")},
                    {value: getCardExpireMonth(), error: $("#card_expirationMonth_errors")},
                    {value: getCardCVN(), error: $("#card_cvNumber_errors")}
                ];

                if (ACC.flexCardTypeSelection == 'true') {
                    formFieldsToValidate.push({value: getCardType(), error: $("#card_cardType_errors")});
                }

                if (isValidFormData(formFieldsToValidate) && ACC.secureacceptance.termsAndConditionsChecked()) {
                    if (CARDINAL_COMMERCE.is3dsEnabled) {
                        CARDINAL_COMMERCE.initialize();
                    }
                    MICROFORM.tokenize();
                }
            }
        };

        var placeOrderWithCard = function () {
            if ($('#hopRequestForm').length) {
                submitHOPForm();
            }
            else if ($('#silentOrderPostForm').length) {
                submitSOPForm();
            }
            else if ($('#flexMicroformPaymentForm').length) {
                submitFlexForm();
            }
            else {
                console.error('Unable to resolve form for credit card payment');
            }
        };

        var placeOrderWithPayPal = function () {
            if (ACC.secureacceptance.termsAndConditionsChecked()) {
                $(".checkout-paymentmethod").after('<form id="paypalPlaceOrder" action="' + ACC.config.contextPath
                + '/checkout/payment/paypal/startFlow" method="get"></form>');
                $("#paypalPlaceOrder").submit();
            }
        };

        var placeOrderWithAlternativePayment = function () {
            var alternativePaymentCode = $("input[type='radio'][name='paymentMode']:checked").val();

            if (ACC.secureacceptance.termsAndConditionsChecked()) {
                $(".checkout-paymentmethod").after(
                    '<form id="alternativePlaceOrder" action="' + ACC.config.contextPath + '/checkout/payment/ap/pay" method="post">' +
                    '<input type="hidden" name="paymentModeCode" value="' + alternativePaymentCode + '" /> ' +
                    '<input type="hidden" name="CSRFToken" value="' + ACC.config.CSRFToken + '" /> ' +
                    '</form>');
               $("#alternativePlaceOrder").submit();
            }
        };


        var placeOrderWithWeChatPayment = function () {
            if (ACC.secureacceptance.termsAndConditionsChecked())
            {
                var alternativePaymentCode = $("input[type='radio'][name='paymentMode']:checked").val();

                event.preventDefault();
                var url = ACC.config.contextPath + '/checkout/payment/ap/payNoRedirect';
                var postData = {paymentModeCode: alternativePaymentCode};

                $.post(url, postData, undefined, 'html')
                        .done(function (result, data, status) {
                            result = ACC.sanitizer.sanitize(result);
                            showWeChatPayQRModal(result);

                        })
                        .fail(function (response, status, error) {
                            window.location = ACC.config.contextPath + response.responseText;
                        });
            }
        };

        var showWeChatPayQRModal = function (qrURL)
        {
            var title = $('#weChatModalTitle').text().trim();

            ACC.colorbox.open(title,{
                inline: true,
                height: "600px",
                width: "500px",
                href: ".checkout-weChatPaymentDetails",
                onComplete: function ()
                {
                    $(this).colorbox.resize();
                }
            });

            $(".confirm-wechat-payment-spinner").hide();

            var iframe = $("#weChatPaymentQRIframe");
            iframe.attr('src', qrURL);

            var cartGuid = $('input[name=cartGuid]').val();

            $('.btn-wechat-complete-payment').on('click', function (e) {
                $(".confirm-wechat-payment-spinner").show();
                ACC.secureacceptance.checkAlternativePaymentOrderStatusInterval(cartGuid, ACC.config.weChatCheckStatusInterval, 'WQR');
            });
        };

        var placeOrderWithKlarna = function() {
            if (ACC.secureacceptance.termsAndConditionsChecked()) {
                if (!KLARNA.loadResponse || !KLARNA.loadResponse['show_form'] || KLARNA.loadResponse['errors']) {
                    window.location = ACC.config.contextPath + '/checkout/multi/summary/view/payment/error';
                }

                $.ajax({
                    method: 'PUT',
                    url: ACC.config.contextPath + '/checkout/payment/klarna/updateSession',
                    data: {},
                    contentType: "application/json; charset=utf-8",
                    dataType: "json",
                    error: function (response) {
                        window.location = ACC.config.contextPath + '/checkout/multi/summary/view/payment/error';
                    }
                });

                Klarna.Credit.authorize({}, function(res) {
                    KLARNA.authResponse = res;
                    console.log(JSON.stringify(res));

                  if (KLARNA.authResponse['approved'] === true) {
                    var alternativePaymentCode = $("input[type='radio'][name='paymentMode']:checked").val();
                    $(".checkout-paymentmethod").after(
                      '<form id="alternativePlaceOrder" action="' + ACC.config.contextPath + '/checkout/payment/ap/pay" method="post">' +
                      '<input type="hidden" name="paymentModeCode" value="' + alternativePaymentCode + '" /> ' +
                      '<input type="hidden" name="klarnaAuthToken" value="' + KLARNA.authResponse['authorization_token'] + '" /> ' +
                      '</form>');
                    $("#alternativePlaceOrder").submit();
                  } else if (KLARNA.authResponse['show_form'] === false) {
                    window.location = ACC.config.contextPath + '/checkout/multi/summary/view/payment/error';
                  }
                });
            }
        };

        var placeOrderWithVisaCheckout = function() {
            ACC.secureacceptance.vcSuccess({ callid: ACC.vcCallId });
        };

        var placeOrderWithApplePay = function () {
            if (ACC.secureacceptance.termsAndConditionsChecked()) {
                var countryCode = $('input[name=countryCode]').val();
                var currencyIso = $('input[name=currency]').val();
                var storeName = $('input[name=store]').val();
                var orderTotal = $('input[name=orderTotal]').val();

                var request = {
                    countryCode: countryCode,
                    currencyCode: currencyIso,
                    supportedNetworks: ['visa', 'masterCard', 'amex', 'discover'],
                    merchantCapabilities: ['supports3DS'],
                    total: {label: storeName, amount: orderTotal, type: 'final'}
                }

                var session = ACC.secureacceptance.createApplePaySession(request);

                session.begin();
            }
        };

        $('.cs_btn-place-order, .btn-place-order').on('click', function (e) {
            e.preventDefault();

            var paymentType = ACC.secureacceptance.getPaymentType();

            switch (paymentType) {
                case "CREDIT_CARD":
                    placeOrderWithCard();
                    break;
                case "PAY_PAL":
                    placeOrderWithPayPal();
                    break;
                case "ALTERNATIVE_PAYMENT":
                    placeOrderWithAlternativePayment();
                    break;
                case "WQR":
                    placeOrderWithWeChatPayment();
                    break;
                case "KLI":
                    placeOrderWithKlarna();
                    break;
                case "VISA_CHECKOUT":
                    placeOrderWithVisaCheckout();
                    break;
                default:
                    break;
            }
        });

        $('.applePayBtn').on('click', function (e) {
            placeOrderWithApplePay();
        });
    },

    //Visa Checkout handlers
    vcSuccess: function (payment) {
        var successEndpoint = '/checkout/payment/vc/success' + (ACC.cartPage ? '/express' : '/');
        var $element = (ACC.cartPage ? $(".visaCheckoutBtnDiv") : $(".checkout-paymentmethod"));

        $element.after(
          '<form id="visaCheckoutPlaceOrder" action="' + ACC.config.contextPath + successEndpoint + '" method="post">' +
              '<input type="hidden" name="callId" value="' + payment.callid + '" /> ' +
              '<input type="hidden" name="CSRFToken" value="' + ACC.config.CSRFToken + '" /> ' +
              (ACC.cartPage ? '' : '<input type="hidden" name="expressCheckout" value="' + ACC.secureacceptance.isVisaExpressCheckout() + '" />') +
          '</form>');

        $("#visaCheckoutPlaceOrder").submit();
    },

    vcCancel: function (payment) {
        window.location = ACC.config.contextPath + (ACC.cartPage ? '/cart' : '/checkout/multi/summary/view');
    },

    vcError: function (payment, error) {
        window.location = ACC.config.contextPath + '/checkout/multi/summary/view/payment/error';
    },

    vcTermsAndConditions: function (event, skipAcceptance) {
        var termsAccepted = ACC.secureacceptance.termsAndConditionsChecked(skipAcceptance);
        if (!termsAccepted) {
            jQuery.event.fix(event).stopImmediatePropagation();
        }
    },

    attachVCHandlers: function() {
        if (typeof V !== 'undefined') {
            V.on("payment.success", ACC.secureacceptance.vcSuccess);
            V.on("payment.error", ACC.secureacceptance.vcError);
            V.on("payment.cancel", ACC.secureacceptance.vcCancel);
        }
    },

    initApplePay: function() {
       if (window.ApplePaySession && window.ApplePaySession.canMakePayments()) {
            $('input[data-payment-type=APP]').parent().removeClass("hidden");
       }
    }
};
