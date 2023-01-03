/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.notificationstub.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;

/**
 * Created by eriklupander on 2017-05-29.
 */
public abstract class BaseStore<T> {

    private static final Logger LOG = LoggerFactory.getLogger(BaseStore.class);

    protected Map<String, String> notificationsMap;

    protected CustomObjectMapper objectMapper = new CustomObjectMapper();

    protected final int maxSize = 100;
    protected final int minSize = 80;

    public void put(T request) {
        try {
            notificationsMap.put(UUID.randomUUID().toString(), objectMapper.writeValueAsString(request));
            if (notificationsMap.values().size() >= maxSize) {
                purge();
            }

        } catch (JsonProcessingException e) {
            LOG.error(e.getMessage());
        }
    }

    protected void purge() {

        LOG.debug("NotificationStoreV3 contains {} notifications, pruning old ones...", notificationsMap.values().size());

        List<Pair<String, T>> values = notificationsMap.entrySet().stream()
            .map(entry -> Pair.of(entry.getKey(), transform(entry.getValue())))
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(this::getTidpunkt))
            .collect(Collectors.toList());
        Iterator<Pair<String, T>> iter = values.iterator();

        // trim map down to minSize
        while (notificationsMap.size() > minSize) {
            Pair<String, T> objToRemove = iter.next();
            notificationsMap.remove(objToRemove.getKey());
        }

        LOG.debug("Pruning done! NotificationStoreV3 now contains {} notifications", notificationsMap.size());
    }

    protected abstract LocalDateTime getTidpunkt(Pair<String, T> left);

    public Collection<T> getNotifications() {
        return notificationsMap.values().stream().map(this::<T>transform)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public int size() {
        return this.notificationsMap.size();
    }

    public int getMaxSize() {
        return maxSize;
    }

    public int getMinSize() {
        return minSize;
    }

    public void clear() {
        notificationsMap.clear();
    }

    protected abstract T transform(String s);

}
