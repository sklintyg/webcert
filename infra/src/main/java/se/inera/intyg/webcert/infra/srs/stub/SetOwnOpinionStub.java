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
import se.inera.intyg.clinicalprocess.healthcond.srs.setownopinion.v1.SetOwnOpinionRequestType;
import se.inera.intyg.clinicalprocess.healthcond.srs.setownopinion.v1.SetOwnOpinionResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.srs.setownopinion.v1.SetOwnOpinionResponseType;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.ResultCodeEnum;

@SchemaValidation(type = SchemaValidation.SchemaValidationType.BOTH)
public class SetOwnOpinionStub implements SetOwnOpinionResponderInterface {

  private static final Logger LOG = LoggerFactory.getLogger(SetOwnOpinionStub.class);

  @Override
  public SetOwnOpinionResponseType setOwnOpinion(
      SetOwnOpinionRequestType setOwnOpinionRequestType) {
    LOG.info(
        "Stub received SetOwnOpinion-request for vardgivare: {}, intyg-id: {}, opinion: {}.",
        setOwnOpinionRequestType.getVardgivareId(),
        setOwnOpinionRequestType.getIntygId(),
        setOwnOpinionRequestType.getEgenBedomningRisk());

    SetOwnOpinionResponseType response = new SetOwnOpinionResponseType();
    response.setResultCode(ResultCodeEnum.OK);

    return response;
  }
}
