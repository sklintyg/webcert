package se.inera.webcert.service.diagnos;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import se.inera.webcert.service.diagnos.model.Diagnos;

public class DiagnosRepositoryFactory {

    public List<String> diagnosKodFiler;
    
    public DiagnosRepository createDiagnosRepository() {
        
        
        return null;
    }
    
    public Diagnos createDiagnosFromString(String diagnosStr) {
        
        if (StringUtils.isBlank(diagnosStr)) {
            return null;
        }
        
        String[] splitedDiagnos = StringUtils.split(diagnosStr);
        
        if (splitedDiagnos == null || splitedDiagnos.length == 0) {
            return null;
        }
        
        Diagnos d = new Diagnos();
        d.setKod(splitedDiagnos[0]);
        d.setBeskrivning(splitedDiagnos[1]);
        
        return d;
    }
    
}
