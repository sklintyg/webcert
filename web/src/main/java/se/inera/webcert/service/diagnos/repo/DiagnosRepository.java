package se.inera.webcert.service.diagnos.repo;

import se.inera.webcert.service.diagnos.model.Diagnos;

import java.io.IOException;
import java.util.List;

public interface DiagnosRepository {
    
    static String CODE = "code";
    
    static String DESC = "description";

    Diagnos getDiagnosByCode(String code);

    List<Diagnos> searchDiagnosisByCode(String codeFragment);

    void openLuceneIndexReader() throws IOException;

    List<Diagnos> searchDiagnosisByDescription(String searchString, int nbrOfResults);
}
