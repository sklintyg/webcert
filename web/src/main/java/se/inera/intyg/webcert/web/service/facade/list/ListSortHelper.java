package se.inera.intyg.webcert.web.service.facade.list;

import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListColumnType;
import se.inera.intyg.webcert.web.service.facade.list.dto.CertificateListItem;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;

import java.util.List;

public interface ListSortHelper {
    List<CertificateListItem> sort(List<CertificateListItem> list, String order, boolean ascending);
}
