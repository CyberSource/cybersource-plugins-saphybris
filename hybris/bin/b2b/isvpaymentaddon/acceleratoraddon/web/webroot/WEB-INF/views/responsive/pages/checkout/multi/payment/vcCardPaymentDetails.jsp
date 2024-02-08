<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="formElement" tagdir="/WEB-INF/tags/responsive/formElement" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<div class="checkout-paymentmethod vc_card_details" hidden="hidden">
    <br>
    <div class="row">
        <div class="col-xs-6">
            <img class="card-image" src='<c:out value="${visaCheckoutPaymentDetails.cardArt}"/>' />
        </div>
        <div class="col-xs-6">
            <div><c:out value="${visaCheckoutPaymentDetails.billToName}"/></div>
            <div><c:out value="${visaCheckoutPaymentDetails.cardExpirationMonth}"/>/<c:out value="${visaCheckoutPaymentDetails.cardExpirationYear}"/></div>
            <div><c:out value="${visaCheckoutPaymentDetails.cardType}"/>&nbsp;x<c:out value="${visaCheckoutPaymentDetails.cardSuffix}"/></div>
        </div>
    </div>
</div>
