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
package se.inera.intyg.webcert.notificationstub.v3;

import org.apache.commons.lang3.tuple.Pair;
import se.inera.intyg.webcert.notificationstub.store.BaseStore;
import se.inera.intyg.webcert.notificationstub.store.StoreFactory;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.time.LocalDateTime;

public class NotificationStoreV3Impl extends BaseStore<CertificateStatusUpdateForCareType> implements NotificationStoreV3 {

    public NotificationStoreV3Impl(String cacheName, int maxSize) {
        super(maxSize);
        this.notificationsMap = StoreFactory.getChronicleMap(cacheName, minSize, AVERAGE_VALUE_SIZE, AVERAGE_KEY);
    }

    @PreDestroy
    public void close() {
        if (this.notificationsMap != null) {
            this.notificationsMap.close();
        }
    }

    @Override
    protected LocalDateTime getTidpunkt(Pair<String, CertificateStatusUpdateForCareType> left) {
        return left.getValue().getHandelse().getTidpunkt();
    }

    @Override
    public void purge() {
        super.purge();
    }

    @Override
    protected CertificateStatusUpdateForCareType transform(String s) {
        try {
            return objectMapper.readValue(s, CertificateStatusUpdateForCareType.class);
        } catch (IOException e) {
            return null;
        }
    }
}
