/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.clinicalprocess.healthcond.srs.setconsent.v1.SetConsentRequestType;
import se.inera.intyg.clinicalprocess.healthcond.srs.setconsent.v1.SetConsentResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.srs.setconsent.v1.SetConsentResponseType;
import se.inera.intyg.infra.integration.srs.stub.repository.ConsentRepository;
import se.inera.intyg.schemas.contract.Personnummer;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.ResultCodeEnum;

@SchemaValidation(type = SchemaValidation.SchemaValidationType.BOTH)
public class SetConsentStub implements SetConsentResponderInterface {

  private static final Logger LOG = LoggerFactory.getLogger(SetConsentStub.class);

  @Autowired private ConsentRepository consentRepository;

  @Override
  public SetConsentResponseType setConsent(SetConsentRequestType setConsentRequestType) {
    LOG.info("Stub received SetConsent-request for {}.", setConsentRequestType.getPersonId());

    Personnummer personnummer =
        Personnummer.createPersonnummer(setConsentRequestType.getPersonId()).get();
    consentRepository.setConsent(
        personnummer,
        setConsentRequestType.getVardenhetId().getExtension(),
        setConsentRequestType.isSamtycke());
    SetConsentResponseType response = new SetConsentResponseType();
    response.setResultCode(ResultCodeEnum.OK);
    return response;
  }
}
