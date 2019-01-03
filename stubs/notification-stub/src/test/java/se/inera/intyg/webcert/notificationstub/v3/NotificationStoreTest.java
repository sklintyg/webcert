/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

import com.google.common.collect.Iterables;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.v3.Handelse;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class NotificationStoreTest {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationStoreTest.class);

    private static List<String> INTYG_IDS = Arrays.asList("intyg1", "intyg2", "intyg3", "intyg4", "intyg5", "intyg6", "intyg7", "intyg8",
            "intyg9",
            "intyg10");


    @Test
    public void testPurge() {

        NotificationStoreV3Impl notificationStore = new NotificationStoreV3Impl();
        notificationStore.initForTesting();
        LocalDateTime now = LocalDateTime.now();
        populateNotificationsMap(100, notificationStore, now);

        notificationStore.purge();
        assertEquals(80, notificationStore.getNotifications().size());

        LOG.info(notificationStore.getNotifications().stream().map(n -> n.getHandelse().getTidpunkt().toString()).sorted()
                .collect(Collectors.joining("\n")));
        LOG.info("Oldest should be: {}", now.minusMinutes(100).toString());
    }

    private void populateNotificationsMap(int nbr, NotificationStoreV3Impl notificationStore, LocalDateTime baseTime) {

        Iterator<String> intygsIdIter = Iterables.cycle(INTYG_IDS).iterator();

        for (int i = 0; i < nbr; i++) {

            String intygsId = intygsIdIter.next();

            CertificateStatusUpdateForCareType statusUpdate = new CertificateStatusUpdateForCareType();

            Handelse handelse = new Handelse();
            handelse.setTidpunkt(baseTime.minusMinutes(nbr - i));

            statusUpdate.setHandelse(handelse);
            Intyg intyg = new Intyg();
            IntygId intygId = new IntygId();
            intygId.setExtension(intygsId);
            intyg.setIntygsId(intygId);
            statusUpdate.setIntyg(intyg);

            notificationStore.put(statusUpdate);
        }
    }

}
