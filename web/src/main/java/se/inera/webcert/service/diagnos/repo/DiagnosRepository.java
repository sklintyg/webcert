package se.inera.webcert.service.diagnos.repo;

import se.inera.webcert.service.diagnos.model.Diagnos;

import java.util.List;

public interface DiagnosRepository {

    Diagnos getDiagnosByCode(String code);

    List<Diagnos> searchDiagnosisByCode(String codeFragment);
}
