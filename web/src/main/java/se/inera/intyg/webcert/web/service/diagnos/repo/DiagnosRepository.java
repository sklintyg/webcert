package se.inera.intyg.webcert.web.service.diagnos.repo;

import se.inera.intyg.webcert.web.service.diagnos.model.Diagnos;

import java.io.IOException;
import java.util.List;

public interface DiagnosRepository {

    String CODE = "code";

    String DESC = "description";

    List<Diagnos> getDiagnosesByCode(String code);

    List<Diagnos> searchDiagnosisByCode(String codeFragment, int nbrOfResults);

    void openLuceneIndexReader() throws IOException;

    List<Diagnos> searchDiagnosisByDescription(String searchString, int nbrOfResults);
}
