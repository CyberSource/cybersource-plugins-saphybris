<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="formElement" tagdir="/WEB-INF/tags/responsive/formElement" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="flex" tagdir="/WEB-INF/tags/addons/isvpaymentaddon/responsive/payment/flex" %>

<div class="creditCardDetails">
    <c:url value="/checkout/payment/flex/pay" var="formAction"/>

    <form:form id="flexMicroformPaymentForm" name="flexMicroformPaymentForm" modelAttribute="sopPaymentDetailsForm"
               action="${formAction}" method="POST">
        <div class="checkout-paymentmethod">

            <div id="flexError" class="alert alert-danger" hidden="hidden"></div>

            <div>
                <img id="flexWait" src="${contextPath}/_ui/responsive/common/images/spinner.gif"/>
            </div>

            <label id="flexCardNumber-label" class="control-label"><spring:theme code="payment.cardNumber"/></label>

            <div id="flexCardNumber-container"></div>

            <c:if test="${not flexCardTypeSelection}">
                <label id="flexCardType-label" class="control-label"></label>
            </c:if>

            <div class="form-group">
                <input type="hidden" id="card_accountNumber" name="card_accountNumber"/>
                <div class="cs-help-block">
                    <span id="card_accountNumber_errors" hidden="hidden">
                        <spring:theme code="checkout.card.number.error"/>
                    </span>
                </div>
            </div>

            <c:if test="${flexCardTypeSelection}">
            <div class="form-group">
                <fieldset id="cardType">
                    <div class="row">
                        <div class="col-xs-12">
                            <formElement:formSelectBox idKey="card_cardType" selectCSSClass="form-control"
                                                       labelKey="payment.cardType" path="card_cardType"
                                                       mandatory="true" skipBlank="false"
                                                       skipBlankMessageKey="payment.cardType.pleaseSelect"
                                                       items="${sopCardTypes}" tabindex="1"/>
                            <div class="cs-help-block col-xs-6">
                                <span id="card_cardType_errors" hidden="hidden">
                                    <spring:theme code="checkout.card.type.error"/>
                                </span>
                            </div>
                        </div>
                    </div>
                </fieldset>
            </div>
            </c:if>

            <fieldset id="cardDate">
                <label for="" class="control-label"><spring:theme code="payment.expiryDate"/></label>
                <div class="row">
                    <div class="col-xs-6">
                        <formElement:formSelectBox idKey="ExpiryMonth" selectCSSClass="form-control"
                                                   labelKey="payment.month" path="card_expirationMonth" mandatory="true"
                                                   skipBlank="false" skipBlankMessageKey="payment.month"
                                                   items="${months}" itemValue="name" tabindex="2"/>
                        <div class="cs-help-block col-xs-6">
                            <span id="card_expirationMonth_errors" hidden="hidden">
                                <spring:theme code="checkout.card.expire.month.error"/>
                            </span>
                        </div>
                    </div>
                    <div class="col-xs-6">
                        <formElement:formSelectBox idKey="ExpiryYear" selectCSSClass="form-control"
                                                   labelKey="payment.year" path="card_expirationYear" mandatory="true"
                                                   skipBlank="false" skipBlankMessageKey="payment.year"
                                                   items="${expiryYears}" tabindex="3"/>
                        <div class="cs-help-block col-xs-6">
                            <span id="card_expirationYear_errors" hidden="hidden">
                                <spring:theme code="checkout.card.expire.year.error"/>
                            </span>
                        </div>
                    </div>
                </div>
            </fieldset>

            <div class="form-group">
                <label id="flexSecurityCode-label" class="control-label"><spring:theme code="payment.cvn"/></label>

                <div id="flexSecurityCode-container"></div>
            </div>

            <div class="form-group">
                <input type="hidden" id="card_cvNumber" name="card_cvNumber"/>
                <div class="cs-help-block">
                    <span id="card_cvNumber_errors" hidden="hidden">
                        <spring:theme code="checkout.card.cvn.error"/>
                    </span>
                </div>
            </div>

            <input type="hidden" id="card_flexToken" name="card_flexToken"/>
        </div>

    </form:form>

    <c:url value="/checkout/payment/flex/newJwk" var="flexNewJwkEndpointUrl"/>
    <c:url value="/checkout/payment/flex/verifyToken" var="flexVerifyTokenEndpointUrl"/>

    <flex:microform
            flexSdkUrl="${flexSdkUrl}"
            flexNewJwkEndpointUrl="${flexNewJwkEndpointUrl}"
            flexVerifyTokenEndpointUrl="${flexVerifyTokenEndpointUrl}"
            flexCardNumberContainerSelector="#flexCardNumber-container"
            flexSecurityCodeSelector="#flexSecurityCode-container"
    />
</div>
