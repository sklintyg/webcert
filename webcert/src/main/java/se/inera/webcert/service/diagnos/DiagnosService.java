package se.inera.webcert.service.diagnos;

import java.util.List;

import se.inera.webcert.service.diagnos.model.Diagnos;

public interface DiagnosService {

    public abstract Diagnos getDiagnosisByCode(String code);

    public abstract List<Diagnos> searchDiagnosisByCode(String codeFragment);

}
