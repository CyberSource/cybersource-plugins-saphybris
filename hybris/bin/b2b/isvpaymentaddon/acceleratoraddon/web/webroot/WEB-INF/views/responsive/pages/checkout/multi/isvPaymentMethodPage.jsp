<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/responsive/template"%>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags"%>
<%@ taglib prefix="multiCheckout" tagdir="/WEB-INF/tags/responsive/checkout/multi"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="formElement" tagdir="/WEB-INF/tags/responsive/formElement" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="address" tagdir="/WEB-INF/tags/responsive/address" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>


<c:url value="${currentStepUrl}" var="choosePaymentMethodUrl" />
<template:page pageTitle="${pageTitle}" hideHeaderLinks="true">
<div class="row">
    <div class="col-sm-6">
        <div class="checkout-headline">
            <span class="glyphicon glyphicon-lock"></span>
            <spring:theme code="checkout.multi.secure.checkout"/>
        </div>
		<multiCheckout:checkoutSteps checkoutSteps="${checkoutSteps}" progressBarId="${progressBarId}">
			<jsp:body>
                <c:if test="${not empty paymentFormUrl}">
                    <div class="checkout-paymentmethod">
                        <div class="checkout-indent">

							    <ycommerce:testId code="paymentDetailsForm">

                                <input type="hidden" id="shouldUseDeliveryAddress" value="${isvPaymentDetailsForm.useDeliveryAddress}"/>
								<form:form id="silentOrderPostForm" name="silentOrderPostForm" modelAttribute="isvPaymentDetailsForm" action="${paymentFormUrl}" method="POST">
                                    <div class="headline">
                                        <spring:theme code="checkout.multi.paymentMethod.addPaymentDetails.billingAddress"/>
                                    </div>
                                    <c:if test="${cartData.deliveryItemsQuantity > 0}">

                                        <div id="useDeliveryAddressData"
                                            data-titlecode="${deliveryAddress.titleCode}"
                                            data-firstname="${deliveryAddress.firstName}"
                                            data-lastname="${deliveryAddress.lastName}"
                                            data-line1="${deliveryAddress.line1}"
                                            data-line2="${deliveryAddress.line2}"
                                            data-town="${deliveryAddress.town}"
                                            data-postalcode="${deliveryAddress.postalCode}"
                                            data-countryisocode="${deliveryAddress.country.isocode}"
                                            data-regionisocode="${deliveryAddress.region.isocodeShort}"
                                            data-address-id="${deliveryAddress.id}"
                                        ></div>
                                        <formElement:formCheckbox
                                            path="useDeliveryAddress"
                                            idKey="useDeliveryAddress"
                                            labelKey="checkout.multi.sop.useMyDeliveryAddress"
                                            tabindex="11"/>
                                    </c:if>
				  
                                    <address:billAddressFormSelector supportedCountries="${countries}" regions="${regions}" tabindex="12"/>
				
									<p class="help-block"><spring:theme code="checkout.multi.paymentMethod.seeOrderSummaryForMoreInformation"/></p>							
								
									</form:form>
							</ycommerce:testId>
                         </div>
                    </div>

                    <button type="button" class="btn btn-primary btn-block submit_silentOrderPostForm checkout-next"><spring:theme code="checkout.multi.paymentMethod.continue"/></button>
                </c:if>

		   </jsp:body>
		</multiCheckout:checkoutSteps>
	</div>

	<div class="col-sm-6 hidden-xs">
		<multiCheckout:checkoutOrderDetails cartData="${cartData}" showDeliveryAddress="true" showPaymentInfo="false" showTaxEstimate="false" showTax="true" />
    </div>

    <div class="col-sm-12 col-lg-12">
        <cms:pageSlot position="SideContent" var="feature" element="div" class="checkout-help">
            <cms:component component="${feature}"/>
        </cms:pageSlot>
    </div>
</div>

</template:page>
