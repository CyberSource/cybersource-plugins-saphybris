package isv.sap.payment.utils;

import org.owasp.encoder.Encode;

public final class LogUtils
{
    private LogUtils()
    {
        // EMPTY
    }

    public static String encode(final String message)
    {
        String encodedMessage = message.replace('\n', '_')
                .replace('\r', '_')
                .replace('\t', '_');

        encodedMessage = Encode.forHtml(encodedMessage);

        return encodedMessage;
    }
}
