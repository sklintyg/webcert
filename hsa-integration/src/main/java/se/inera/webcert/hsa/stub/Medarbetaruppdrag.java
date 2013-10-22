package se.inera.webcert.hsa.stub;

import java.util.List;

/**
 * @author andreaskaltenbach
 */
public class Medarbetaruppdrag {

    public static final String VARD_OCH_BEHANDLING = "Vård och behandling";

    private String hsaId;
    private List<String> enhetIds;

    private String andamal = VARD_OCH_BEHANDLING;

    public Medarbetaruppdrag() {
    }

    public Medarbetaruppdrag(String hsaId, List<String> enhetIds) {
        this(hsaId, enhetIds, VARD_OCH_BEHANDLING);
    }

    public Medarbetaruppdrag(String hsaId, List<String> enhetIds, String andamal) {
        this.hsaId = hsaId;
        this.enhetIds = enhetIds;
        this.andamal = andamal;
    }

    public String getHsaId() {
        return hsaId;
    }

    public void setHsaId(String hsaId) {
        this.hsaId = hsaId;
    }

    public List<String> getEnhetIds() {
        return enhetIds;
    }

    public void setEnhetIds(List<String> enhetIds) {
        this.enhetIds = enhetIds;
    }

    public String getAndamal() {
        return andamal;
    }

    public void setAndamal(String andamal) {
        this.andamal = andamal;
    }
}
