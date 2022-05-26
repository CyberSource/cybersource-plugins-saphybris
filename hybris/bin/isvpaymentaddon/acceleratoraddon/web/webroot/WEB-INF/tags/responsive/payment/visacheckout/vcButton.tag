<%@ attribute name="imageUrl" required="true" type="java.lang.String" %>
<%@ attribute name="locale" required="true" type="java.lang.String" %>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<div class="visaCheckoutBtnDiv" hidden="hidden">
    <div class="click-to-pay-btn-wrapper">
        <img alt="Click to pay" class="v-button btn"
             role="button" src="${imageUrl}" onclick="vcButtonHandler(event);"/>
        <a class="v-learn click-to-pay-link" href="#" data-locale="${locale}">
            <spring:theme code="checkout.summary.paymentMethod.clickToPay.learnMore"/>
        </a>
    </div>
</div>
