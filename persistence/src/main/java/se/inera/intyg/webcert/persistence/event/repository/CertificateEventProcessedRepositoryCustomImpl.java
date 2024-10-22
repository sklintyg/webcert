/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.persistence.event.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Collectors;

public class CertificateEventProcessedRepositoryCustomImpl implements CertificateEventProcessedRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void saveBatch(List<String> idList) {
        var inserts = idList.stream().map(id -> "('" + id + "')").collect(Collectors.joining(","));
        entityManager.createNativeQuery("INSERT INTO CERTIFICATE_EVENT_PROCESSED VALUES " + inserts + ";").executeUpdate();
    }
}
