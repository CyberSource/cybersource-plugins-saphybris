<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>

<%@ attribute name="deviceFingerPrint" required="true" type="isv.sap.payment.security.DeviceFingerPrintData" %>

<div class="deviceFingerPrint">
    <p style="background:url(https://h.online-metrix.net/fp/clear.png?org_id=${deviceFingerPrint.organizationId}&amp;session_id=${deviceFingerPrint.merchantId}${deviceFingerPrint.sessionId}&amp;m=1)"></p>
    <img src="https://h.online-metrix.net/fp/clear.png?org_id=${deviceFingerPrint.organizationId}&session_id=${deviceFingerPrint.merchantId}${deviceFingerPrint.sessionId}&m-2"
         alt="" width="1" height="1">

    <object type="application/x-shockwave-flash"
            data="https://h.online-metrix.net/fp/fp.swf?org_id=${deviceFingerPrint.organizationId}&session_id=${deviceFingerPrint.merchantId}${deviceFingerPrint.sessionId}"
            width="1" height="1" id="thm_fp">
        <param name="movie"
               value="https://h.online-metrix.net/fp/fp.swf?org_id=${deviceFingerPrint.organizationId}&session_id=${deviceFingerPrint.merchantId}${deviceFingerPrint.sessionId}"/>
        <div></div>
    </object>

    <script type="text/javascript"
            src="https://h.online-metrix.net/fp/tags.js?org_id=${deviceFingerPrint.organizationId}&session_id=${deviceFingerPrint.merchantId}${deviceFingerPrint.sessionId}">
    </script>
    <noscript>
        <iframe style="width: 100px; height: 100px; border: 0; position: absolute; top: -5000px;"
                src="https://h.online-metrix.net/fp/tags?org_id=${deviceFingerPrint.organizationId}&session_id=${deviceFingerPrint.merchantId}${deviceFingerPrint.sessionId}">
        </iframe>
    </noscript>
</div>