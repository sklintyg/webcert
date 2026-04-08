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
package se.inera.intyg.webcert.infra.logmessages;

import java.io.Serializable;

/**
 * Defines a single PDL logged "resource", e.g. which patient on which care unit that was logged and
 * what type of information ({@link ResourceType}) that was logged about the patient.
 *
 * <p>Created by eriklupander on 2016-03-02.
 */
public class PdlResource implements Serializable {

  private Patient patient;
  private String resourceType;
  private Enhet resourceOwner;

  public Patient getPatient() {
    return patient;
  }

  public void setPatient(Patient patient) {
    this.patient = patient;
  }

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public Enhet getResourceOwner() {
    return resourceOwner;
  }

  public void setResourceOwner(Enhet resourceOwner) {
    this.resourceOwner = resourceOwner;
  }
}
