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
package se.inera.intyg.webcert.notificationstub;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.inera.intyg.webcert.notificationstub.v3.NotificationStoreV3;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.v3.Handelse;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

import java.io.File;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * Created by eriklupander on 2017-05-29.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/notification-stub-test-context.xml")
@ActiveProfiles({"dev", "wc-hsa-stub"})
public class IntegrationTest {

    @Autowired
    private NotificationStoreV3 notificationStore;
    @Autowired
    private NotificationStoreV3 notificationStore2;

    @Before
    @After
    public void init() {
        File dataFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "notificationsv1.data");
        if (dataFile.exists()) {
            dataFile.delete();
        }
        notificationStore.clear();
    }

    @Test
    public void testPut() {
        notificationStore.put(buildNotificationV3());
        assertEquals(1, notificationStore.getNotifications().size());
    }

    @Test
    public void testSyncWorks() {
        notificationStore.put(buildNotificationV3());
        notificationStore2.put(buildNotificationV3());
        notificationStore2.put(buildNotificationV3());

        assertEquals(3, notificationStore2.getNotifications().size());
    }

    private CertificateStatusUpdateForCareType buildNotificationV3() {
        Handelse handelse = new Handelse();
        handelse.setTidpunkt(LocalDateTime.now().minusMinutes(1));

        Intyg intyg = new Intyg();
        IntygId intygId = new IntygId();
        intygId.setExtension(UUID.randomUUID().toString());
        intyg.setIntygsId(intygId);

        CertificateStatusUpdateForCareType type = new CertificateStatusUpdateForCareType();
        type.setIntyg(intyg);
        type.setHandelse(handelse);

        return type;
    }
}
