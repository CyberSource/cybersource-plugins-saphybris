$(function () {
    $("<style id='antiClickjack'>html { display: none; }</style>").appendTo("head");
    if (self === top) {
        var antiClickjack = document.getElementById("antiClickjack");
        antiClickjack.parentNode.removeChild(antiClickjack);
    } else {
        top.location = self.location;
    }
});