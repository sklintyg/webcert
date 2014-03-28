package se.inera.webcert.hsa.stub;

import static java.util.Arrays.asList;
import java.util.ArrayList;
import java.util.List;

/**
 * @author andreaskaltenbach
 */
public class Medarbetaruppdrag {

    public static final String VARD_OCH_BEHANDLING = "Vård och behandling";

    private String hsaId;
    private List<Uppdrag> uppdrag;


    public Medarbetaruppdrag() {
    }

    public Medarbetaruppdrag(String hsaId, List<Uppdrag> uppdrag) {
        this.hsaId = hsaId;
        this.uppdrag = uppdrag;
    }

    public String getHsaId() {
        return hsaId;
    }

    public void setHsaId(String hsaId) {
        this.hsaId = hsaId;
    }

    public List<Uppdrag> getUppdrag() {
        return uppdrag;
    }

    public void setUppdrag(List<Uppdrag> uppdrag) {
        this.uppdrag = uppdrag;
    }

    public static class Uppdrag {
        private String enhet;
        private List<String> andamal;
        public Uppdrag() {
            enhet = "";
            andamal = asList(VARD_OCH_BEHANDLING);
        }
        public Uppdrag(String enhet) {
            this(enhet, VARD_OCH_BEHANDLING);
        }
        public Uppdrag(String enhet, String andamal) {
            this.enhet = enhet;
            this.andamal = asList(andamal);
        }
        public Uppdrag(String enhet, List<String> andamal) {
            this.enhet = enhet;
            this.andamal = andamal;
        }
        public String getEnhet() {
            return enhet;
        }
        public void setEnhet(String enhet) {
            this.enhet = enhet;
        }
        public List<String> getAndamal() {
            return andamal;
        }
        public void setAndamal(List<String> andamal) {
            this.andamal = andamal;
        }
    }
}
