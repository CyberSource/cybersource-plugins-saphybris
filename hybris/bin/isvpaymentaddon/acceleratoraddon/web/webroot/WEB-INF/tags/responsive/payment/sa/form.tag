<%@ tag body-content="empty" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ attribute name="formId" required="true" type="java.lang.String"%>
<%@ attribute name="postUrl" required="true" type="java.lang.String"%>
<%@ attribute name="formFields" required="true" type="java.util.Map"%>
<%@ attribute name="targetUrl" required="false" type="java.lang.String"%>

<form id="${formId}" action="${postUrl}" method="post" target="${targetUrl}">
	<c:forEach items="${formFields}" var="entry" varStatus="status">
		<input type="hidden" id="${entry.key}" name="${entry.key}" value="${entry.value}"/>
	</c:forEach>
</form>
