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
package se.inera.intyg.webcert.infra.integration.hsatk.services;

import java.util.List;
import se.inera.intyg.webcert.infra.integration.hsatk.model.HealthCareProvider;
import se.inera.intyg.webcert.infra.integration.hsatk.model.HealthCareUnit;
import se.inera.intyg.webcert.infra.integration.hsatk.model.HealthCareUnitMembers;
import se.inera.intyg.webcert.infra.integration.hsatk.model.Unit;

public interface HsatkOrganizationService {

  List<HealthCareProvider> getHealthCareProvider(
      String healthCareProviderHsaId, String healthCareProviderOrgNo);

  HealthCareUnit getHealthCareUnit(String healthCareUnitMemberHsaId);

  HealthCareUnitMembers getHealthCareUnitMembers(String healtCareUnitHsaId);

  Unit getUnit(String unitHsaId, String profile);
}
