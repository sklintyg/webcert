package se.inera.webcert.eid.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import se.sll.www.osif.v2_1.Osif;
import se.sll.www.osif.v2_1.Property;
import se.sll.www.osif.v2_1.Status;
import se.sll.www.osif.v2_1.VerifySignatureRequest;
import se.sll.www.osif.v2_1.VerifySignatureResponse;

public class SignatureServiceImpl implements SignatureService {

    private static final String SUBJECT_SERIAL_NUMBER = "Subject.SerialNumber";
    private static final Logger LOG = LoggerFactory.getLogger(SignatureServiceImpl.class);

    @Autowired
    private Osif osif;

    @Value("${eid.osif.policy}")
    private String policy;

    @Override
    public boolean validateSiths(String userId, String hash, String signature) {
        VerifySignatureRequest request = new VerifySignatureRequest();
        request.setPolicy(policy);
        request.setProvider(NETID);
        request.setTbsText(hash);
        request.setSignature(signature);
        VerifySignatureResponse response = osif.verifySignature(request);
        if (!isOk(response)) {
            Status status = response.getStatus();
            LOG.error("Kunde inte verifiera signatur för {}, response code:{} description:{} group:{} groupDescription{}",
                    new Object[] {
                            userId, status.getErrorCode(), status.getErrorCodeDescription(), status.getErrorGroup(),
                            status.getErrorGroupDescription()
                    });
            return false;
        } else {
            return userId.equals(getUserId(response));
        }
    }

    private boolean isOk(VerifySignatureResponse response) {
        Status status = response.getStatus();
        return status.getErrorCode() == 0 && status.getErrorGroup() == 0;
    }

    private String getUserId(VerifySignatureResponse response) {
        for (Property property : response.getAttributes()) {
            if (SUBJECT_SERIAL_NUMBER.equals(property.getName())) {
                return property.getValue();
            }
        }

        return null;
    }
}
