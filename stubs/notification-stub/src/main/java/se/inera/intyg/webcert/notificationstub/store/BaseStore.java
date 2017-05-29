package se.inera.intyg.webcert.notificationstub.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.openhft.chronicle.map.ChronicleMap;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by eriklupander on 2017-05-29.
 */
public abstract class BaseStore<T> {

    private static final Logger LOG = LoggerFactory.getLogger(BaseStore.class);


    private static final double LOAD = 0.8;

    protected static final int AVERAGE_VALUE_SIZE = 1024;
    protected static final String AVERAGE_KEY = "963d957f-6662-4cd3-bb99-d3d9fb419b51";


    protected ChronicleMap<String, String> notificationsMap;
    protected CustomObjectMapper objectMapper = new CustomObjectMapper();

    private final int maxSize;
    protected final int minSize;

    protected BaseStore(int maxSize) {
        this.maxSize = maxSize;
        this.minSize = Double.valueOf(maxSize * LOAD).intValue();
    }

    public void put(T request) {
        try {
            notificationsMap.put(UUID.randomUUID().toString(), objectMapper.writeValueAsString(request));
            if (notificationsMap.size() >= maxSize) {
                purge();
            }

        } catch (JsonProcessingException e) {
            LOG.error(e.getMessage());
        }
    }


    protected void purge() {

        LOG.debug("NotificationStoreV3 contains {} notifications, pruning old ones...", notificationsMap.size());

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
