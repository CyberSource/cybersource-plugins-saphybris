<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/responsive/template" %>
<%@ taglib prefix="isv" tagdir="/WEB-INF/tags/addons/isvpaymentaddon/responsive/payment/sa" %>

<html>
    <head>
        <template:javaScriptVariables/>

        <script type="text/javascript" src="${commonResourcePath}/js/jquery-3.5.1.min.js"></script>
        <script type="text/javascript" src="${contextPath}/_ui/addons/isvpaymentaddon/responsive/common/js/isvpaymentaddon.js"></script>
    </head>
    <body>
        <iframe name="isvIframe" width="100%" height="100%" frameborder="0px"></iframe>
        <isv:form formId="hopRequestForm" postUrl="${postUrl}" formFields="${formFields}" targetUrl="isvIframe" />

        <script type="text/javascript" language="JavaScript">
            $(function() {
                var payPageRequestForm = $('#hopRequestForm');
                payPageRequestForm.submit();

                ACC.secureacceptance.checkOrderStatusInterval('${cartGuid}', 10000);
            });
        </script>
    </body>
</html>
