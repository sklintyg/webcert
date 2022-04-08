package se.inera.intyg.webcert.web.service.facade.list;

import se.inera.intyg.webcert.web.service.facade.list.dto.CertificateListItem;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;

import java.util.List;

public interface ListPaginationHelper {
    List<CertificateListItem> paginate(List<CertificateListItem> list, ListFilter filter);
}
