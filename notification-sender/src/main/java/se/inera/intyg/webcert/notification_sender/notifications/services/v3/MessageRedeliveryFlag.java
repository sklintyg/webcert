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
package se.inera.intyg.webcert.notification_sender.notifications.services.v3;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

/**
 * Keeps track of status flag for failed messages (redelivery).
 */
@Component
public class MessageRedeliveryFlag {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // TTL in minutes
    @Value("${notificationSender.redeliveryflagTTL:120}")
    private int redeliveryflagTTL;

    // state for flag
    static class StatusFlag {
        private long successTimestamp;

        public long getSuccessTimestamp() {
            return successTimestamp;
        }

        public boolean isOutdated(long timestamp) {
            return timestamp < successTimestamp;
        }

        void raised() {
            this.successTimestamp = 0L;
        }

        void lowered(final long timestamp) {
            this.successTimestamp = timestamp;
        }
    }

    /**
     * Returns if a timestamp (message) is outdated and shall be ignored.
     *
     * @param key the message key.
     * @param timestamp the message timestamp.
     * @return true if the message is outdated, otherwise false.
     */
    public boolean isOutdated(final String key, final long timestamp) {
        final ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        final StatusFlag statusFlag = unmarshal(ops.get(key));
        return Objects.isNull(statusFlag) ? false : statusFlag.isOutdated(timestamp);
    }

    /**
     * Raise error flag.
     *
     * @param key the key.
     */
    public void raiseError(final String key) {
        final ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        StatusFlag statusFlag = unmarshal(ops.get(key));
        if (Objects.isNull(statusFlag)) {
            statusFlag = new StatusFlag();
        }
        statusFlag.raised();
        ops.set(key, marshal(statusFlag), redeliveryflagTTL, TimeUnit.MINUTES);
    }

    /**
     * Lower error flag.
     *
     * @param key the key.
     * @param messageTimestamp
     */
    public void lowerError(final String key, long messageTimestamp) {
        final ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        final StatusFlag statusFlag = unmarshal(ops.get(key));
        if (Objects.nonNull(statusFlag)) {
            statusFlag.lowered(messageTimestamp);
            ops.set(key, marshal(statusFlag), redeliveryflagTTL, TimeUnit.MINUTES);
        }
    }

    String marshal(final StatusFlag statusFlag) {
        return Objects.isNull(statusFlag) ? null : uncheck(() -> objectMapper.writeValueAsString(statusFlag));
    }

    StatusFlag unmarshal(final String s) {
        return Objects.isNull(s) ? null : uncheck(() -> objectMapper.readValue(s, StatusFlag.class));
    }

    static final <T> T uncheck(final Callable<T> c) {
        try {
            return c.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
