$cronjobNodeId = 0

INSERT_UPDATE IsvAlternativePaymentOptionsCronJob; code[unique = true]; job(ServicelayerJob.springId)[unique = true, forceWrite = true]; merchant(id)[unique = true]; active[default = true]; sessionLanguage(isocode)[default = en]; nodeID[default = $cronjobNodeId]
; IsvAlternativePaymentOptionsUpdateCronJob ; isv.sap.payment.updateAlternativePaymentOptionsJob ; <merchantID> ; ; ; ;


INSERT_UPDATE IsvAlternativePaymentUpdateOrderStatusJob; code[unique = true]; job(ServicelayerJob.springId)[unique = true, forceWrite = true]; active[default = true]; sessionLanguage(isocode)[default = en]; nodeID[default = $cronjobNodeId]
; IsvAlternativePaymentUpdateOrderStatusJob ; isv.sap.payment.updateAlternativePaymentOrderStatusJob ; ; ; ;

INSERT_UPDATE Trigger; cronJob(code)[unique = true]; second; minute; hour; day; month; year; relative; active; maxAcceptableDelay
; IsvAlternativePaymentOptionsUpdateCronJob ; 0  ; 0  ; 18 ; -1 ; -1 ; -1 ; false ; true ; -1
; IsvAlternativePaymentUpdateOrderStatusJob ; 30 ; -1 ; -1 ; -1 ; -1 ; -1 ; true  ; true ; -1

