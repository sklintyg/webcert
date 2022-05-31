/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.persistence.referens.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import se.inera.intyg.webcert.persistence.referens.model.Referens;

public interface ReferensRepository extends CrudRepository<Referens, Long> {

    Referens findByIntygId(String intygsId);

    @Query("select r from Referens r where r.intygId in :certificateIds")
    List<Referens> getReferenserByIntygsIds(@Param("certificateIds") List<String> certificateIds);

    default int eraseReferenserByCertificateIds(List<String> certificateIds) {
        final var referensList = getReferenserByIntygsIds(certificateIds);
        deleteAll(referensList);
        return referensList.size();
    }
}
