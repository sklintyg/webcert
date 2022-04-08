package se.inera.intyg.webcert.web.service.facade.list;

import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;

import java.util.List;

public interface ListDecorator {
    List<ListIntygEntry> decorateWithCertificateTypeName(List<ListIntygEntry> list);

    List<ListIntygEntry> decorateWithStaffName(List<ListIntygEntry> list);

    List<ListIntygEntry> decorateAndFilterProtectedPerson(List<ListIntygEntry> list);
}
