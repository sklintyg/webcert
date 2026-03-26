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
package se.inera.intyg.webcert.infra.integration.hsatk.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class Commission {

  protected String commissionPurpose;
  protected String healthCareUnitHsaId;
  protected String healthCareUnitName;
  protected String healthCareProviderHsaId;
  protected String healthCareProviderName;
  protected LocalDateTime healthCareUnitStartDate;
  protected LocalDateTime healthCareUnitEndDate;

  protected String commissionName;
  protected String commissionHsaId;
  protected List<CommissionRight> commissionRight = new ArrayList<>();
  protected String healthCareProviderOrgNo;
  protected LocalDateTime healthCareProviderStartDate;
  protected LocalDateTime healthCareProviderEndDate;
  protected Boolean feignedHealthCareProvider;
  protected Boolean feignedHealthCareUnit;
  protected Boolean feignedCommission;
  protected Boolean archivedHealthCareProvider;
  protected Boolean archivedHealthCareUnit;
  protected String pharmacyIdentifier;

  @Data
  public static class CommissionRight {

    protected String activity;
    protected String informationClass;
    protected String scope;
  }
}
