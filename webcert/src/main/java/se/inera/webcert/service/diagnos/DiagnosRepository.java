package se.inera.webcert.service.diagnos;

import java.util.Map;
import java.util.TreeMap;

import se.inera.webcert.service.diagnos.model.Diagnos;

public class DiagnosRepository {

    private Map<String, Diagnos> diagnoses = new TreeMap<String, Diagnos>();
        
    public Diagnos getDiagnosByCode(String code) {
        return diagnoses.get(code);
    }
    
    public void addDiagnos(Diagnos diagnos) {
        diagnoses.put(diagnos.getKod(), diagnos);
    }
}
