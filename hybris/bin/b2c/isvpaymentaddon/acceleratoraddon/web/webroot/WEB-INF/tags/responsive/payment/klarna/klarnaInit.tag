<%@ attribute name="klarnaSDKURL" required="true" type="java.lang.String"%>

<script type="application/javascript">

var KLARNA = {
  sdkURL: '${klarnaSDKURL}',
  loadResponse: null,
  sessionId: null,
  loaded: false
};

window.klarnaAsyncCallback = function () {
  Klarna.Credit.init({client_token: KLARNA.sessionId});

  Klarna.Credit.load({
    container: "#klarna_container"
  }, function(res) {
    KLARNA.loadResponse = res;
  });
};
</script>
