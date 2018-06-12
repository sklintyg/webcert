package se.inera.intyg.webcert.web.service.underskrift.xmldsig;

import org.springframework.stereotype.Service;

import se.inera.intyg.webcert.web.service.underskrift.CommonUnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;

@Service
public class XmlUnderskriftServiceImpl implements CommonUnderskriftService {

    @Override
    public SignaturBiljett skapaSigneringsBiljettMedDigest(String intygsId, String intygsTyp, long version, String utkastJson) {
        return null;
    }
}
