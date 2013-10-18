package se.inera.webcert.hsa.stub;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import se.inera.webcert.hsa.model.Mottagning;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;

/**
 * @author johannesc
 */
public class HsaServiceStub {

    // Data cache

    private List<Vardgivare> vardgivare = new ArrayList<>();
    private List<Medarbetaruppdrag> medarbetaruppdrag = new ArrayList<>();

    public Vardenhet getVardenhet(String hsaIdentity) {

        for (Vardgivare vg : vardgivare) {
            for (Vardenhet vardenhet : vg.getVardenheter()) {
                if (vardenhet.getId().equals(hsaIdentity)) {
                    return vardenhet;
                }
            }
        }
        return null;
    }

    public void deleteVardgivare(String id) {
        Iterator<Vardgivare> iterator = vardgivare.iterator();
        while (iterator.hasNext()) {
            Vardgivare next = iterator.next();
            if (next.getId().equals(id)) {
                iterator.remove();
            }
        }
    }

    public void deleteMedarbetareuppdrag(String hsaId) {
        Iterator<Medarbetaruppdrag> iterator = medarbetaruppdrag.iterator();
        while (iterator.hasNext()) {
            Medarbetaruppdrag next = iterator.next();
            if (next.getHsaId().equals(hsaId)) {
                iterator.remove();
            }
        }
    }

    public List<Vardgivare> getVardgivare() {
        return vardgivare;
    }

    public List<Medarbetaruppdrag> getMedarbetaruppdrag() {
        return medarbetaruppdrag;
    }

    public Mottagning getMottagning(String hsaIdentity) {
        for (Vardgivare vg : vardgivare) {
            for (Vardenhet vardenhet : vg.getVardenheter()) {
                for (Mottagning mottagning : vardenhet.getMottagningar()) {
                    if (mottagning.getId().equals(hsaIdentity)) {
                        return mottagning;
                    }
                }
            }
        }
        return null;
    }
}
