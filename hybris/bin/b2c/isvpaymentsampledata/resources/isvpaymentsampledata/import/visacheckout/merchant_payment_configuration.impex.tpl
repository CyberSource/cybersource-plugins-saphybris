INSERT_UPDATE IsvMerchantPaymentConfiguration;merchant(id)[unique=true];currency(isoCode)[unique=true];paymentType(code)[default=VISA_CHECKOUT][unique=true];paymentChannel(code)[default=WEB][unique=true];site(uid)[unique=true];authServiceCommerceIndicator()
;<merchantID>;EUR;;;apparel-uk;
;<merchantID>;GBP;;;apparel-uk;

INSERT_UPDATE IsvMerchantProfile;id[unique=true];merchant(id);profileType(code);accessKey
;<profileID>;<merchantID>;VCO;<credentials>
