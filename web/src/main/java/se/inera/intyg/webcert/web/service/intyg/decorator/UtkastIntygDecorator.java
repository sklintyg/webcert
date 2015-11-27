package se.inera.intyg.webcert.web.service.intyg.decorator;

import se.inera.intyg.common.support.modules.support.api.dto.CertificateResponse;

/**
 * Created by eriklupander on 2015-06-23.
 */
public interface UtkastIntygDecorator {

    void decorateWithUtkastStatus(CertificateResponse certificate);
}
