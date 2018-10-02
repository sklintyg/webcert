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
package se.inera.intyg.webcert.notificationstub.v3;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.support.collections.DefaultRedisMap;
import se.inera.intyg.webcert.notificationstub.store.BaseStore;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;

public class NotificationStoreV3Impl extends BaseStore<CertificateStatusUpdateForCareType> implements NotificationStoreV3 {

    private static final String NOTIFICATION_STORE_V3 = "NOTIFICATION_STORE_V3";

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    private StringRedisTemplate stringRedisTemplate;

    @PostConstruct
    public void init() {
        stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(redisConnectionFactory);
        stringRedisTemplate.afterPropertiesSet();
        notificationsMap = new DefaultRedisMap<String, String>(NOTIFICATION_STORE_V3, stringRedisTemplate);
    }

    void initForTesting() {
        notificationsMap = new HashMap<>();
    }

//    @PreDestroy
//    public void close() {
//        if (this.notificationsMap != null) {
//            this.notificationsMap.close();
//        }
//    }

    @Override
    protected LocalDateTime getTidpunkt(Pair<String, CertificateStatusUpdateForCareType> left) {
        return left.getValue().getHandelse().getTidpunkt();
    }

    @Override
    public void purge() {
        super.purge();
    }

    @Override
    protected CertificateStatusUpdateForCareType transform(String s) {
        try {
            return objectMapper.readValue(s, CertificateStatusUpdateForCareType.class);
        } catch (IOException e) {
            return null;
        }
    }
}
