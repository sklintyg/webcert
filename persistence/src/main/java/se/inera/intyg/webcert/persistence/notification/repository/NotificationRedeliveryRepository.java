/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.persistence.notification.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;

public interface NotificationRedeliveryRepository extends JpaRepository<NotificationRedelivery, String> {

    Optional<NotificationRedelivery> findByCorrelationId(String correlationId);

    List<NotificationRedelivery> findByCorrelationIdNull();

    Optional<NotificationRedelivery> findByEventId(Long handelseId);

    List<NotificationRedelivery> findByRedeliveryTimeLessThan(LocalDateTime currentTime);

    List<NotificationRedelivery> findByRedeliveryTime(LocalDateTime currentTime);

    List<NotificationRedelivery> findByAttemptedDeliveries(Integer attemptedDeliveries);
}
