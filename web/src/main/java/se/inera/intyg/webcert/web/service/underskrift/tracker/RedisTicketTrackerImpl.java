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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;

import javax.annotation.PostConstruct;

@Service
@DependsOn("cacheManager")
public class RedisTicketTrackerImpl implements RedisTicketTracker {

    private static final String SIGNATURE_CACHE = "webcert.signature.ticket";

    @Autowired
    private CacheManager cacheManager;

    private Cache cache;

    @PostConstruct
    public void init() {
        cache = getCache();
    }


    @Override
    public void trackBiljett(SignaturBiljett signaturBiljett) {
        cache.put(signaturBiljett.getTicketId(), signaturBiljett);
    }



    @Override
    public SignaturBiljett findBiljett(String ticketId) {
        return cache.get(ticketId, SignaturBiljett.class);
    }

    @Override
    public SignaturBiljett updateStatus(String ticketId, SignaturStatus status) {
        SignaturBiljett sb = cache.get(ticketId, SignaturBiljett.class);
        sb.setStatus(status);
        cache.put(ticketId, sb);
        return sb;
    }

    private Cache getCache() {
        if (cacheManager == null) {
            throw new IllegalStateException("No CacheManager (Redis) injected into RedisTicketTrackerImpl.");
        }
        Cache cache = cacheManager.getCache(SIGNATURE_CACHE);
        if (cache == null) {
            throw new IllegalStateException("CacheManager (Redis) contains no cache " + SIGNATURE_CACHE);
        }
        return cache;
    }
}
