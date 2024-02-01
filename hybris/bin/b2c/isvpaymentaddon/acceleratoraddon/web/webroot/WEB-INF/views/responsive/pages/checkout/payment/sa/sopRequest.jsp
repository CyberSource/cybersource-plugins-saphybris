<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="isv" tagdir="/WEB-INF/tags/addons/isvpaymentaddon/responsive/payment/sa" %>

<html>
    <head></head>
    <body>
        <input type="hidden" id="cart_guid" name="cart_guid" value="${cartGuid}"/>
        <isv:form formId="sopRequestForm" postUrl="${postUrl}" formFields="${formFields}" />
    </body>
</html>
