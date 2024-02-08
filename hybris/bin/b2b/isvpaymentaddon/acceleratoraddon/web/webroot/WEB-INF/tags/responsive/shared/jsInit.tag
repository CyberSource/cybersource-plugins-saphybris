<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>

<%-- JS isv payment addon specific configuration --%>
<script type="text/javascript">
    /*<![CDATA[*/

    ACC.config = ACC.config || {};
    ACC.config.weChatCheckStatusInterval = '${requestScope.weChatCheckStatusInterval}';

    ACC.expressVisaCheckout = '${requestScope.expressVisaCheckout}';
    ACC.vcCallId = '${requestScope.callId}';
    ACC.flexCardTypeSelection = '${requestScope.flexCardTypeSelection}';

    /*]]>*/
</script>
