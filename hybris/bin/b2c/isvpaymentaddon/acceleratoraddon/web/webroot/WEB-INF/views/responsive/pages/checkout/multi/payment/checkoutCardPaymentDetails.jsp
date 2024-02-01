<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="formElement" tagdir="/WEB-INF/tags/responsive/formElement" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<div class="creditCardDetails">
    <form:form id="silentOrderPostForm" name="silentOrderPostForm" modelAttribute="sopPaymentDetailsForm" action="#" method="POST">
        <div class="checkout-paymentmethod">
            <div class="form-group">
                <br>
                <formElement:formSelectBox idKey="card_cardType" labelKey="payment.cardType" path="card_cardType" selectCSSClass="form-control" mandatory="true" skipBlank="false" skipBlankMessageKey="payment.cardType.pleaseSelect" items="${sopCardTypes}" tabindex="1"/>
                <div class="help-block">
                <span id="card_cardType_errors" hidden="hidden">
                    <spring:theme code="checkout.card.type.error"/>
                </span>
                </div>
            </div>
            <div class="form-group">
                <formElement:formInputBox idKey="card_accountNumber" labelKey="payment.cardNumber" path="card_accountNumber" inputCSS="form-control" mandatory="true" tabindex="3" autocomplete="off" />
                <div class="help-block">
                <span id="card_accountNumber_errors" hidden="hidden">
                    <spring:theme code="checkout.card.number.error"/>
                </span>
                </div>
            </div>

            <fieldset id="startDate">
                <label for="" class="control-label"><spring:theme code="payment.startDate"/></label>
                <div class="row">
                    <div class="col-xs-6">
                        <formElement:formSelectBox idKey="StartMonth" selectCSSClass="form-control" labelKey="payment.month" path="card_startMonth" mandatory="true" skipBlank="false" skipBlankMessageKey="payment.month" items="${months}" tabindex="4"/>
                    </div>
                    <div class="col-xs-6">
                        <formElement:formSelectBox idKey="StartYear" selectCSSClass="form-control" labelKey="payment.year" path="card_startYear" mandatory="true" skipBlank="false" skipBlankMessageKey="payment.year" items="${startYears}" tabindex="7"/>
                    </div>
                </div>
            </fieldset>

            <fieldset id="cardDate">
                <label for="" class="control-label"><spring:theme code="payment.expiryDate"/></label>
                <div class="row">
                    <div class="col-xs-6">
                        <formElement:formSelectBox idKey="ExpiryMonth" selectCSSClass="form-control" labelKey="payment.month" path="card_expirationMonth" mandatory="true" skipBlank="false" skipBlankMessageKey="payment.month" items="${months}" tabindex="6"/>
                        <div class="cs-help-block col-xs-6">
                        <span id="card_expirationMonth_errors" hidden="hidden">
                            <spring:theme code="checkout.card.expire.month.error"/>
                        </span>
                        </div>
                    </div>
                    <div class="col-xs-6">
                        <formElement:formSelectBox idKey="ExpiryYear" selectCSSClass="form-control" labelKey="payment.year" path="card_expirationYear" mandatory="true" skipBlank="false" skipBlankMessageKey="payment.year" items="${expiryYears}" tabindex="7"/>
                        <div class="cs-help-block col-xs-6">
                        <span id="card_expirationYear_errors" hidden="hidden">
                            <spring:theme code="checkout.card.expire.year.error"/>
                        </span>
                        </div>
                    </div>
                </div>
            </fieldset>

            <div class="row">
                <div class="col-xs-6">
                    <formElement:formInputBox idKey="card_cvNumber" labelKey="payment.cvn" path="card_cvNumber" inputCSS="form-control" mandatory="true" tabindex="8" />
                </div>
            </div>

            <div class="row">
                <div class="col-xs-6">
                    <div id="issueNum">
                        <formElement:formInputBox idKey="card_issueNumber" labelKey="payment.issueNumber" path="card_issueNumber" inputCSS="text" mandatory="false" tabindex="9"/>
                    </div>
                </div>
            </div>
        </div>

    </form:form>
</div>
