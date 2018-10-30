package se.inera.intyg.webcert.web.service.icf.resource;

import se.inera.intyg.webcert.web.web.controller.api.dto.IcfKod;

public interface IcfTextResource {

    void init();

    IcfKod lookupTextByIcfKod(String icfKod);
}
