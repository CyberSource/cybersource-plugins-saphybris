<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/
xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
    <title>This is an AliPay Simulator. No action is required.</title>
    <script type="text/javascript">
        // Parse GET parameters
        window.params = function()
        {
            var parameters = {};
            var urlData = window.location.href.split('?')[1].split('&');
            for(var i in urlData)
            {
                x = urlData[i].split('=');
                parameters[x[0]] = x[1];
            }
            return parameters;
        }();
        // Redirect to HTTP GET "return_url" value
        function Redirect()
        {
            window.location = decodeURIComponent((window.params.return_url).replace(/\+/g, '%20'))
        }
    </script>
</head>
<body onLoad="setTimeout('Redirect()', 3000)">
<p>This is an AliPay Simulator. No action is required.</p>
</body>
</html>