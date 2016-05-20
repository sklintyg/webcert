/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.intyg.converter;

import java.util.List;

import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateResponse;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygPdf;

public interface IntygModuleFacade {

    IntygPdf convertFromInternalToPdfDocument(String intygType, String internalIntygJsonModel, List<Status> statuses, boolean isEmployer)
            throws IntygModuleFacadeException;

    CertificateResponse getCertificate(String certificateId, String intygType) throws IntygModuleFacadeException;

    void registerCertificate(String intygType, String internalIntygJsonModel) throws ModuleException, IntygModuleFacadeException;

    String getRevokeCertificateRequest(String intygType, Utlatande utlatande, HoSPersonal skapatAv, String message)
            throws ModuleException, IntygModuleFacadeException;

}
