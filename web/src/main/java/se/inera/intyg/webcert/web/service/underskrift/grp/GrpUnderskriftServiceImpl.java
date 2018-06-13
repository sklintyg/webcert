package se.inera.intyg.webcert.web.service.underskrift.grp;

import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.underskrift.CommonUnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.grp.dto.IntygGRPSignature;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class GrpUnderskriftServiceImpl implements CommonUnderskriftService {

    @Autowired
    private RedisTicketTracker redisTicketTracker;

    @Override
    public SignaturBiljett skapaSigneringsBiljettMedDigest(String intygsId, String intygsTyp, long version, String intygJson) {
        String hash = createHash(intygJson);

        IntygGRPSignature intygGRPSignature = new IntygGRPSignature(intygJson, hash);

        SignaturBiljett biljett = SignaturBiljett.SignaturBiljettBuilder.aSignaturBiljett()
                .withTicketId(UUID.randomUUID().toString())
                .withIntygsId(intygsId)
                .withVersion(version)
                .withIntygSignature(intygGRPSignature)
                .withStatus(SignaturStatus.BEARBETAR)
                .withSkapad(LocalDateTime.now())
                .build();

        redisTicketTracker.trackBiljett(biljett);
        return biljett;
    }

    private String createHash(String payload) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            sha.update(payload.getBytes("UTF-8"));
            byte[] digest = sha.digest();
            return new String(Hex.encodeHex(digest));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
