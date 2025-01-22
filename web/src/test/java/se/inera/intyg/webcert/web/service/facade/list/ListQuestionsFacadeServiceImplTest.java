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

package se.inera.intyg.webcert.web.service.facade.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.webcert.web.csintegration.certificate.ListCertificateQuestionsFromCS;
import se.inera.intyg.webcert.web.service.access.CertificateAccessServiceHelper;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.facade.list.dto.CertificateListItem;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;
import se.inera.intyg.webcert.web.service.facade.list.filter.QuestionFilterConverter;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarParameter;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarResponse;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;

@ExtendWith(MockitoExtension.class)
class ListQuestionsFacadeServiceImplTest {

    private static final ListFilter LIST_FILTER = new ListFilter();
    private static final QueryFragaSvarParameter QUERY_FRAGA_SVAR_PARAMETER = new QueryFragaSvarParameter();
    private static final QueryFragaSvarResponse QUERY_FRAGA_SVAR_RESPONSE_WC = new QueryFragaSvarResponse();
    private static final QueryFragaSvarResponse QUERY_FRAGA_SVAR_RESPONSE_CS = new QueryFragaSvarResponse();
    private static final WebCertUser WEBCERT_USER = new WebCertUser();
    private static final ArendeListItem ARENDE_LIST_ITEM_WC = new ArendeListItem();
    private static final ArendeListItem ARENDE_LIST_ITEM_CS = new ArendeListItem();
    private static final List<ArendeListItem> ARENDE_LIST_WC = List.of(ARENDE_LIST_ITEM_WC);
    private static final List<ArendeListItem> ARENDE_LIST_CS = List.of(ARENDE_LIST_ITEM_CS);
    private static final CertificateListItem CERTIFICATE_LIST_ITEM = new CertificateListItem();
    private static final String PATIENT_ID = "191212121212";
    private static final String ID = "ID";
    private static final ArendeListItem PAGINATED_ITEM_1 = new ArendeListItem();
    private static final ArendeListItem PAGINATED_ITEM_2 = new ArendeListItem();
    private static final List<ArendeListItem> PAGINATED_LIST = List.of(PAGINATED_ITEM_1, PAGINATED_ITEM_2);

    @InjectMocks
    ListQuestionsFacadeServiceImpl listQuestionsFacadeServiceImpl;
    @Mock
    QuestionFilterConverter questionFilterConverter;
    @Mock
    CertificateListItemConverter certificateListItemConverter;
    @Mock
    ArendeService arendeService;
    @Mock
    WebCertUserService webCertUserService;
    @Mock
    CertificateAccessServiceHelper certificateAccessServiceHelper;
    @Mock
    PaginationAndLoggingService paginationAndLoggingService;
    @Mock
    ListCertificateQuestionsFromCS listCertificateQuestionsFromCS;


    @Test
    void shouldReturnListInfo() {
        final var expectedConvertedList = List.of(CERTIFICATE_LIST_ITEM, CERTIFICATE_LIST_ITEM);

        QUERY_FRAGA_SVAR_PARAMETER.setPatientPersonId(PATIENT_ID);
        final var valdVardenhet = new Vardenhet();
        valdVardenhet.setId(ID);
        final var valdVardgivare = new Vardgivare();
        valdVardgivare.setId(ID);
        WEBCERT_USER.setValdVardenhet(valdVardenhet);
        WEBCERT_USER.setValdVardgivare(valdVardgivare);

        ARENDE_LIST_ITEM_WC.setPatientId(PATIENT_ID);
        QUERY_FRAGA_SVAR_RESPONSE_WC.setResults(ARENDE_LIST_WC);
        QUERY_FRAGA_SVAR_RESPONSE_WC.setTotalCount(1);
        QUERY_FRAGA_SVAR_RESPONSE_CS.setResults(ARENDE_LIST_CS);
        QUERY_FRAGA_SVAR_RESPONSE_CS.setTotalCount(1);

        when(questionFilterConverter.convert(LIST_FILTER))
            .thenReturn(QUERY_FRAGA_SVAR_PARAMETER);

        when(arendeService.filterArende(QUERY_FRAGA_SVAR_PARAMETER, true))
            .thenReturn(QUERY_FRAGA_SVAR_RESPONSE_WC);

        when(listCertificateQuestionsFromCS.list(QUERY_FRAGA_SVAR_PARAMETER))
            .thenReturn(QUERY_FRAGA_SVAR_RESPONSE_CS);

        when(webCertUserService.getUser())
            .thenReturn(WEBCERT_USER);

        when(paginationAndLoggingService.get(QUERY_FRAGA_SVAR_PARAMETER, List.of(ARENDE_LIST_ITEM_WC, ARENDE_LIST_ITEM_CS), WEBCERT_USER))
            .thenReturn(PAGINATED_LIST);

        when(certificateListItemConverter.convert(PAGINATED_ITEM_1))
            .thenReturn(CERTIFICATE_LIST_ITEM);

        when(certificateListItemConverter.convert(PAGINATED_ITEM_2))
            .thenReturn(CERTIFICATE_LIST_ITEM);

        final var result = listQuestionsFacadeServiceImpl.get(LIST_FILTER);

        assertEquals(expectedConvertedList, result.getList());
        assertEquals(expectedConvertedList.size(), result.getTotalCount());
    }
}
