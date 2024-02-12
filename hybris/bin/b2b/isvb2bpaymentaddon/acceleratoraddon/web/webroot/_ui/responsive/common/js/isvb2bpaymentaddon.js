ACC.secureacceptance = {

    _autoload: [
        'initPaymentMode',
        'initSOPIframeDialog',
        'attachHandlerForPlaceOrderBtn',
        'initReplenishmentBtn'
    ],

    checkOrderStatusInterval: function (cartGuid, timeout) {
        setInterval(function() { ACC.secureacceptance.checkOrderStatus(cartGuid) }, timeout);
    },

    checkOrderStatus: function (cartGuid) {
        $.ajax({
            url: ACC.config.contextPath + '/checkout/payment/sa/isorderplaced',
            cache: false,
            dataType: 'json',
            data: {
                cartGuid: cartGuid
            },
            success: function (orderCode) {

                if (orderCode) {
                    window.location = ACC.config.contextPath + '/checkout/orderConfirmation/' + orderCode;
                }
            }
        });
    },

    initSOPIframeDialog: function() {
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

    startSOPIframeTimeout: function($sopRequestIframe) {
        var previousSOPIframeTimeout = null;

        $sopRequestIframe.on('load', function() {
            if (previousSOPIframeTimeout) {
                clearTimeout(previousSOPIframeTimeout);
            }

            previousSOPIframeTimeout = setTimeout(function () {
                $('#sopIframeCbox').dialog('open');
            }, 3000);
        });
    },

    initPaymentMode: function() {
        var creditCardDetails = $(".creditCardDetails");
        if (creditCardDetails.length) {
            // isvB2BPaymentMethod variable is generated in checkoutSummaryPage.jsp
            if (isvB2BPaymentMethod !== 'CARD') {
                creditCardDetails.hide()
            }
        }
    },

    initReplenishmentBtn: function() {
        if(typeof isvB2BPaymentMethod !== 'undefined')
        {
            var replenishmentBtn = $("#scheduleReplenishment");

            if(replenishmentBtn && isvB2BPaymentMethod)
            {
                isvB2BPaymentMethod == "PAYPAL" ? replenishmentBtn.hide() : replenishmentBtn.show();
            }
        }
    },

    attachHandlerForPlaceOrderBtn: function () {

        var submitHOPForm = function () {
            if(termsAndConditionsChecked()) {
                $("#hopRequestForm").submit();
            }
        };

        var isHOPEnabled = function() {
            return $('#hopRequestForm').length
        };

        var submitSOPForm = function() {
            if (isCreditCardPayment() && isValidCardData() && termsAndConditionsChecked()) {

                var $sopRequestIframe = $('#sopRequestIframe');
                var sopForm = $sopRequestIframe.contents().find('#sopRequestForm');

                sopForm.find("#card_type").val(getCardType());
                sopForm.find("#card_number").val(getCardNumber());
                sopForm.find("#card_expiry_date").val(getCardExpireDate());

                sopForm.submit();

                ACC.secureacceptance.startSOPIframeTimeout($sopRequestIframe);

                var cartGuid = $sopRequestIframe.contents().find("#cart_guid").val();
                ACC.secureacceptance.checkOrderStatusInterval(cartGuid, 10000);
            }
        };

        var placeOrderWithPayPal = function () {
            if (termsAndConditionsChecked()) {
                $(".place-order-form.hidden-xs").after('<form id="paypalPlaceOrder" action="' + ACC.config.contextPath
                + '/checkout/payment/paypal/startFlow" method="get"></form>');
                $("#paypalPlaceOrder").submit();
            }
        };

        var isCreditCardPayment = function() {
            return getPaymentType() === "CARD";
        };

        var placeOrderWithCard = function () {
            isHOPEnabled() ? submitHOPForm() : submitSOPForm();
        };

        var removeReplenishment = function() {
            $.ajax({
                type: "DELETE",
                url: ACC.config.contextPath + "/checkout/replenishment"
            });
        };

        var isValidCardData = function () {

            if(isHOPEnabled())
            {
                return true;
            }
            else
            {
                var fieldsToValidate = [
                    {value: getCardType(), error: $("#card_cardType_errors")},
                    {value: getCardNumber(), error: $("#card_accountNumber_errors")},
                    {value: getCardExpireYear(), error: $("#card_expirationYear_errors")},
                    {value: getCardExpireMonth(), error: $("#card_expirationMonth_errors")}
                ];

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
            }
        };

        var termsAndConditionsChecked = function () {
            if($("input[name='termsCheck']").is(":checked")) {
                $(".tc-unchecked-alert").attr("hidden", "hidden");
                return true;
            } else {
                $(".tc-unchecked-alert").removeAttr("hidden");
                return false;
            }
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

        var getPaymentType = function() {
            // isvB2BPaymentMethod variable is generated in checkoutSummaryPage.jsp
            return isvB2BPaymentMethod;
        };

        var placeOrder = function () {
            var paymentType = getPaymentType();

            switch (paymentType) {
                case "CARD" : placeOrderWithCard();
                    break;
                case "PAYPAL": placeOrderWithPayPal();
                    break;
                case "ACCOUNT" : $("#placeOrderForm1").submit();
                    break;
                default : alert('UNKNOWN is not supported: ' + paymentType);
                    break;
            }
        };

        var closeReplenishmentDialog = function() {
            $(".replenishmentOrderClass").val(false);
            $.colorbox.close();
        };

        var replenishmentInfo = function() {
            return {
                day: $("#nDays").val(),
                week: $("#nWeeks").val(),
                dayOfMonth: $("#nthDayOfMonth").val(),
                daysOfWeek: $("input[name=nDaysOfWeek]:checked").map(function () {return this.value;}).get(),
                startDate: $("#replenishmentStartDate").val(),
                recurrence: $("input[name=replenishmentRecurrence]").val()
            };
        };

        var addReplenishment = function() {
            $.ajax({
                type: "POST",
                url: ACC.config.contextPath + "/checkout/replenishment",
                contentType: "application/json; charset=utf-8",
                data:  JSON.stringify(replenishmentInfo()),
                dataType: "json",
                success: placeOrder()
            });
        };

        var placeReplenishmentOrderBtn = $("#placeReplenishmentOrder");

        if(placeReplenishmentOrderBtn) {

            placeReplenishmentOrderBtn.on('click', function(e) {
                if(isCreditCardPayment() && isValidCardData()) {
                    e.stopPropagation();
                    addReplenishment();
                }
                closeReplenishmentDialog();
            });

        }

        $('.cs_btn-place-order, .btn-place-order').on('click', function(e) {
            e.preventDefault();
            removeReplenishment();
            placeOrder();
        });
    }
};
