package se.inera.intyg.webcert.web.service.facade.list;

import java.util.List;

public interface ListDraftsFacadeService {
    List<CertificateListItemDTO> get(ListDraftFilterDTO filter);
}
