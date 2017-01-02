/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.notificationstub.v2;

import java.time.LocalDateTime;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.*;

import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v2.CertificateStatusUpdateForCareType;

public class NotificationStoreV2Impl implements NotificationStoreV2 {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationStoreV2Impl.class);

    private static final double LOAD = 0.8;

    private final int maxSize;

    private final int minSize;

    public NotificationStoreV2Impl(int maxSize) {
        this.maxSize = maxSize;
        this.minSize = Double.valueOf(maxSize * LOAD).intValue();
    }

    private Multimap<String, CertificateStatusUpdateForCareType> rawNotificationsMap = ArrayListMultimap.create();
    private Multimap<String, CertificateStatusUpdateForCareType> notificationsMap = Multimaps.synchronizedMultimap(rawNotificationsMap);

    /*
     * (non-Javadoc)
     *
     * @see se.inera.intyg.webcert.notificationstub.NotificationStoreV2#put(java.lang.String,
     * se.inera.certificate.clinicalprocess
     * .healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType)
     */
    @Override
    public void put(String utlatandeId, CertificateStatusUpdateForCareType request) {
        notificationsMap.put(utlatandeId, request);
        if (notificationsMap.size() >= maxSize) {
            purge(notificationsMap);
        }
    }

    public int size() {
        return this.notificationsMap.size();
    }

    public void purge(Multimap<String, CertificateStatusUpdateForCareType> notificationsMap) {

        LOG.debug("NotificationStoreV2 contains {} notifications, pruning old ones...", notificationsMap.size());

        // find the oldest ones
        Ordering<CertificateStatusUpdateForCareType> order = new Ordering<CertificateStatusUpdateForCareType>() {

            @Override
            public int compare(CertificateStatusUpdateForCareType left, CertificateStatusUpdateForCareType right) {
                LocalDateTime lDate = left.getHandelse().getTidpunkt();
                LocalDateTime rDate = right.getHandelse().getTidpunkt();

                return lDate.compareTo(rDate);
            }
        };

        // sort the list of entries in the map so that the oldest entries are first
        List<CertificateStatusUpdateForCareType> sortedEntries = order.sortedCopy(notificationsMap.values());

        Iterator<CertificateStatusUpdateForCareType> iter = sortedEntries.iterator();

        // trim map down to minSize
        while (notificationsMap.size() > minSize) {
            CertificateStatusUpdateForCareType objToRemove = iter.next();
            String intygsId = objToRemove.getIntyg().getIntygsId().getExtension();
            notificationsMap.remove(intygsId, objToRemove);
        }

        LOG.debug("Pruning done! NotificationStoreV2 now contains {} notifications", notificationsMap.size());
    }

    public int getMaxSize() {
        return maxSize;
    }

    public int getMinSize() {
        return minSize;
    }

    /*
     * (non-Javadoc)
     *
     * @see se.inera.intyg.webcert.notificationstub.NotificationStoreV2#getNotifications()
     */
    @Override
    public Collection<CertificateStatusUpdateForCareType> getNotifications() {
        return notificationsMap.values();
    }

    /*
     * (non-Javadoc)
     *
     * @see se.inera.intyg.webcert.notificationstub.NotificationStoreV2#clear()
     */
    @Override
    public void clear() {
        notificationsMap.clear();
    }
}
