INSERT_UPDATE IsvMerchantPaymentConfiguration;merchant(id)[unique=true];currency(isoCode)[unique=true];paymentType(code)[default=GOOGLE_PAY][unique=true];paymentChannel(code)[default=WEB][unique=true];site(uid)[unique=true]
;<merchantID>;EUR;;;apparel-uk
;<merchantID>;GBP;;;apparel-uk
;<merchantID>;USD;;;apparel-uk

;<merchantID>;EUR;;;apparel-de
;<merchantID>;GBP;;;apparel-de
;<merchantID>;USD;;;apparel-de
