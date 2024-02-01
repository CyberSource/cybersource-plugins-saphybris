if (typeof MICROFORM !== "undefined") {

    MICROFORM.onWaitBegin = function () {
        $('#flexWait').show();
    };

    MICROFORM.onWaitComplete = function () {
        $('#flexWait').hide();
    };

    MICROFORM.onGetOptions = function () {
        var cardOptions = {
            expirationMonth: $('#ExpiryMonth').val(),
            expirationYear: $('#ExpiryYear').val()
        };

        if (ACC.flexCardTypeSelection == 'true') {
            cardOptions.type = $('#card_cardType').val();
        }
        return cardOptions;
    };

    MICROFORM.onGetStyles = function () {
        return {
            'input': {
                'color': '#c53131;',
                'font-family': '"Open Sans", Helvetica, Arial, sans-serif',
                'font-size': '15px',
                'font-weight': '400'
            },

            'valid': {'color': '#19212b'},
            'invalid': {'color': '#a94442'}
        }
    };

    MICROFORM.onTokenCreated = function (transientToken) {
        $('#card_flexToken').val(transientToken);

        if (CARDINAL_COMMERCE.is3dsEnabled) {
            if (CARDINAL_COMMERCE.initialized) {
                CARDINAL_COMMERCE.pay(transientToken)
            } else {
                CARDINAL_COMMERCE.flexToken = transientToken;
            }
        }
    };

    MICROFORM.onError = function (message) {
        $('#flexWait').hide();
        $('#flexError').text(message);
        $('#flexError').removeAttr("hidden");
    };

    MICROFORM.cardChangeEventHandler = handleCardChangeEvent;
    MICROFORM.cvnChangeEventHandler = flexCVNValidationChangeListener;

    function flexCVNValidationChangeListener(data) {
        if (data.valid == true) {
            $('#card_cvNumber').val('valid');
        } else {
            $('#card_cvNumber').val('');
        }
    }

    function handleCardChangeEvent(data) {
        if (data.card) {
            flexCardTypeChangeListener(data);
        }
        flexCardNumberValidationChangeListener(data);
    }

    function flexCardNumberValidationChangeListener(data) {
        if (data.valid == true) {
            $('#card_accountNumber').val('valid');
        } else {
            $('#card_accountNumber').val('');
        }
    }

    function flexCardTypeChangeListener(data) {
        var cardTypeElement = $('#flexCardType-label');

        if (data.card.length === 1) {
            var elementText = 'Card type: ' + data.card[0].brandedName;
            cardTypeElement.text(elementText);
            cardTypeElement.show();
        } else {
            cardTypeElement.hide();
        }
    }

}
