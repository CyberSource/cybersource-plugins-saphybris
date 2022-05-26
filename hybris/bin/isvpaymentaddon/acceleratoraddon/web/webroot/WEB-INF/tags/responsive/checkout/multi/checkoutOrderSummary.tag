<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="cartData" required="true" type="de.hybris.platform.commercefacades.order.data.CartData" %>
<%@ attribute name="showDeliveryAddress" required="true" type="java.lang.Boolean" %>
<%@ attribute name="showPaymentInfo" required="false" type="java.lang.Boolean" %>
<%@ attribute name="showTax" required="false" type="java.lang.Boolean" %>
<%@ attribute name="showTaxEstimate" required="false" type="java.lang.Boolean" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="multi-checkout" tagdir="/WEB-INF/tags/responsive/checkout/multi" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="order" tagdir="/WEB-INF/tags/responsive/order" %>

<%@ taglib prefix="visacheckout" tagdir="/WEB-INF/tags/addons/isvpaymentaddon/responsive/payment/visacheckout" %>
<%@ taglib prefix="googlePay" tagdir="/WEB-INF/tags/addons/isvpaymentaddon/responsive/payment/googlepay" %>

<spring:url value="/checkout/multi/summary/placeOrder" var="placeOrderUrl"/>
<spring:url value="/checkout/multi/termsAndConditions" var="getTermsAndConditionsUrl"/>

<div class="checkout-summary-headline hidden-xs">
    <spring:theme code="checkout.multi.order.summary" />
</div>
<div class="checkout-order-summary">
    <ycommerce:testId code="orderSummary">
        <multi-checkout:deliveryCartItems cartData="${cartData}" showDeliveryAddress="${showDeliveryAddress}" />

        <c:forEach items="${cartData.pickupOrderGroups}" var="groupData" varStatus="status">
            <multi-checkout:pickupCartItems cartData="${cartData}" groupData="${groupData}" showHead="true" />
        </c:forEach>

        <order:appliedVouchers order="${cartData}" />

        <multi-checkout:paymentInfo cartData="${cartData}" paymentInfo="${cartData.paymentInfo}" showPaymentInfo="${showPaymentInfo}" />


        <multi-checkout:orderTotals cartData="${cartData}" showTaxEstimate="${showTaxEstimate}" showTax="${showTax}" />
    </ycommerce:testId>
</div>

<div class="visible-xs clearfix">
    <form:form action="${placeOrderUrl}" id="placeOrderForm1" modelAttribute="placeOrderForm" class="place-order-form col-xs-12">
        <div class="checkbox">
            <label> <form:checkbox id="Terms1" path="termsCheck" />
                <spring:theme code="checkout.summary.placeOrder.readTermsAndConditions" arguments="${getTermsAndConditionsUrl}" />
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

            <button id="placeOrder" type="submit" class="btn btn-primary btn-place-order btn-block">
                <spring:theme code="checkout.summary.placeOrder"/>
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

    <googlePay:googlePay/>
</div>
