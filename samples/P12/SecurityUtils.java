package isv.cjl.payment.utils;

import isv.cjl.payment.configuration.service.ConfigurationService;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.StringTokenizer;
import javax.naming.ConfigurationException;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.DigestMethodParameterSpec;
import javax.xml.crypto.dsig.spec.SignatureMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.soap.SOAPElement;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class SecurityUtils {
    private ConfigurationService configurationService;

    public SecurityUtils(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public String generateBinarySecurityToken() throws ConfigurationException, CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {
        X509Certificate certificate = this.extractMerchantCertificateFromFile();
        byte[] certificateBytes = certificate.getEncoded();
        return Base64.getEncoder().encodeToString(certificateBytes);
    }

    private X509Certificate extractMerchantCertificateFromFile() throws IOException, ConfigurationException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
        KeyStore merchantKeyStore = this.loadCertificateIntoKeyStore();
        String merchantKeyAlias = this.extractMerchantKeyAlias(merchantKeyStore);

        try {
            KeyStore.PrivateKeyEntry e = (KeyStore.PrivateKeyEntry)merchantKeyStore.getEntry(merchantKeyAlias, new KeyStore.PasswordProtection(this.configurationService.getString("isv.payment.p12.keyPass").toCharArray()));
            return (X509Certificate)e.getCertificate();
        } catch (UnrecoverableEntryException var4) {
            return null;
        }
    }

    public PrivateKey getKeyFromCertificate() throws IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException, ConfigurationException {
        KeyStore merchantKeyStore = this.loadCertificateIntoKeyStore();
        String merchantKeyAlias = this.extractMerchantKeyAlias(merchantKeyStore);

        try {
            KeyStore.PrivateKeyEntry e = (KeyStore.PrivateKeyEntry)merchantKeyStore.getEntry(merchantKeyAlias, new KeyStore.PasswordProtection(this.configurationService.getString("isv.payment.p12.keyPass").toCharArray()));
            return e.getPrivateKey();
        } catch (UnrecoverableEntryException var4) {
            return null;
        }
    }

    private String extractMerchantKeyAlias(KeyStore merchantKeyStore) throws KeyStoreException {
        Enumeration<String> enumKeyStore = merchantKeyStore.aliases();
        ArrayList<String> array = new ArrayList();

        while(enumKeyStore.hasMoreElements()) {
            String internalMerchantKeyAlias = (String)enumKeyStore.nextElement();
            array.add(internalMerchantKeyAlias);
        }

        return this.keyAliasValidator(array, this.configurationService.getString("isv.payment.p12.keyAlias"));
    }

    public void createDetachedSignature(SOAPElement signatureElement, PrivateKey privateKey, SOAPElement securityTokenReference) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        XMLSignatureFactory xmlSignatureFactory = XMLSignatureFactory.getInstance("DOM");
        DigestMethod digestMethod = xmlSignatureFactory.newDigestMethod("http://www.w3.org/2001/04/xmlenc#sha256", (DigestMethodParameterSpec)null);
        ArrayList<Transform> transformList = new ArrayList();
        Transform envTransform = xmlSignatureFactory.newTransform("http://www.w3.org/2001/10/xml-exc-c14n#", (TransformParameterSpec)null);
        transformList.add(envTransform);
        ArrayList<Reference> refList = new ArrayList();
        Reference refBody = xmlSignatureFactory.newReference("#Body", digestMethod, transformList, (String)null, (String)null);
        refList.add(refBody);
        CanonicalizationMethod cm = xmlSignatureFactory.newCanonicalizationMethod("http://www.w3.org/2001/10/xml-exc-c14n#", (C14NMethodParameterSpec)null);
        SignatureMethod sm = xmlSignatureFactory.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", (SignatureMethodParameterSpec)null);
        SignedInfo signedInfo = xmlSignatureFactory.newSignedInfo(cm, sm, refList);
        DOMSignContext signContext = new DOMSignContext(privateKey, signatureElement);
        signContext.setDefaultNamespacePrefix("ds");
        signContext.putNamespacePrefix("http://www.w3.org/2000/09/xmldsig#", "ds");
        KeyInfoFactory keyFactory = KeyInfoFactory.getInstance();
        DOMStructure domKeyInfo = new DOMStructure(securityTokenReference);
        KeyInfo keyInfo = keyFactory.newKeyInfo(Collections.singletonList(domKeyInfo));
        XMLSignature signature = xmlSignatureFactory.newXMLSignature(signedInfo, keyInfo);
        signContext.setBaseURI("");
        signature.sign(signContext);
    }

    private KeyStore loadCertificateIntoKeyStore() throws IOException, ConfigurationException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
        if (this.configurationService.getString("isv.payment.p12.keyAlias") == null) {
            throw new ConfigurationException("Key Alias is missing in properties file.");
        } else {
            String keyFilePath = PropertiesUtil.getKeyFilePath(this.configurationService);
            String keyPass = this.configurationService.getString("isv.payment.p12.keyPass");
            KeyStore merchantKeyStore = KeyStore.getInstance("PKCS12", new BouncyCastleProvider());
            merchantKeyStore.load(new FileInputStream(keyFilePath), keyPass.toCharArray());
            return merchantKeyStore;
        }
    }

    private String keyAliasValidator(ArrayList<String> array, String merchantID) {
        for(String s : array) {
            StringTokenizer str = new StringTokenizer(s, ",");

            while(str.hasMoreTokens()) {
                String tempKeyAlias = str.nextToken();
                if (tempKeyAlias.contains("CN")) {
                    str = new StringTokenizer(tempKeyAlias, "=");

                    while(str.hasMoreElements()) {
                        String result = str.nextToken();
                        if (result.equalsIgnoreCase(merchantID)) {
                            return s;
                        }
                    }
                }
            }
        }

        return null;
    }
}
