package se.inera.webcert.service.diagnos.repo;

import java.util.List;

import se.inera.webcert.service.diagnos.model.Diagnos;

public interface DiagnosRepository {

    public abstract Diagnos getDiagnosByCode(String code);

    public abstract List<Diagnos> searchDiagnosisByCode(String codeFragment);

}
