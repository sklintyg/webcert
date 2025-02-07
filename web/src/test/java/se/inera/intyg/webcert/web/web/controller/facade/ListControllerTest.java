/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.facade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.service.facade.list.ListDraftsFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.facade.list.ListSignedCertificatesFacadeServiceImpl;
import se.inera.intyg.webcert.web.service.facade.list.dto.CertificateListItem;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListInfo;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ListResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.list.ListRequestDTO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class ListControllerTest {

    @Mock
    private ListDraftsFacadeServiceImpl listDraftsFacadeService;
    @Mock
    private ListSignedCertificatesFacadeServiceImpl listSignedCertificatesFacadeService;
    @InjectMocks
    private ListController listController;

    @Nested
    class ListDrafts {

        ListInfo listInfo = new ListInfo();
        final List<CertificateListItem> list = List.of(new CertificateListItem());

        @BeforeEach
        void setup() {
            listInfo.setList(list);
            listInfo.setTotalCount(1);
            doReturn(listInfo)
                .when(listDraftsFacadeService)
                .get(any());
        }

        @Test
        void shallIncludeListInResponse() {
            final var request = new ListRequestDTO();
            request.setFilter(new ListFilter());
            final var response = (ListResponseDTO) listController.getListOfDrafts(request).getEntity();
            assertEquals(list, response.getList());
        }

        @Test
        void shallIncludeTotalCountInResponse() {
            final var request = new ListRequestDTO();
            request.setFilter(new ListFilter());
            final var response = (ListResponseDTO) listController.getListOfDrafts(request).getEntity();
            assertEquals(1, response.getTotalCount());
        }
    }

    @Nested
    class ListSignedCertificates {

        ListInfo listInfo = new ListInfo();
        final List<CertificateListItem> list = List.of(new CertificateListItem());

        @BeforeEach
        void setup() {
            listInfo.setList(list);
            listInfo.setTotalCount(1);
            doReturn(listInfo)
                .when(listSignedCertificatesFacadeService)
                .get(any());
        }

        @Test
        void shallIncludeListInResponse() {
            final var request = new ListRequestDTO();
            request.setFilter(new ListFilter());
            final var response = (ListResponseDTO) listController.getListOfSignedCertificates(request).getEntity();
            assertEquals(list, response.getList());
        }

        @Test
        void shallIncludeTotalCountInResponse() {
            final var request = new ListRequestDTO();
            request.setFilter(new ListFilter());
            final var response = (ListResponseDTO) listController.getListOfSignedCertificates(request).getEntity();
            assertEquals(1, response.getTotalCount());
        }
    }
}
