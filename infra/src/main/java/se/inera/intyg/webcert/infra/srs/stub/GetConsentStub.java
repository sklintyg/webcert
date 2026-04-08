/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.infra.srs.stub;

import java.util.Optional;
import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.GetConsentRequestType;
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.GetConsentResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.GetConsentResponseType;
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.Samtyckesstatus;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.infra.srs.stub.repository.ConsentRepository;
import se.inera.intyg.webcert.infra.srs.stub.repository.model.Consent;

@SchemaValidation(type = SchemaValidation.SchemaValidationType.BOTH)
public class GetConsentStub implements GetConsentResponderInterface {

  private static final Logger LOG = LoggerFactory.getLogger(GetConsentStub.class);

  @Autowired private ConsentRepository consentRepository;

  @Override
  public GetConsentResponseType getConsent(GetConsentRequestType getConsentRequestType) {
    LOG.info("Stub received GetConsent-request for {}.", getConsentRequestType.getPersonId());

    GetConsentResponseType response = new GetConsentResponseType();

    Optional<Personnummer> personnummer =
        Personnummer.createPersonnummer(getConsentRequestType.getPersonId());
    Optional<Consent> consent =
        consentRepository.getConsent(
            personnummer.get(), getConsentRequestType.getVardenhetId().getExtension());

    if (consent.isPresent()) {
      response.setSamtycke(true);
      response.setSamtyckesstatus(Samtyckesstatus.JA);
      response.setSparattidpunkt(consent.get().getTimestamp());
    } else {
      response.setSamtyckesstatus(Samtyckesstatus.INGET);
    }

    return response;
  }
}
