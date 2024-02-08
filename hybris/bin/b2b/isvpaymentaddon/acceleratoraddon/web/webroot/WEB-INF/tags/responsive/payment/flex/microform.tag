<%@ attribute name="flexSdkUrl" required="true" type="java.lang.String" %>

<%@ attribute name="flexNewJwkEndpointUrl" required="true" type="java.lang.String" %>
<%@ attribute name="flexVerifyTokenEndpointUrl" required="true" type="java.lang.String" %>
<%@ attribute name="flexCardNumberContainerSelector" required="true" type="java.lang.String" %>
<%@ attribute name="flexSecurityCodeSelector" required="true" type="java.lang.String" %>

<script src="${flexSdkUrl}"></script>

<script type="application/javascript">
    var MICROFORM = {

        loaded: false,
        error: null,

        onWaitBegin: null,
        onWaitComplete: null,
        onGetOptions: null,
        onGetStyles: null,
        onTokenCreated: null,
        onError: null,
        captureContext: null,
        transientToken: null,
        newJwkEndpointUrl: '${flexNewJwkEndpointUrl}',
        verifyTokenEndpointUrl: '${flexVerifyTokenEndpointUrl}',
        flexCardNumberContainerSelector: '${flexCardNumberContainerSelector}',
        flexSecurityCodeSelector: '${flexSecurityCodeSelector}',
        jwk: null,
        microformInstance: null,
        cardChangeEventHandler: null,
        cvnChangeEventHandler: null,


        setup: function () {
            if (!MICROFORM.loaded && MICROFORM.createNewJwk()) {

                MICROFORM.waitBegin();

                var flex = new Flex(MICROFORM.captureContext);
                var microform = flex.microform({styles: MICROFORM.onGetStyles()});

                var number = microform.createField('number', {placeholder: 'Enter card number'});
                var securityCode = microform.createField('securityCode', {placeholder: '***'});

                securityCode.load(MICROFORM.flexSecurityCodeSelector);
                number.load(MICROFORM.flexCardNumberContainerSelector);

                if (MICROFORM.cardChangeEventHandler) {
                    number.on('change', MICROFORM.cardChangeEventHandler);
                }

                if (MICROFORM.cvnChangeEventHandler) {
                    securityCode.on('change', MICROFORM.cvnChangeEventHandler);
                }

                MICROFORM.microformInstance = microform;


                MICROFORM.loaded = true;
                MICROFORM.waitComplete();
            }
        },

        tokenize: function () {

            MICROFORM.waitBegin();

            MICROFORM.microformInstance.createToken(
                MICROFORM.getTokenizationOptions(),
                function (err, response) {
                    if (err) {
                        MICROFORM.reportError(
                            'Failed to create Flex Microform token: ' + err.details.responseStatus.message,
                            err, MICROFORM.canRetryTokenizationRequest(err));
                    } else {
                        if (MICROFORM.verifyFlexToken(response)) {
                            if (MICROFORM.onTokenCreated) {
                                MICROFORM.onTokenCreated(MICROFORM.transientToken);
                            }

                            if (!CARDINAL_COMMERCE.is3dsEnabled) {
                                $('#flexMicroformPaymentForm').submit();
                            }
                        } else {
                            MICROFORM.reportError('Flex Microform token verification failed');
                        }
                    }

                }
            );

            MICROFORM.waitComplete();
        },

        createNewJwk: function () {
            var created = false;

            MICROFORM.waitBegin();

            $.ajax({
                url: MICROFORM.newJwkEndpointUrl,
                cache: false,
                async: false,
                dataType: 'json',

                success: function (result) {
                    console.debug('Obtained new JWK:', result);
                    MICROFORM.captureContext = result;
                    created = true;
                },
                error: function (jqXHR, textStatus) {
                    MICROFORM.reportError('Failed to create new JWK', textStatus);
                }
            });

            MICROFORM.waitComplete();

            return created;
        },

        verifyFlexToken: function verifyFlexToken(flexToken) {
            var isValid = false;

            MICROFORM.waitBegin();

            $.ajax({
                type: "POST",
                url: MICROFORM.verifyTokenEndpointUrl,
                cache: false,
                async: false,
                data: JSON.stringify(flexToken),
                contentType: "application/json",
                success: function (transientToken) {
                    isValid = true;
                    MICROFORM.transientToken = transientToken;
                },
                error: function (jqXHR, textStatus) {
                    MICROFORM.reportError('An error occured while verifying Flex Microform token', textStatus);
                }
            });

            MICROFORM.waitComplete();

            return isValid;
        },

        canRetryTokenizationRequest: function (err) {
            return err.details.responseStatus.reason == 'VALIDATION_ERROR' || err.details.responseStatus.reason == 'TOKENIZATION_ERROR';
        },

        getStyles: function () {
            if (MICROFORM.onGetStyles) {
                return MICROFORM.onGetStyles();
            } else {
                return {};
            }
        },

        getTokenizationOptions: function () {
            if (MICROFORM.onGetOptions) {
                return MICROFORM.onGetOptions();
            } else {
                return {};
            }
        },

        waitBegin: function () {
            if (MICROFORM.onWaitBegin) {
                MICROFORM.onWaitBegin();
            }
        },

        waitComplete: function () {
            if (MICROFORM.onWaitComplete) {
                MICROFORM.onWaitComplete();
            }
        },

        reportError: function (message, details, canRetry = false) {
            if (canRetry) {
                MICROFORM.error = null;
            } else if (!MICROFORM.error) {
                MICROFORM.error = message;
            }

            if (MICROFORM.onError) {
                MICROFORM.onError(message);
            }
            console.error(message, details);
        }
    };
</script>
