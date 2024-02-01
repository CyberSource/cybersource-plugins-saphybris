<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/responsive/template"%>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags"%>
<%@ taglib prefix="multi-checkout" tagdir="/WEB-INF/tags/responsive/checkout/multi"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<spring:url value="/checkout/multi/summary/placeOrder" var="placeOrderUrl"/>
<spring:url value="/checkout/multi/termsAndConditions" var="getTermsAndConditionsUrl"/>

<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="isv" uri="/WEB-INF/tld/addons/isvpaymentaddon/isv.tld"%>

<%@ taglib prefix="fraud" tagdir="/WEB-INF/tags/addons/isvpaymentaddon/responsive/fraud" %>
<%@ taglib prefix="visacheckout" tagdir="/WEB-INF/tags/addons/isvpaymentaddon/responsive/payment/visacheckout" %>
<%@ taglib prefix="klarna" tagdir="/WEB-INF/tags/addons/isvpaymentaddon/responsive/payment/klarna" %>
<%@ taglib prefix="wechat" tagdir="/WEB-INF/tags/addons/isvpaymentaddon/responsive/payment/wechat" %>
<%@ taglib prefix="multi-checkout-isv" tagdir="/WEB-INF/tags/addons/isvpaymentaddon/responsive/checkout/multi"%>
<%@ taglib prefix="shared" tagdir="/WEB-INF/tags/addons/isvpaymentaddon/responsive/shared" %>
<%@ taglib prefix="isv3ds" tagdir="/WEB-INF/tags/addons/isvpaymentaddon/responsive/payment/3ds" %>

<template:page pageTitle="${pageTitle}" hideHeaderLinks="true">

<c:if test="${visaCheckoutEnabled}">
    <visacheckout:vcInit
            apiKey="${visaCheckoutAPIKey}"
            currency="${cartData.totalPriceWithTax.currencyIso}"
            total="${cartData.totalPriceWithTax.value}"/>
</c:if>

<div class="row">
    <div class="col-sm-6">
    	<div class="checkout-headline">
    		<span class="glyphicon glyphicon-lock"></span>
            <spring:theme code="checkout.multi.secure.checkout" />
        </div>
		<multi-checkout:checkoutSteps checkoutSteps="${checkoutSteps}" progressBarId="${progressBarId}">
			<ycommerce:testId code="checkoutStepFour">

                <jsp:include page="payment/checkoutPaymentModes.jsp"/>

                <c:if test="${klarnaEnabled}">
                    <klarna:klarnaInit klarnaSDKURL="${klarnaSDKUrl}"/>
                </c:if>

                <isv:pciStrategyType type="FLEX">
                    <jsp:include page="payment/flexCardPaymentDetails.jsp"/>
                </isv:pciStrategyType>

                <isv:pciStrategyType type="HOP">
                    <c:url var="hopFormAction" value="/checkout/payment/sa/hop"/>
                    <form:form id="hopRequestForm" name="hopRequestForm" action="${hopFormAction}"/>
                </isv:pciStrategyType>

                <isv:pciStrategyType type="SOP">
                    <jsp:include page="payment/checkoutCardPaymentDetails.jsp"/>
                    <c:url var="sopFormAction" value="/checkout/payment/sa/sop"/>
                    <div id="sopIframeCbox">
                        <iframe id="sopRequestIframe" src="${sopFormAction}"></iframe>
                    </div>
                </isv:pciStrategyType>

                <jsp:include page="payment/vcCardPaymentDetails.jsp"/>

                <div class="checkout-review hidden-xs">
                    <div class="checkout-order-summary">
                        <multi-checkout:orderTotals cartData="${cartData}" showTaxEstimate="${showTaxEstimate}" showTax="${showTax}" subtotalsCssClasses="dark"/>
                    </div>
                </div>

                <div class="display-none">
                        <div class="checkout-weChatPaymentDetails m-2">
                            <div class="row m-2">
                                <div class="col-xs-12  w-100 border-0">
                                    <iframe id="weChatPaymentQRIframe" src="" style="height: 304px;"></iframe>
                                </div>
                            </div>
                            <div class="row m-2">
                                <label class="weChatModalInstructions">
                                    <spring:theme code="checkout.summary.paymentMethod.weChat.modal.instructions"/>
                                </label>
                            </div>
                            <div class="row m-2">
                                <button type="submit" class="btn btn-primary btn-block btn-wechat-complete-payment">
                                    <spring:theme code="checkout.summary.paymentMethod.weChat.modal.submit" />
                                </button>
                            </div>
                            <div class="row mb-2">
                                <div class="confirm-wechat-payment-spinner" hidden="hidden">
                                    <img src="${contextPath}/_ui/responsive/common/images/spinner.gif"/>
                                </div>
                            </div>
                            <div class="display-none">
                                <label id="weChatModalTitle">
                                    <spring:theme code="checkout.summary.paymentMethod.weChat.modal.title"/>
                                </label>
                            </div>
                        </div>
                </div>

                <div class="place-order-form hidden-xs">
                    <form:form action="${placeOrderUrl}" id="placeOrderForm1" modelAttribute="placeOrderForm">
                        <div class="checkbox">
                            <label> <form:checkbox id="Terms1" path="termsCheck" />
                                <spring:theme code="checkout.summary.placeOrder.readTermsAndConditions" arguments="${getTermsAndConditionsUrl}" text="Terms and Conditions"/>
                            </label>
                        </div>

                        <div class="alert alert-danger tc-unchecked-alert" hidden="hidden">
                            <spring:theme code="checkout.terms.and.conditions.unchecked.error"/>
                        </div>

                        <div class="place-order-3ds-notification">
                            <spring:theme code="checkout.summary.placeOrder.3ds.popup.notification"/>
                        </div>

                        <div class="placeOrderBtnDiv">
                            <div class="spinner-wrapper">
                                <div class="spinner"></div>
                            </div>
                            <button id="placeOrder" type="button" class="btn btn-primary cs_btn-place-order btn-block">
                                <spring:theme code="checkout.summary.placeOrder" text="Place Order"/>
                            </button>
                        </div>

                        <div class="applePayBtnDiv" hidden="hidden">
                            <a class="applePayBtn" role="link"></a>
                        </div>

                        <div class="googlePayBtnDiv" hidden="hidden"></div>
                    </form:form>

                    <c:if test="${visaCheckoutEnabled}">
                        <visacheckout:vcButton imageUrl="${visaCheckoutImageUrl}" locale="${locale}"/>
                    </c:if>
                </div>

            </ycommerce:testId>
		</multi-checkout:checkoutSteps>
    </div>

    <div class="col-sm-6">
		<multi-checkout-isv:checkoutOrderSummary cartData="${cartData}" showDeliveryAddress="true" showPaymentInfo="true" showTaxEstimate="true" showTax="true" />
	</div>

    <c:if test="${visaCheckoutEnabled}">
        <visacheckout:vcLoadSDK sdkUrl="${visaCheckoutSDKUrl}"/>
    </c:if>

    <div class="col-sm-12 col-lg-12">
        <br class="hidden-lg">
        <cms:pageSlot position="SideContent" var="feature" element="div" class="checkout-help">
            <cms:component component="${feature}"/>
        </cms:pageSlot>
    </div>
</div>

<fraud:deviceFingerPrint deviceFingerPrint="${deviceFingerPrint}"/>

</template:page>

<%--Tags below will render with 'ACC.config' JS namespace available--%>

<shared:jsInit/>

<isv3ds:cardinalCommerce/>
