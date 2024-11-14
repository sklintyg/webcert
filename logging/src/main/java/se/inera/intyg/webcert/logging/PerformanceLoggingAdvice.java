/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.logging;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class PerformanceLoggingAdvice {
    @Around("@annotation(performanceLogging)")
    public Object logPerformance(ProceedingJoinPoint joinPoint, PerformanceLogging performanceLogging) throws Throwable {

        if (performanceLogging.isActive()) {
            final var start = LocalDateTime.now();
            var success = true;
            try {
                return joinPoint.proceed();

            } catch (final Throwable throwable) {
                success = false;
                throw throwable;

            } finally {
                final var end = LocalDateTime.now();
                final var duration = Duration.between(start, end).toMillis();
                final var className = joinPoint.getSignature().getDeclaringTypeName();
                final var methodName = joinPoint.getSignature().getName();
                try (final var mdcLogConstants =
                    MdcCloseableMap.builder()
                        .put(MdcLogConstants.EVENT_START, start.toString())
                        .put(MdcLogConstants.EVENT_END, end.toString())
                        .put(MdcLogConstants.EVENT_DURATION, Long.toString(duration))
                        .put(MdcLogConstants.EVENT_ACTION, performanceLogging.eventAction())
                        .put(MdcLogConstants.EVENT_TYPE, performanceLogging.eventType())
                        .put(MdcLogConstants.EVENT_CATEGORY, performanceLogging.eventCategory())
                        .put(MdcLogConstants.EVENT_CLASS, className)
                        .put(MdcLogConstants.EVENT_METHOD, methodName)
                        .put(
                            MdcLogConstants.EVENT_OUTCOME, success ? MdcLogConstants.EVENT_OUTCOME_SUCCESS
                                : MdcLogConstants.EVENT_OUTCOME_FAILURE
                        )
                        .build()
                ) {
                    log.info("Class: {} Method: {} Duration: {} ms",
                        className,
                        methodName,
                        duration
                    );
                }
            }
        }
        return joinPoint.proceed();
    }
}
