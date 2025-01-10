<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/ xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
  <head>
    <title>This is an AliPay Simulator. No action is required.</title>
    <script type="text/javascript">
      // All functions need to be defined before they're used
      // Parse GET parameters safely
      window.params = (function () {
        var parameters = {};
        try {
          if (window.location.href.indexOf("?") > -1) {
            var urlData = window.location.href.split("?")[1].split("&");
            for (var i in urlData) {
              var x = urlData[i].split("=");
              if (x[0] && x[1]) {
                parameters[x[0]] = x[1];
              }
            }
          }
        } catch (e) {
          console.error("Error parsing URL parameters");
        }
        return parameters;
      })();
      // Simple URL validation - define this BEFORE Redirect function
      function isValidUrl(url) {
        try {
          // If ACC.config is not available, fall back to basic validation
          if (
            typeof ACC !== "undefined" &&
            ACC.config &&
            ACC.config.contextPath
          ) {
            return url.indexOf(ACC.config.contextPath) === 0;
          }
          // Fallback: check if URL starts with / or current origin
          return (
            url.indexOf("/") === 0 || url.indexOf(window.location.origin) === 0
          );
        } catch (e) {
          console.error("URL validation error:", e);
          return false;
        }
      }
      // Safer redirect function
      function Redirect() {
        try {
          var returnUrl = window.params.return_url;
          if (returnUrl) {
            var decodedUrl = decodeURIComponent(
              returnUrl.replace(/\+/g, "%20")
            );
            if (isValidUrl(decodedUrl)) {
              window.location = decodedUrl;
            } else {
              console.error("Invalid redirect URL");
            }
          }
        } catch (e) {
          console.error("Redirect error:", e);
        }
      }
    </script>
  </head>
  <body onLoad="setTimeout('Redirect()', 3000)">
    <p>This is an AliPay Simulator. No action is required.</p>
  </body>
</html>
