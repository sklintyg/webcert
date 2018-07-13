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
package se.inera.intyg.webcert.web.service.underskrift.tracker;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;

/**
 * Stores {@link SignaturBiljett} instances in Redis, using {@link RedisTicketTrackerImpl#SIGNATURE_CACHE} + ticketId
 * as key.
 *
 * Uses the RedisTemplate directly to allow fine-grained control over entry expiry.
 *
 * @author eriklupander
 */
@Service
@DependsOn("rediscache")
public class RedisTicketTrackerImpl implements RedisTicketTracker {

    private static final String SIGNATURE_CACHE = "webcert.signature.ticket";
    private static final long TICKET_EXPIRY_MINUTES = 15L;

    @Autowired
    @Qualifier("rediscache")
    private RedisTemplate<Object, Object> redisTemplate;

    // inject the template as ValueOperations
    @Resource(name = "rediscache")
    private ValueOperations<String, SignaturBiljett> valueOps;

    @Override
    public void trackBiljett(SignaturBiljett signaturBiljett) {
        valueOps.set(buildKey(signaturBiljett.getTicketId()), signaturBiljett, TICKET_EXPIRY_MINUTES,
                TimeUnit.MINUTES);
    }

    @Override
    public SignaturBiljett findBiljett(String ticketId) {
        return valueOps.get(buildKey(ticketId));
    }

    @Override
    public SignaturBiljett updateStatus(String ticketId, SignaturStatus status) {
        SignaturBiljett sb = valueOps.get(buildKey(ticketId));
        sb.setStatus(status);
        valueOps.set(buildKey(ticketId), sb);
        return sb;
    }

    private String buildKey(String ticketId) {
        return SIGNATURE_CACHE + ticketId;
    }
}
