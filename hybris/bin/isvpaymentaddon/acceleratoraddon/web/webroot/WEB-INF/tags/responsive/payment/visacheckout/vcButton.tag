<%@ attribute name="imageUrl" required="true" type="java.lang.String"%>

<div class="visaCheckoutBtnDiv" hidden="hidden">
    <img alt="Visa Checkout" class="v-button btn"
         role="button" style="display:block;margin:auto;" src="${imageUrl}" onclick="vcButtonHandler(event);"/>
</div>
