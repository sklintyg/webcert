package se.inera.intyg.webcert.web.service.facade.util;

import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;

public interface IntygToCertificateConverter {

    Certificate convert(IntygContentHolder intygContentHolder);
}
