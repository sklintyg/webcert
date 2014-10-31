package se.inera.webcert.eid.services;

public interface SignatureService {

    int NETID = 5;

    /**
     *
     * @param userId Id of signing user
     * @param hash Signed text
     * @param signature Generated signature
     * @return true if signature is valid and by signing user.
     */
    boolean validateSiths(String userId, String hash, String signature);
}
