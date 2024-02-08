<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/responsive/template"%>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags"%>
<%@ taglib prefix="multi-checkout" tagdir="/WEB-INF/tags/responsive/checkout/multi"%>
<%@ taglib prefix="b2b-multi-checkout" tagdir="/WEB-INF/tags/addons/b2bacceleratoraddon/responsive/checkout/multi" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<spring:url value="/checkout/multi/summary/placeOrder" var="placeOrderUrl"/>
<spring:url value="/checkout/multi/termsAndConditions" var="getTermsAndConditionsUrl"/>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>

<%@ taglib prefix="fraud" tagdir="/WEB-INF/tags/addons/isvpaymentaddon/responsive/fraud" %>

<template:page pageTitle="${pageTitle}" hideHeaderLinks="true">


    <div class="row">
        <div class="col-sm-6">
            <div class="checkout-headline">
                <span class="glyphicon glyphicon-lock"></span>
                <spring:theme code="checkout.multi.secure.checkout"></spring:theme>
            </div>

            <multi-checkout:checkoutSteps checkoutSteps="${checkoutSteps}" progressBarId="${progressBarId}">
                <ycommerce:testId code="checkoutStepFour">

                    <c:if test="${cartData.paymentType.code == 'CARD'}">
                        <c:choose>
                            <c:when test="${paymentPciType == 'HOP'}">
                                <c:url var="hopFormAction" value="/checkout/payment/sa/hop"/>
                                <form:form id="hopRequestForm" name="hopRequestForm" action="${hopFormAction}"/>
                            </c:when>
                            <c:otherwise>
                                <jsp:include page="/WEB-INF/views/addons/isvpaymentaddon/responsive/pages/checkout/multi/payment/checkoutCardPaymentDetails.jsp"/>
                                <c:url var="sopFormAction" value="/checkout/payment/sa/sop"/>
                                <div id="sopIframeCbox">
                                    <iframe id="sopRequestIframe" src="${sopFormAction}"></iframe>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </c:if>

                    <div class="checkout-review hidden-xs">
                        <div class="checkout-order-summary">
                            <multi-checkout:orderTotals cartData="${cartData}" showTaxEstimate="${showTaxEstimate}" showTax="${showTax}" subtotalsCssClasses="dark"/>
                        </div>
                    </div>
                    <div class="place-order-form hidden-xs">
                        <form:form action="${placeOrderUrl}" id="placeOrderForm1" modelAttribute="placeOrderForm">
                            <div class="checkbox">
                                <label> <form:checkbox id="Terms1" path="termsCheck" />
                                    <spring:theme code="checkout.summary.placeOrder.readTermsAndConditions" arguments="${getTermsAndConditionsUrl}"/>
                                </label>
                            </div>

                            <button id="placeOrder" type="submit" class="btn btn-primary btn-block btn-place-order btn-block btn-lg checkoutSummaryButton" disabled="disabled">
                                <spring:theme code="checkout.summary.placeOrder"/>
                            </button>

                            <c:if test="${cartData.quoteData eq null}">
                                <button id="scheduleReplenishment" type="button" class="btn btn-default btn-block scheduleReplenishmentButton checkoutSummaryButton" disabled="disabled">
                                    <spring:theme code="checkout.summary.scheduleReplenishment"/>
                                </button>

                                <b2b-multi-checkout:replenishmentScheduleForm/>
                            </c:if>
                        </form:form>
                    </div>

                    <script type="application/javascript">
                        var isvB2BPaymentMethod = '${cartData.paymentType.code}';
                    </script>

                </ycommerce:testId>
            </multi-checkout:checkoutSteps>
        </div>

        <div class="col-sm-6">
            <b2b-multi-checkout:checkoutOrderSummary cartData="${cartData}" showDeliveryAddress="true" showPaymentInfo="true" showTaxEstimate="true" showTax="true" />
        </div>

        <div class="col-sm-12 col-lg-12">
            <br class="hidden-lg">
            <cms:pageSlot position="SideContent" var="feature" element="div" class="checkout-help">
                <cms:component component="${feature}"/>
            </cms:pageSlot>
        </div>
    </div>

    <fraud:deviceFingerPrint deviceFingerPrint="${deviceFingerPrint}"/>

</template:page>
