INSERT_UPDATE IsvMerchantPaymentConfiguration;merchant(id)[unique=true];currency(isoCode)[unique=true];paymentType(code)[default=CREDIT_CARD][unique=true];paymentChannel(code)[default=WEB][unique=true];site(uid)[unique=true];authServiceCommerceIndicator()
;<merchantID>;EUR;;;apparel-uk;
;<merchantID>;GBP;;;apparel-uk;
;<merchantID>;USD;;;apparel-uk;

;<merchantID>;EUR;;;apparel-de;
;<merchantID>;GBP;;;apparel-de;
;<merchantID>;USD;;;apparel-de;

;<merchantID>;USD;;;powertools;recurring

INSERT_UPDATE IsvMerchantProfile;id[unique=true];merchant(id);profileType(code);profileId;accessKey;secretKey
;<merchantID>_SOP;<merchantID>;SOP;<profileId>;<accessKey>;<secretKey>
;<merchantID>_HOP;<merchantID>;HOP;<profileId>;<accessKey>;<secretKey>
