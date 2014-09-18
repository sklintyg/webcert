package se.inera.webcert.service.diagnos;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import se.inera.webcert.service.diagnos.model.Diagnos;

public class DiagnosRepository {
    
    private Map<String, Diagnos> diagnoses = new TreeMap<String, Diagnos>();
    
    private SortedSet<String> diagnoisCodesSet = new TreeSet<String>();
    
    public Diagnos getDiagnosByCode(String code) {
        return diagnoses.get(code);
    }

    public List<Diagnos> searchDiagnosisByCode(String codeFragment) {
        
        return null;
    }
    
    public void addDiagnos(Diagnos diagnos) {
        if (diagnos != null) {
            diagnoisCodesSet.add(diagnos.getKod());
            diagnoses.put(diagnos.getKod(), diagnos);
        }
    }

    public int nbrOfDiagosis() {
        return diagnoses.size();

    }
}
