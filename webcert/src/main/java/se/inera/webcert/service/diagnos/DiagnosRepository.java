package se.inera.webcert.service.diagnos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import se.inera.webcert.service.diagnos.model.Diagnos;

public class DiagnosRepository {
    
    private Map<String, Diagnos> diagnoses = new TreeMap<String, Diagnos>();
    
    private SortedSet<String> diagnoisCodesSet = new TreeSet<String>();
    
    public Diagnos getDiagnosByCode(String code) {
        return ((code = sanitizeCodeValue(code)) != null) ? diagnoses.get(code) : null;
    }

    public List<Diagnos> searchDiagnosisByCode(String codeFragment) {
        
        List<Diagnos> matches = new ArrayList<Diagnos>();
                
        String lowVal = sanitizeCodeValue(codeFragment);
        
        if (lowVal == null) {
            return matches;
        }
        
        String highVal = createHighValue(lowVal);
        
        SortedSet<String> keys = diagnoisCodesSet.subSet(lowVal, highVal);
        
        for (String key : keys) {
            matches.add(diagnoses.get(key));
        }
                
        return matches;
    }
    
    private String sanitizeCodeValue(String codeValue) {
        if (StringUtils.isNotBlank(codeValue)) {
            return StringUtils.deleteWhitespace(codeValue).toUpperCase();
        }
        
        return null;
    }
    
    public String createHighValue(String lowStr) {
        char[] highCharArray = lowStr.toCharArray();
        highCharArray[highCharArray.length-1] = ++highCharArray[highCharArray.length-1];
        return String.valueOf(highCharArray);
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
