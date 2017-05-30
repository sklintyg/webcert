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
package se.inera.intyg.webcert.notificationstub;

import com.google.common.collect.Iterables;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.intyg.webcert.notificationstub.v1.NotificationStoreImpl;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.Handelse;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.UtlatandeType;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.UtlatandeId;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class NotificationStoreTest {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationStoreTest.class);

    private static List<String> INTYG_IDS = Arrays.asList("intyg1", "intyg2", "intyg3", "intyg4", "intyg5", "intyg6", "intyg7", "intyg8", "intyg9",
            "intyg10");

    @Before
    @After
    public void init() {
        File dataFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "notificationsv1.data");
        if (dataFile.exists()) {
            dataFile.delete();
        }
    }

    @Test
    public void testPurge() {

        NotificationStoreImpl notificationStore = new NotificationStoreImpl(UUID.randomUUID().toString(), 100);
        LocalDateTime now = LocalDateTime.now();
        populateNotificationsMap(100, notificationStore, now);

        notificationStore.purge();
        assertEquals(80, notificationStore.getNotifications().size());

        LOG.info(notificationStore.getNotifications().stream().map(n -> n.getUtlatande().getHandelse().getHandelsetidpunkt().toString()).sorted()
                .collect(Collectors.joining("\n")));
        LOG.info("Oldest should be: {}", now.minusMinutes(100).toString());
    }

    private void populateNotificationsMap(int nbr, NotificationStoreImpl notificationStore, LocalDateTime baseTime) {

        Iterator<String> intygsIdIter = Iterables.cycle(INTYG_IDS).iterator();

        for (int i = 0; i < nbr; i++) {

            String intygsId = intygsIdIter.next();

            Handelse handelse = new Handelse();
            handelse.setHandelsetidpunkt(baseTime.minusMinutes(nbr - i));

            UtlatandeType utl = new UtlatandeType();
            utl.setHandelse(handelse);
            utl.setUtlatandeId(new UtlatandeId());
            utl.getUtlatandeId().setExtension(intygsId);

            CertificateStatusUpdateForCareType type = new CertificateStatusUpdateForCareType();
            type.setUtlatande(utl);

            notificationStore.put(type);
        }
    }

}
