package se.inera.intyg.webcert.web.service.underskrift.xmldsig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.xmldsig.model.IntygXMLDSignature;
import se.inera.intyg.infra.xmldsig.service.PrepareSignatureServiceImpl;
import se.inera.intyg.webcert.web.service.underskrift.CommonUnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class XmlUnderskriftServiceImpl implements CommonUnderskriftService {

    @Autowired
    private UtkastModelToXMLConverter utkastModelToXMLConverter;

    @Autowired
    private PrepareSignatureServiceImpl prepareSignatureService;

    @Autowired
    private RedisTicketTracker redisTicketTracker;

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
                .build();

        redisTicketTracker.trackBiljett(biljett);
        return biljett;
    }
}
