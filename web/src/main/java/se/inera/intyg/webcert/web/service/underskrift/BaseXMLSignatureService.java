/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

import jakarta.xml.bind.JAXBElement;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3._2000._09.xmldsig_.KeyInfoType;
import org.w3._2000._09.xmldsig_.ObjectFactory;
import org.w3._2000._09.xmldsig_.SignatureType;
import org.w3._2000._09.xmldsig_.SignatureValueType;
import se.inera.intyg.common.support.common.enumerations.SignaturTyp;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.xml.XmlMarshallerHelper;
import se.inera.intyg.infra.xmldsig.model.IntygXMLDSignature;
import se.inera.intyg.infra.xmldsig.model.TransformAndDigestResponse;
import se.inera.intyg.infra.xmldsig.service.PrepareSignatureService;
import se.inera.intyg.infra.xmldsig.service.XMLDSigService;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.csintegration.certificate.FinalizedCertificateSignature;
import se.inera.intyg.webcert.web.csintegration.certificate.SignCertificateService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.service.underskrift.xmldsig.UtkastModelToXMLConverter;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

public abstract class BaseXMLSignatureService extends BaseSignatureService {

    @Autowired
    private UtkastModelToXMLConverter utkastModelToXMLConverter;

    @Autowired
    private PrepareSignatureService prepareSignatureService;

    @Autowired
    private SignCertificateService signCertificateService;

    @Autowired
    private XMLDSigService xmldSigService;

    protected SignaturBiljett finalizeXMLDSigSignature(String x509certificate, WebCertUser user, SignaturBiljett biljett,
        byte[] rawSignature,
        Utkast utkast) {
        try {
            IntygXMLDSignature intygXmldSignature = (IntygXMLDSignature) biljett.getIntygSignature();
            applySignature(rawSignature, intygXmldSignature, biljett.getSignMethod());
            storeX509InSignatureType(x509certificate, intygXmldSignature);
            String signatureXml = marshallSignatureToString(intygXmldSignature.getSignatureType());
            createAndPersistSignatureForXMLDSig(utkast, biljett, signatureXml, user);

            // If all good, change status of ticket
            biljett.setStatus(SignaturStatus.SIGNERAD);

            return biljett;
        } catch (Throwable e) {
            // For ANY type of exception, update the ticket tracker and then rethrow.
            redisTicketTracker.updateStatus(biljett.getTicketId(), SignaturStatus.OKAND);
            throw e;
        }
    }

    protected FinalizedCertificateSignature finalizeXMLDSigSignatureForCS(String x509certificate, SignaturBiljett biljett,
        byte[] rawSignature) {
        try {
            final var intygXmldSignature = (IntygXMLDSignature) biljett.getIntygSignature();
            applySignature(rawSignature, intygXmldSignature, biljett.getSignMethod());
            storeX509InSignatureType(x509certificate, intygXmldSignature);

            final var signatureXml = marshallSignatureToString(intygXmldSignature.getSignatureType());
            final var certificate = signCertificateService.sign(biljett.getIntygsId(), signatureXml, biljett.getVersion());
            biljett.setStatus(SignaturStatus.SIGNERAD);

            return FinalizedCertificateSignature.builder()
                .signaturBiljett(biljett)
                .certificate(certificate)
                .build();

        } catch (Throwable e) {
            redisTicketTracker.updateStatus(biljett.getTicketId(), SignaturStatus.OKAND);
            throw e;
        }
    }

    private void storeX509InSignatureType(String x509certificate, IntygXMLDSignature intygXmldSignature) {
        // Store X509 in SignatureType
        if (x509certificate != null && !x509certificate.isEmpty()) {
            KeyInfoType keyInfoType = xmldSigService.buildKeyInfoForCertificate(x509certificate);
            intygXmldSignature.getSignatureType().setKeyInfo(keyInfoType);
        }
    }

    private void performBasicSignatureValidation(String x509certificate, Utkast utkast, IntygXMLDSignature intygXmldSignature) {
        String utkastXml = utkastModelToXMLConverter.utkastToXml(intygXmldSignature.getIntygJson(), utkast.getIntygsTyp());
        String finalXml = prepareSignatureService.encodeSignatureIntoSignedXml(intygXmldSignature.getSignatureType(),
            utkastXml);
        if (x509certificate != null && !x509certificate.isEmpty()) {
            boolean validationResult = xmldSigService.validateSignatureValidity(finalXml, false).isValid();
            if (!validationResult) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Signature is invalid.");
            }
        }
    }

    private String marshallSignatureToString(SignatureType signatureType) {
        try {
            JAXBElement<SignatureType> signature = new ObjectFactory().createSignature(signatureType);
            return XmlMarshallerHelper.marshal(signature);
        } catch (Exception e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM, e.getMessage());
        }
    }

    private void applySignature(byte[] rawSignature, IntygXMLDSignature intygXmldSignature, SignMethod signMethod) {
        SignatureValueType svt = new SignatureValueType();
        switch (signMethod) {
            case NETID_PLUGIN:
            case FAKE:
            case SIGN_SERVICE:
                // Don't decode RAW signatures from the NetiD plugin, or Sign Service.
                svt.setValue(rawSignature);
                break;

            case GRP:
                throw new IllegalStateException("We do not handle signatures as XMLDSig for GRP, "
                    + "if you're here something has gone wrong.");
        }
        intygXmldSignature.getSignatureType().setSignatureValue(svt);
    }

    protected SignaturBiljett createAndPersistSignatureForXMLDSig(Utkast utkast, SignaturBiljett biljett, String signaturXml,
        WebCertUser user) {
        IntygXMLDSignature intygXMLDSignature = (IntygXMLDSignature) biljett.getIntygSignature();

        final String payloadJson = intygXMLDSignature.getIntygJson();

        final String signingXmlHash = Base64.getEncoder()
            .encodeToString(intygXMLDSignature.getSignatureType().getSignedInfo().getReference().get(0).getDigestValue());

        checkIntysId(utkast, biljett);

        // Use the JSON from the DB temp storage. Convert it (again) into XML and do a digest on it. It MUST
        // match the digest used in the signing process or we have a problem.
        // Note: Determine whether we should use the payload json here or the utkast.getModel.
        String utkastXml = utkastModelToXMLConverter.utkastToXml(payloadJson, utkast.getIntygsTyp());

        // Use the JSON->XML converted XML and perform a _new_ digest on it so we can compare digests.
        TransformAndDigestResponse transformAndDigestResponse = prepareSignatureService
            .transformAndGenerateDigest(utkastXml, biljett.getIntygsId());

        checkDigests(utkast.getIntygsId(), signingXmlHash, new String(transformAndDigestResponse.getDigest(), StandardCharsets.UTF_8));
        checkVersion(utkast, biljett);

        // For WC 6.1, we want to store the following:
        // INTYG_DATA - the actual data we're going to send to Intygstjansten OR the <intyg> element WITHOUT signature.
        // INTYG_HASH - the hash of the canonicalized <intyg>...</intyg> we've signed on.
        // SIGNATUR_DATA - the base64-encoded <intyg>...<Signature>...</Signature></intyg> containing the canonicalized
        // intyg data and the XMLDSig.
        final String signingXml = intygXMLDSignature.getCanonicalizedIntyg();
        final Signatur signatur = new Signatur(LocalDateTime.now(), user.getHsaId(), biljett.getIntygsId(), signingXml,
            signingXmlHash, signaturXml, SignaturTyp.XMLDSIG);

        // Encode the DSIG signature into the JSON model.
        String finalJson;
        try {
            finalJson = moduleRegistry.getModuleApi(utkast.getIntygsTyp(), utkast.getIntygTypeVersion())
                .updateAfterSigning(payloadJson, signaturXml);
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
