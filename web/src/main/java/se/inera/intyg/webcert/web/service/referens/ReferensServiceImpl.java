/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.referens;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.persistence.referens.model.Referens;
import se.inera.intyg.webcert.persistence.referens.repository.ReferensRepository;

@Service
public class ReferensServiceImpl implements ReferensService {

    @Autowired
    ReferensRepository repo;

    @Override
    public void saveReferens(String intygsId, String referens) {
        Referens ref = new Referens();
        ref.setIntygsId(intygsId);
        ref.setReferens(referens);

        if (repo.findByIntygId(intygsId) == null) {
            repo.save(ref);
        }
    }

    @Override
    public String getReferensForIntygsId(String intygsId) {
        Referens referens = repo.findByIntygId(intygsId);
        return referens != null ? referens.getReferens() : null;
    }

    @Override
    public boolean referensExists(String intygsId) {
        return repo.findByIntygId(intygsId) != null;
    }
}
