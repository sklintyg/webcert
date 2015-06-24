package se.inera.webcert.service.intyg.decorator;

import se.inera.certificate.modules.support.api.dto.CertificateResponse;

/**
 * Created by eriklupander on 2015-06-23.
 */
public interface UtkastIntygDecorator {

    void decorateWithUtkastStatus(CertificateResponse certificate);
}
