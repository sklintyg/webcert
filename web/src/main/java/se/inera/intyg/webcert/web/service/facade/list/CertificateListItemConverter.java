package se.inera.intyg.webcert.web.service.facade.list;

import se.inera.intyg.webcert.web.service.facade.list.dto.CertificateListItem;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListType;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;

public interface CertificateListItemConverter {
    CertificateListItem convert(ListIntygEntry listIntygEntry, ListType listType);
}
