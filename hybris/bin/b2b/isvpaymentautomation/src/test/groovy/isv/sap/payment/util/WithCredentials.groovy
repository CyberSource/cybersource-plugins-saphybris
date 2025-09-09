package isv.sap.payment.util

import isv.sap.payment.data.Credentials

trait WithCredentials
{
    static final PAYPAL_CREDENTIALS = new Credentials(email: 'ap.paypal@cybs.com',
            usercode: 'Password1',)

    static final GOOGLE_CREDENTIALS = new Credentials(email: System.properties['googlepay.username'],
            usercode: System.properties['googlepay.userCode'],)

    static final B2B_CREDENTIALS = new Credentials(email: 'william.hunter@rustic-hw.com',
            usercode: '24451234',)

    static final ASM_CREDENTIALS = new Credentials(email: 'asagent',
            usercode: '123456',)

    static credentialsMap = ['paypal': PAYPAL_CREDENTIALS,
                             'google': GOOGLE_CREDENTIALS,
                             'b2b'   : B2B_CREDENTIALS,
                             'asm'   : ASM_CREDENTIALS,]

    Map<String, Credentials> getCredentials()
    {
        credentialsMap
    }
}