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
package se.inera.intyg.webcert.notificationstub.v3;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.openhft.chronicle.map.ChronicleMap;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class NotificationStoreV3Impl implements NotificationStoreV3 {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationStoreV3Impl.class);

    private static final double LOAD = 0.8;
    private static final String WEBCERT_STUB_DATA_FOLDER = "webcert.stub.data.folder";
    private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
    public static final int AVERAGE_VALUE_SIZE = 1024;

    private final int maxSize;
    private final int minSize;

    private ChronicleMap<String, String> notificationsMap;

    private CustomObjectMapper objectMapper = new CustomObjectMapper();

    public NotificationStoreV3Impl(int maxSize) {
        this.maxSize = maxSize;
        this.minSize = Double.valueOf(maxSize * LOAD).intValue();
        String notificationStubFile = getStubDataFile();

        LOG.info("Created disk-persistent ChronicleMap for notificationstub at {} with minsize {}.", notificationStubFile, minSize);

        try {
            notificationsMap = ChronicleMap
                    .of(String.class, String.class)
                    .name("notificationsv3")
                    .averageKey("963d957f-6662-4cd3-bb99-d3d9fb419b51")
                    .averageValueSize(AVERAGE_VALUE_SIZE)
                    .entries(minSize)
                    .createOrRecoverPersistedTo(new File(notificationStubFile));
            LOG.info("Successfully created disk-persistent ChronicleMap for notificationstub at {}", notificationStubFile);
        } catch (IOException e) {
            LOG.error("Could not create persistent notifications store: " + e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    private String getStubDataFile() {
        if (System.getProperty(WEBCERT_STUB_DATA_FOLDER) != null) {
            return System.getProperty(WEBCERT_STUB_DATA_FOLDER) + File.separator + "notificationstubv3.data";
        } else if (System.getProperty(JAVA_IO_TMPDIR) != null) {
            return System.getProperty(JAVA_IO_TMPDIR) + File.separator + "notificationstubv3.data";
        } else {
            throw new IllegalStateException("Error booting stub - cannot determine stub data folder from system properties.");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see se.inera.intyg.webcert.notificationstub.NotificationStoreV3#put(java.lang.String,
     * se.inera.certificate.clinicalprocess
     * .healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType)
     */
    @Override
    public void put(String utlatandeId, CertificateStatusUpdateForCareType request) {
        try {
            notificationsMap.put(UUID.randomUUID().toString(), objectMapper.writeValueAsString(request));
            if (notificationsMap.size() >= maxSize) {
                purge(notificationsMap);
            }

        } catch (JsonProcessingException e) {
            LOG.error(e.getMessage());
        }
    }

    public int size() {
        return this.notificationsMap.size();
    }

    private void purge(ChronicleMap<String, String> notificationsMap) {

        LOG.debug("NotificationStoreV3 contains {} notifications, pruning old ones...", notificationsMap.size());

        List<Pair<String, CertificateStatusUpdateForCareType>> values = notificationsMap.entrySet().stream().map(entry -> {
            try {
                return Pair.of(entry.getKey(), objectMapper.readValue(entry.getValue(), CertificateStatusUpdateForCareType.class));
            } catch (IOException e) {
                return null;
            }
        })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(left -> left.getRight().getHandelse().getTidpunkt()))
                .collect(Collectors.toList());
        Iterator<Pair<String, CertificateStatusUpdateForCareType>> iter = values.iterator();

        // trim map down to minSize
        while (notificationsMap.size() > minSize) {
            Pair<String, CertificateStatusUpdateForCareType> objToRemove = iter.next();
            notificationsMap.remove(objToRemove.getKey(), objToRemove.getValue());
        }

        LOG.debug("Pruning done! NotificationStoreV3 now contains {} notifications", notificationsMap.size());
    }

    /*
     * (non-Javadoc)
     *
     * @see se.inera.intyg.webcert.notificationstub.NotificationStoreV3#getNotifications()
     */
    @Override
    public Collection<CertificateStatusUpdateForCareType> getNotifications() {
        return notificationsMap.values().stream().map(s -> {
            try {
                return objectMapper.readValue(s, CertificateStatusUpdateForCareType.class);
            } catch (IOException e) {
                return null;
            }
        })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /*
     * (non-Javadoc)
     *
     * @see se.inera.intyg.webcert.notificationstub.NotificationStoreV3#clear()
     */
    @Override
    public void clear() {
        notificationsMap.clear();
    }
}
