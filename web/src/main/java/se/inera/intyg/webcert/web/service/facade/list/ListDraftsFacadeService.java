package se.inera.intyg.webcert.web.service.facade.list;

import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListInfo;

public interface ListDraftsFacadeService {
    ListInfo get(ListFilter filter);
}
