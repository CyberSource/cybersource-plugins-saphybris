<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/responsive/template" %>

<html>
    <head>
        <template:javaScriptVariables/>
        <script>
            window.parent.CARDINAL_COMMERCE.validation(window.parent.CARDINAL_COMMERCE.transientToken,"${transactionId}");
        </script>
    </head>
</html>
