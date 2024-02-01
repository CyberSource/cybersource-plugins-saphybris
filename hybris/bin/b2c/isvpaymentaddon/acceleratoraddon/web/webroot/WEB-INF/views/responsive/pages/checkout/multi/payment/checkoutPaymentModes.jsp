<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="formElement" tagdir="/WEB-INF/tags/responsive/formElement" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:if test="${paymentModes.size() > 0}">
    <div class="checkout-paymentmethod">
        <br>
        <div class="form-group">
            <label class="control-label"><spring:theme code="checkout.summary.payment.modes"/></label>
        </div>
        <c:set var="imagePath" value="${contextPath}/_ui/addons/isvpaymentaddon/responsive/common/images" />
        <c:forEach var="paymentMode" items="${paymentModes}">
            <div class="row">
                <div class="col-auto payment-logos ${(paymentMode.getPaymentSubType() != null and fn:containsIgnoreCase({'APP', 'GGP'}, paymentMode.getPaymentSubType())) ? 'hidden' : ''}">
                    <input type="hidden" name="orderTotal" value="${cartData.totalPriceWithTax.value}">
                    <input type="hidden" name="currency" value="${cartData.totalPriceWithTax.currencyIso}">
                    <input type="hidden" name="store" value="${cartData.store}">
                    <input type="hidden" name="countryCode" value="${countryCode}">
                    <input type="hidden" name="cartGuid" value="${cartData.guid}">
                    <input type="radio" name="paymentMode" id="paymentMode_${paymentMode.getCode()}"
                           value="${paymentMode.getCode()}" class="paymentMode"
                           data-payment-type="${(paymentMode.getPaymentSubType() != null and fn:containsIgnoreCase({'KLI', 'APP', 'GGP', 'WQR'}, paymentMode.getPaymentSubType())) ? paymentMode.getPaymentSubType() : paymentMode.getPaymentType()}">
                    <c:choose>
                        <c:when test="${paymentMode.getPaymentType() == \"CREDIT_CARD\"}">
                            <img src="${imagePath}/visa.png" alt="${paymentMode.getName()}" class="ccVisaImage ccLogo">
                            <img src="${imagePath}/mastercard.svg" alt="${paymentMode.getName()}" class="ccMasterImage ccLogo">
                            <img src="${imagePath}/ae.jpg" alt="${paymentMode.getName()}" class="ccAmexImage ccLogo">
                            <img src="${imagePath}/maestro.png" alt="${paymentMode.getName()}" class="ccMaestroImage ccLogo">
                            <img src="${imagePath}/dinersclub.png" alt="${paymentMode.getName()}" class="ccDiscoverImage ccLogo">
                        </c:when>
                        <c:when test="${paymentMode.getPaymentType() == \"VISA_CHECKOUT\"}"><img src="${imagePath}/7_visacheckout.png" alt="${paymentMode.getName()}" class="ccImage"><span class="click-to-pay-label"><spring:theme code="checkout.summary.paymentMethod.clickToPay.title"/></span></c:when>
                        <c:otherwise>
                            <img src="${imagePath}/${paymentMode.getCode()}.png" alt="${paymentMode.getName()}" class="ccImage">
                            <c:if test="${paymentMode.getPaymentType() == \"VISA_CHECKOUT\"}">
                                <span class="click-to-pay-label">
                                    <spring:theme code="checkout.summary.paymentMethod.clickToPay.title"/>
                                </span>
                            </c:if>
                            <c:if test="${paymentMode.getPaymentSubType() == \"KLI\"}">
                                <div id="klarna_container" hidden="hidden">
                                    <img id="klarna_load_progress" src="${contextPath}/_ui/responsive/common/images/spinner.gif">
                                </div>
                            </c:if>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </c:forEach>
    </div>
</c:if>
