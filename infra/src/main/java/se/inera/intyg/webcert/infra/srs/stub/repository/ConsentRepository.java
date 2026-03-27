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
package se.inera.intyg.webcert.infra.srs.stub.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.infra.srs.stub.repository.model.Consent;
import se.inera.intyg.webcert.infra.srs.stub.repository.model.Individ;

public class ConsentRepository {

  private ConcurrentHashMap<Individ, Consent> consentRepo = new ConcurrentHashMap<>();

  public Optional<Consent> getConsent(Personnummer personnummer, String vardgivarId) {
    return Optional.ofNullable(consentRepo.get(new Individ(personnummer, vardgivarId)));
  }

  public void setConsent(Personnummer personnummer, String vardgivarId, boolean consent) {
    Individ individ = new Individ(personnummer, vardgivarId);
    if (consent) {
      consentRepo.put(individ, new Consent(true, LocalDateTime.now()));
    } else {
      consentRepo.remove(individ);
    }
  }

  public void clear() {
    consentRepo.clear();
  }
}
