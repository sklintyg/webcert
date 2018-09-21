package se.inera.intyg.webcert.integration.fmb.model.fmdxinfo;

import java.util.List;
import se.inera.intyg.webcert.integration.fmb.model.Kod;

public interface FmxBeskrivning {
    String getBeskrivning();
    List<Kod> getCentralkod();
    List<Kod> getKompletterandekod();
}
