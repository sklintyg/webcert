package se.inera.webcert.eid.stub;

import org.junit.Test;
import se.sll.www.osif.v2_1.Osif;
import se.sll.www.osif.v2_1.VerifySignatureRequest;
import se.sll.www.osif.v2_1.VerifySignatureResponse;

import static org.junit.Assert.assertEquals;

public class OsifWebServiceStubTest {

    public static final int SITHS_PROVIDER = 5;
    private Osif stub = new OsifWebServiceStub();

    @Test
    public void okRequestContainsSubjectId() throws Exception {
        VerifySignatureRequest request = new VerifySignatureRequest();
        request.setProvider(SITHS_PROVIDER);
        request.setPolicy("policy");
        request.setSignature("userid:SIGNATURE");
        String transactionid = "transactionid";
        request.setTransactionID(transactionid);
        request.setTbsText("TEXT");
        VerifySignatureResponse response = stub.verifySignature(request);
        assertEquals(0, response.getStatus().getErrorCode());
        assertEquals(0, response.getStatus().getErrorGroup());
        assertEquals(transactionid, response.getTransactionID());
        assertEquals("userid", response.getAttributes().get(0).getValue());
    }

    @Test
    public void missingTextGivesError() throws Exception {
        VerifySignatureRequest request = new VerifySignatureRequest();
        request.setProvider(SITHS_PROVIDER);
        request.setPolicy("policy");
        request.setSignature("SIGNATURE");
        request.setTransactionID("transactionid");
        VerifySignatureResponse response = stub.verifySignature(request);
        assertEquals(218, response.getStatus().getErrorCode());
        assertEquals(17, response.getStatus().getErrorGroup());
    }

    @Test
    public void missingPolicyGivesError() throws Exception {
        VerifySignatureRequest request = new VerifySignatureRequest();
        request.setProvider(SITHS_PROVIDER);
        request.setSignature("SIGNATURE");
        request.setTbsText("TEXT");
        request.setTransactionID("transactionid");
        VerifySignatureResponse response = stub.verifySignature(request);
        assertEquals(-1, response.getStatus().getErrorCode());
        assertEquals(-1, response.getStatus().getErrorGroup());
    }

    @Test
    public void missingProviderGivesError() throws Exception {
        VerifySignatureRequest request = new VerifySignatureRequest();
        request.setSignature("SIGNATURE");
        request.setTbsText("TEXT");
        request.setPolicy("policy");
        request.setTransactionID("transactionid");
        VerifySignatureResponse response = stub.verifySignature(request);
        assertEquals(218, response.getStatus().getErrorCode());
        assertEquals(17, response.getStatus().getErrorGroup());
    }

    @Test
    public void missingSignatureGivesError() throws Exception {
        VerifySignatureRequest request = new VerifySignatureRequest();
        request.setProvider(SITHS_PROVIDER);
        request.setTbsText("TEXT");
        request.setPolicy("policy");
        request.setTransactionID("transactionid");
        VerifySignatureResponse response = stub.verifySignature(request);
        assertEquals(219, response.getStatus().getErrorCode());
        assertEquals(3, response.getStatus().getErrorGroup());
    }
}