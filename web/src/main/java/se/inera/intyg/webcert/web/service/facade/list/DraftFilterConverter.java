package se.inera.intyg.webcert.web.service.facade.list;

import se.inera.intyg.webcert.persistence.utkast.repository.UtkastFilter;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;

public interface DraftFilterConverter {
    UtkastFilter convert(ListFilter filter);
}
