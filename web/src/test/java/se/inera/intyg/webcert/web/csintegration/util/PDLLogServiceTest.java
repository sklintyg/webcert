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

package se.inera.intyg.webcert.web.csintegration.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.log.factory.LogRequestFactory;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@ExtendWith(MockitoExtension.class)
class PDLLogServiceTest {

    private static final LogRequest REQUEST = new LogRequest();
    private static final WebCertUser USER = new WebCertUser();
    private static final String PATIENT_ID = "191212121212";

    @Mock
    LogService logService;

    @Mock
    LogRequestFactory logRequestFactory;

    @Mock
    WebCertUserService webCertUserService;

    @InjectMocks
    PDLLogService pdlLogService;

    @BeforeEach
    void setup() {
        when(webCertUserService.getUser())
            .thenReturn(USER);

        when(logRequestFactory.createLogRequestFromUser(any(WebCertUser.class), anyString()))
            .thenReturn(REQUEST);
    }

    @Nested
    class Create {

        @Test
        void shouldCreateRequestUsingUser() {
            final var captor = ArgumentCaptor.forClass(WebCertUser.class);

            pdlLogService.logCreated(PATIENT_ID);

            verify(logRequestFactory).createLogRequestFromUser(captor.capture(), anyString());
            assertEquals(USER, captor.getValue());
        }

        @Test
        void shouldCreateRequestUsingPatientId() {
            final var captor = ArgumentCaptor.forClass(String.class);

            pdlLogService.logCreated(PATIENT_ID);

            verify(logRequestFactory).createLogRequestFromUser(any(WebCertUser.class), captor.capture());
            assertEquals(PATIENT_ID, captor.getValue());
        }


        @Test
        void shouldLogUsingRequest() {
            final var captor = ArgumentCaptor.forClass(LogRequest.class);

            pdlLogService.logCreated(PATIENT_ID);

            verify(logService).logCreateIntyg(captor.capture());
            assertEquals(REQUEST, captor.getValue());
        }
    }

    @Nested
    class Read {

        @Test
        void shouldCreateRequestUsingUser() {
            final var captor = ArgumentCaptor.forClass(WebCertUser.class);

            pdlLogService.logRead(PATIENT_ID);

            verify(logRequestFactory).createLogRequestFromUser(captor.capture(), anyString());
            assertEquals(USER, captor.getValue());
        }

        @Test
        void shouldCreateRequestUsingPatientId() {
            final var captor = ArgumentCaptor.forClass(String.class);

            pdlLogService.logRead(PATIENT_ID);

            verify(logRequestFactory).createLogRequestFromUser(any(WebCertUser.class), captor.capture());
            assertEquals(PATIENT_ID, captor.getValue());
        }


        @Test
        void shouldLogUsingRequest() {
            final var captor = ArgumentCaptor.forClass(LogRequest.class);

            pdlLogService.logRead(PATIENT_ID);

            verify(logService).logReadIntyg(captor.capture());
            assertEquals(REQUEST, captor.getValue());
        }
    }

}