package isv.sap.payment.util.api

import java.util.regex.Matcher

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import com.google.api.services.gmail.model.ListMessagesResponse
import com.google.api.services.gmail.model.Message

class GmailApi
{
    private static final JSON_FACTORY = JacksonFactory.defaultInstance
    private static final TOKENS_DIRECTORY_PATH = 'tokens'
    private static final String APPLICATION_NAME = 'Gmail API Java Quickstart'
    private static final LAST = 0
    private static final ME = 'me'

    static getLastVcoCode()
    {
        Gmail service = makeGmailService()
        ListMessagesResponse messages = service.users().messages().list(ME).execute()
        Message message = service.users().messages().get(ME, messages.messages[LAST].id).execute()
        Matcher matcher = (message.snippet =~ /\d{6}/)
        matcher.size() == 1 ? matcher[0] : null
    }

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_READONLY)
    private static final String CREDENTIALS_FILE_PATH = "${TOKENS_DIRECTORY_PATH}/credentials.json"

    /**
     * Creates an authorized Credential object.
     * @param httpTransport The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport httpTransport) throws IOException
    {
        // Load client secrets.
        InputStream input = GmailApi.getResourceAsStream(CREDENTIALS_FILE_PATH)
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(input))

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType('offline')
                .build()
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build()
        new AuthorizationCodeInstalledApp(flow, receiver).authorize('user')
    }

    private static Gmail makeGmailService()
    {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        new Gmail.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport))
                .setApplicationName(APPLICATION_NAME)
                .build()
    }
}
