package se.inera.webcert.eid.stub;

import se.inera.webcert.eid.services.SignatureService;
import se.sll.www.osif.v2_1.EncodeTBSRequest;
import se.sll.www.osif.v2_1.EncodeTBSResponse;
import se.sll.www.osif.v2_1.GenerateChallengeRequest;
import se.sll.www.osif.v2_1.GenerateChallengeResponse;
import se.sll.www.osif.v2_1.Osif;
import se.sll.www.osif.v2_1.Property;
import se.sll.www.osif.v2_1.Status;
import se.sll.www.osif.v2_1.VerifyAuthenticationRequest;
import se.sll.www.osif.v2_1.VerifyAuthenticationResponse;
import se.sll.www.osif.v2_1.VerifyCertificateRequest;
import se.sll.www.osif.v2_1.VerifyCertificateResponse;
import se.sll.www.osif.v2_1.VerifySignatureRequest;
import se.sll.www.osif.v2_1.VerifySignatureResponse;

public class OsifWebServiceStub implements Osif {
    @Override
    public VerifyCertificateResponse verifyCertificate(VerifyCertificateRequest verifyCertificateRequest) {
        return null;
    }

    @Override
    public EncodeTBSResponse encodeTBS(EncodeTBSRequest encodeTBSRequest) {
        return null;
    }

    @Override
    public VerifyAuthenticationResponse verifyAuthentication(VerifyAuthenticationRequest verifyAuthenticationRequest) {
        return null;
    }

    @Override
    public VerifySignatureResponse verifySignature(VerifySignatureRequest verifySignatureRequest) {
        VerifySignatureResponse response = new VerifySignatureResponse();
        if (validate(verifySignatureRequest, response)) {
            return response;
        }
        response.setStatus(okStatus());
        response.setTransactionID(verifySignatureRequest.getTransactionID());
        response.getAttributes().add(createPropery("Subject.SerialNumber", verifySignatureRequest.getSignature().split(":")[0]));
        return response;
    }

    private boolean validate(VerifySignatureRequest verifySignatureRequest, VerifySignatureResponse response) {
        if (verifySignatureRequest.getTbsText() == null) {
            response.setStatus(failStatus(218, 17));
            return true;
        }
        if (verifySignatureRequest.getPolicy() == null) {
            response.setStatus(failStatus(-1, -1));
            return true;
        }
        if (verifySignatureRequest.getProvider() != SignatureService.NETID) {
            response.setStatus(failStatus(218, 17));
            return true;
        }
        if (verifySignatureRequest.getSignature() == null) {
            response.setStatus(failStatus(219, 3));
            return true;
        }
        return false;
    }

    private Status failStatus(int code, int group) {
        Status status = new Status();
        status.setErrorCode(code);
        status.setErrorGroup(group);
        status.setErrorCodeDescription("Error code description");
        status.setErrorGroupDescription("Error group description");
        return status;
    }

    private Status okStatus() {
        Status status = new Status();
        status.setErrorCode(0);
        status.setErrorGroup(0);
        return status;
    }

    private Property createPropery(String name, String value) {
        Property property = new Property();
        property.setName(name);
        property.setValue(value);
        return property;
    }

    @Override
    public GenerateChallengeResponse generateChallenge(GenerateChallengeRequest generateChallengeRequest) {
        return null;
    }
}
