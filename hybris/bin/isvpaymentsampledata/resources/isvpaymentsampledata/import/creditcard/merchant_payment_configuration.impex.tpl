INSERT_UPDATE IsvMerchantPaymentConfiguration;merchant(id)[unique=true];currency(isoCode)[unique=true];paymentType(code)[default=CREDIT_CARD][unique=true];paymentChannel(code)[default=WEB][unique=true];site(uid)[unique=true];authServiceCommerceIndicator()
;<mechantID>;EUR;;;apparel-uk;
;<mechantID>;GBP;;;apparel-uk;
;<mechantID>;USD;;;apparel-uk;

;<mechantID>;EUR;;;apparel-de;
;<mechantID>;GBP;;;apparel-de;
;<mechantID>;USD;;;apparel-de;

;<mechantID>;USD;;;powertools;recurring

INSERT_UPDATE IsvMerchantProfile;id[unique=true];merchant(id);profileType(code);profileId;accessKey;secretKey
;<mechantID>_SOP;<mechantID>;SOP;<profileId>;<accessKey>;<secretKey>
;<mechantID>_HOP;<mechantID>;HOP;<profileId>;<accessKey>;<secretKey>
