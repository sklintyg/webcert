package se.inera.intyg.webcert.web.service.underskrift.xmldsig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.xmldsig.model.IntygXMLDSignature;
import se.inera.intyg.infra.xmldsig.service.PrepareSignatureServiceImpl;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.underskrift.BaseXMLSignatureService;
import se.inera.intyg.webcert.web.service.underskrift.CommonUnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class XmlUnderskriftServiceImpl extends BaseXMLSignatureService implements CommonUnderskriftService {

    @Autowired
    private UtkastModelToXMLConverter utkastModelToXMLConverter;

    @Autowired
    private PrepareSignatureServiceImpl prepareSignatureService;

    @Autowired
    private RedisTicketTracker redisTicketTracker;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Override
    public SignaturBiljett skapaSigneringsBiljettMedDigest(String intygsId, String intygsTyp, long version, String utkastJson) {
        String registerCertificateXml = utkastModelToXMLConverter.utkastToXml(utkastJson, intygsTyp);
        IntygXMLDSignature intygSignature = prepareSignatureService.prepareSignature(registerCertificateXml, intygsId);
        intygSignature.setIntygJson(utkastJson);

        SignaturBiljett biljett = SignaturBiljett.SignaturBiljettBuilder.aSignaturBiljett()
                .withTicketId(UUID.randomUUID().toString())
                .withIntygsId(intygsId)
                .withVersion(version)
                .withIntygSignature(intygSignature)
                .withStatus(SignaturStatus.BEARBETAR)
                .withSkapad(LocalDateTime.now())
                .withHash(intygSignature.getSignedInfoForSigning())
                .build();

        redisTicketTracker.trackBiljett(biljett);
        return biljett;
    }

    public SignaturBiljett finalizeXmlSignature(SignaturBiljett biljett, byte[] signatur, String certifikat, Utkast utkast,
            WebCertUser user) {
        monitoringLogService.logIntygSigned(utkast.getIntygsId(), utkast.getIntygsTyp(), user.getHsaId(), user.getAuthenticationScheme(),
                utkast.getRelationKod());

        finalizeXMLDSigSignature(certifikat, user, biljett, signatur, utkast);
        redisTicketTracker.updateBiljett(biljett);
        return biljett;

    }
}
