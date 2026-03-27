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

/**
 * Kan vara kemlabbsvar, journaltext, remiss, översikt, samtycke, patientrelation, sätta spärr,
 * rapport, Översikt sjukskrivning osv.
 *
 * <p>Created by eriklupander on 2016-03-02.
 */
public enum ResourceType {
  RESOURCE_TYPE_INTYG("Intyg"),

  RESOURCE_TYPE_SJUKFALL("Sjukfall"),

  RESOURCE_TYPE_SAMTYCKE("Samtycke"),

  RESOURCE_TYPE_PREDIKTION_SRS("Prediktion från SRS av risk för lång sjukskrivning"),

  RESOURCE_TYPE_FMU_OVERSIKT("Översikt försäkringsmedicinska utredningar"),
  RESOURCE_TYPE_FMU("Försäkringsmedicinsk utredning"),
  RESOURCE_TYPE_FMU_BESOK("Besök i försäkringsmedicinsk utredning"),
  RESOURCE_TYPE_FMU_AVVIKELSE("Avvikelse i försäkringsmedicinsk utredning"),
  RESOURCE_TYPE_FMU_TOLK("Användning av tolk i försäkringsmedicinsk utredning"),
  RESOURCE_TYPE_FMU_ANTECKNING("Anteckning i försäkringsmedicinsk utredning"),

  RESOURCE_TYPE_BESTALLNING("Beställning");

  private final String resourceTypeName;

  ResourceType(String resourceTypeName) {
    this.resourceTypeName = resourceTypeName;
  }

  public String getResourceTypeName() {
    return resourceTypeName;
  }
}
