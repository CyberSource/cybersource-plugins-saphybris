package isv.cjl.payment.utils;

import isv.cjl.payment.configuration.service.ConfigurationService;
import isv.cjl.payment.logging.LoggingHandler;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BinarySecurityTokenHandler implements SOAPHandler<SOAPMessageContext> {
    private static final Logger LOG = LoggerFactory.getLogger(BinarySecurityTokenHandler.class);
    private ConfigurationService configurationService;

    public BinarySecurityTokenHandler(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public Set<QName> getHeaders() {
        return Set.of();
    }

    public boolean handleMessage(SOAPMessageContext soapMessageContext) {
        LoggingHandler loggingHandler = new LoggingHandler();
        loggingHandler.handleMessage(soapMessageContext);
        Boolean outboundProperty = (Boolean)soapMessageContext.get("javax.xml.ws.handler.message.outbound");
        if (Boolean.TRUE.equals(outboundProperty)) {
            try {
                SOAPMessage soapMessage = soapMessageContext.getMessage();
                soapMessage = this.processSoapMessage(soapMessage);
                printSOAPMessage(soapMessage);
            } catch (Exception e) {
                LOG.error("Error processing SOAP message", e);
            }
        }

        return true;
    }

    public SOAPMessage processSoapMessage(SOAPMessage soapMessage) {
        try {
            SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();
            SOAPBody soapBody = soapMessage.getSOAPBody();
            soapBody.addAttribute(soapEnvelope.createName("Id", "wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"), "Body");
            SOAPHeader soapHeader = soapEnvelope.getHeader();
            if (soapHeader == null) {
                soapHeader = soapEnvelope.addHeader();
            }

            SOAPElement securityElement = soapHeader.addChildElement("Security", "wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
            securityElement.addNamespaceDeclaration("wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
            SOAPElement tokenElement = securityElement.addChildElement("BinarySecurityToken", "wsse");
            tokenElement.setAttribute("ValueType", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3");
            tokenElement.setAttribute("EncodingType", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary");
            tokenElement.setAttribute("wsu:Id", "X509Token");
            SecurityUtils securityUtils = new SecurityUtils(this.configurationService);
            tokenElement.addTextNode(securityUtils.generateBinarySecurityToken());
            SOAPElement securityTokenReferenceElement = securityElement.addChildElement("SecurityTokenReference", "wsse");
            SOAPElement referenceElement = securityTokenReferenceElement.addChildElement("Reference", "wsse");
            referenceElement.setAttribute("URI", "#X509Token");
            securityUtils.createDetachedSignature(securityElement, securityUtils.getKeyFromCertificate(), securityTokenReferenceElement);
            return soapMessage;
        } catch (Exception e) {
            LOG.error("Error modifying SOAP message", e);
            throw new RuntimeException(e);
        }
    }

    public boolean handleFault(SOAPMessageContext soapMessageContext) {
        return false;
    }

    public void close(MessageContext messageContext) {
    }

    public static void printSOAPMessage(SOAPMessage soapMessage) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            soapMessage.writeTo(outputStream);
            new String(outputStream.toByteArray());
        } catch (IOException | SOAPException e) {
            LOG.error("Error printing SOAP message", e);
        }

    }
}
