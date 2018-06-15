/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.service.underskrift;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3._2000._09.xmldsig_.KeyInfoType;
import org.w3._2000._09.xmldsig_.ObjectFactory;
import org.w3._2000._09.xmldsig_.SignatureType;
import org.w3._2000._09.xmldsig_.SignatureValueType;
import org.w3._2002._06.xmldsig_filter2.XPathType;
import se.inera.intyg.common.support.common.enumerations.SignaturTyp;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.infra.security.common.model.AuthenticationMethod;
import se.inera.intyg.infra.xmldsig.model.IntygXMLDSignature;
import se.inera.intyg.infra.xmldsig.service.PrepareSignatureService;
import se.inera.intyg.infra.xmldsig.service.XMLDSigService;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.service.underskrift.xmldsig.UtkastModelToXMLConverter;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Base64;

public abstract class BaseXMLSignatureService extends BaseSignatureService {

    private static final Logger LOG = LoggerFactory.getLogger(BaseXMLSignatureService.class);
    private static final String DIGEST_ALGORITHM = "SHA-256";
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    @Autowired
    private UtkastModelToXMLConverter utkastModelToXMLConverter;

    @Autowired
    private PrepareSignatureService prepareSignatureService;

    @Autowired
    private XMLDSigService xmldSigService;

    protected SignaturBiljett finalizeXMLDSigSignature(String x509certificate, WebCertUser user, SignaturBiljett biljett,
            byte[] rawSignature,
            Utkast utkast) {
        IntygXMLDSignature intygXmldSignature = (IntygXMLDSignature) biljett.getIntygSignature();

        applySignature(user, rawSignature, intygXmldSignature);

        // Store X509 in SignatureType
        if (x509certificate != null && !x509certificate.isEmpty()) {
            KeyInfoType keyInfoType = xmldSigService.buildKeyInfoForCertificate(x509certificate);
            intygXmldSignature.getSignatureType().setKeyInfo(keyInfoType);
        }

        // This isn't strictly necessary...
        performBasicSignatureValidation(x509certificate, utkast, intygXmldSignature);

        String signatureXml = marshallSignatureToString(intygXmldSignature.getSignatureType());

        createAndPersistSignatureForXMLDSig(utkast, biljett, signatureXml, user);

        // If all good, change status of ticket
        biljett.setStatus(SignaturStatus.SIGNERAD);

        return biljett;
    }

    private void performBasicSignatureValidation(String x509certificate, Utkast utkast, IntygXMLDSignature intygXmldSignature) {
        // again, convert JSON to XML.
        String utkastXml = utkastModelToXMLConverter.utkastToXml(intygXmldSignature.getIntygJson(), utkast.getIntygsTyp());

        // This is the base64 encoded
        // <RegisterCertificate><intyg>...data...<Signature>...</Signature></intyg></<RegisterCertificate>>
        // that we're storing.
        String finalXml = prepareSignatureService.encodeSignatureIntoSignedXml(intygXmldSignature.getSignatureType(),
                utkastXml);

        // Only store if we received a certificate. Note - if cert comes from NIAS, it's been encoded as Base64 already.
        if (x509certificate != null && !x509certificate.isEmpty()) {

            // Due to a bug with SAXON and the JDK DSIG validator, do NOT check references.
            boolean validationResult = xmldSigService.validateSignatureValidity(finalXml, false);
            if (!validationResult) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Signature is invalid.");
            }
        }
    }

    private String marshallSignatureToString(SignatureType signatureType) {
        try {
            StringWriter sw = new StringWriter();
            JAXBElement<SignatureType> signature = new ObjectFactory().createSignature(signatureType);
            JAXBContext jaxbContext = JAXBContext.newInstance(SignatureType.class, XPathType.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(signature, sw);
            return sw.toString();

        } catch (Exception e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM, e.getMessage());
        }
    }

    private void applySignature(WebCertUser user, byte[] rawSignature, IntygXMLDSignature intygXmldSignature) {
        SignatureValueType svt = new SignatureValueType();
        if (user.getAuthenticationMethod() == AuthenticationMethod.SITHS) {
            // Don't decode RAW signatures from the NetiD plugin.
            svt.setValue(rawSignature); //rawSignature.getBytes(Charset.forName("UTF-8")));
        } else if (user.getAuthenticationMethod() == AuthenticationMethod.FAKE) {
            svt.setValue(rawSignature);
        } else {
            svt.setValue(Base64.getDecoder().decode(rawSignature)); // Remember to decode signature from Base64 to binary.
        }
        intygXmldSignature.getSignatureType().setSignatureValue(svt);
    }

    protected SignaturBiljett createAndPersistSignatureForXMLDSig(Utkast utkast, SignaturBiljett biljett, String signaturXml,
            WebCertUser user) {
        IntygXMLDSignature intygXMLDSignature = (IntygXMLDSignature) biljett.getIntygSignature();

        final String payloadJson = intygXMLDSignature.getIntygJson();

        // SigningXml is the <intyg>...</intyg> and the hash below is the sha256 digest of it.
        final String signingXml = intygXMLDSignature.getCanonicalizedIntyg();
        final String signingXmlHash = Base64.getEncoder()
                .encodeToString(intygXMLDSignature.getSignatureType().getSignedInfo().getReference().get(0).getDigestValue());

        checkIntysId(utkast, biljett);

        // Use the JSON from the DB temp storage. Convert it (again) into XML and do a digest on it. It MUST
        // match the digest used in the signing process or we have a problem.
        String utkastXml = utkastModelToXMLConverter.utkastToXml(biljett.getIntygSignature().getIntygJson(), utkast.getIntygsTyp());

        // Use the JSON->XML converted XML and perform a _new_ prepare on it so we can compare digests.
        IntygXMLDSignature intygSignature = prepareSignatureService.prepareSignature(utkastXml, biljett.getIntygsId());

        checkDigests(utkast, signingXmlHash, Base64.getEncoder()
                .encodeToString(intygSignature.getSignatureType().getSignedInfo().getReference().get(0).getDigestValue()));
        checkVersion(utkast, biljett);

        // For WC 6.1, we want to store the following:
        // INTYG_DATA - the actual data we're going to send to Intygstjansten OR the <intyg> element WITHOUT signature.
        // INTYG_HASH - the hash of the canonicalized <intyg>...</intyg> we've signed on.
        // SIGNATUR_DATA - the base64-encoded <intyg>...<Signature>...</Signature></intyg> containing the canonicalized
        // intyg data and the XMLDSig.
        Signatur signatur = new Signatur(LocalDateTime.now(), user.getHsaId(), biljett.getIntygsId(), signingXml,
                signingXmlHash, signaturXml, SignaturTyp.XMLDSIG);

        // Encode the DSIG signature into the JSON model.
        String finalJson;
        try {
            finalJson = moduleRegistry.getModuleApi(utkast.getIntygsTyp()).updateAfterSigning(payloadJson, signaturXml);
        } catch (ModuleNotFoundException | ModuleException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e);
        }

        // Update user information ("senast sparat av")
        // Add signature to the utkast and set status as signed
        Utkast savedUtkast = updateAndSaveUtkast(utkast, finalJson, signatur, user);

        // Send to Intygstjanst
        intygService.storeIntyg(savedUtkast);

        return biljett;
    }
}
