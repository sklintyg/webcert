/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.converter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.Test;
import se.inera.intyg.webcert.persistence.model.Filter;
import se.inera.intyg.webcert.persistence.model.VantarPa;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarParameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FilterConverterTest {

    @Test
    public void testConvert() {
        final LocalDateTime changedFrom = LocalDateTime.now();
        final LocalDateTime changedTo = LocalDateTime.now().plusDays(1);
        final String enhetId = "enhetId";
        final String otherEnhetId = "otherId";
        final String otherEnhetId2 = "otherId2";
        final String hsaId = "hsaId";
        final Integer pageSize = 5;
        final Boolean questionFromFK = Boolean.TRUE;
        final Boolean questionFromWC = Boolean.FALSE;
        final LocalDate replyLatest = LocalDate.now();
        final Integer startFrom = 0;
        final String vantarPa = "SVAR_FRAN_FK";
        final Boolean vidarebefordrad = Boolean.TRUE;
        QueryFragaSvarParameter source = createQueryFragaSvarParameter(changedFrom, changedTo, enhetId, hsaId, pageSize, questionFromFK,
                questionFromWC, replyLatest, startFrom, vantarPa, vidarebefordrad);
        List<String> unitIds = Arrays.asList(otherEnhetId, otherEnhetId2);

        Filter result = FilterConverter.convert(source, unitIds, Stream.of("fk7263").collect(Collectors.toSet()));

        assertEquals(changedFrom, result.getChangedFrom());
        assertTrue(changedTo.isBefore(result.getChangedTo()));
        assertEquals(2, result.getEnhetsIds().size());
        assertEquals(otherEnhetId, result.getEnhetsIds().get(0));
        assertEquals(otherEnhetId2, result.getEnhetsIds().get(1));
        assertEquals(hsaId, result.getHsaId());
        assertEquals(pageSize, result.getPageSize());
        assertTrue(result.isQuestionFromFK());
        assertFalse(result.isQuestionFromWC());
        assertEquals(replyLatest, result.getReplyLatest());
        assertEquals(startFrom, result.getStartFrom());
        assertEquals(VantarPa.SVAR_FRAN_FK, result.getVantarPa());
        assertTrue(result.getVidarebefordrad());
    }

    @Test
    public void testNullSafeBoolean() {
        QueryFragaSvarParameter source = createQueryFragaSvarParameter(LocalDateTime.now(), LocalDateTime.now(), "enhetId", "hsaId", 5, null,
                null, LocalDate.now(), 0, "KOMPLETTERING_FRAN_VARDEN", Boolean.TRUE);

        Filter result = FilterConverter.convert(source, new ArrayList<>(), Stream.of("fk7263").collect(Collectors.toSet()));
        assertFalse(result.isQuestionFromFK());
        assertFalse(result.isQuestionFromWC());
    }

    @Test
    public void testNoPageSizeNorStartFrom() {
        QueryFragaSvarParameter source = createQueryFragaSvarParameter(LocalDateTime.now(), LocalDateTime.now(), "enhetId", "hsaId", null, Boolean.FALSE,
                Boolean.TRUE, LocalDate.now(), null, "KOMPLETTERING_FRAN_VARDEN", Boolean.TRUE);

        Filter result = FilterConverter.convert(source, new ArrayList<>(), Stream.of("fk7263").collect(Collectors.toSet()));

        assertEquals(Integer.valueOf(0), result.getStartFrom());
        assertEquals(FilterConverter.DEFAULT_PAGE_SIZE, result.getPageSize());
    }

    private QueryFragaSvarParameter createQueryFragaSvarParameter(LocalDateTime changedFrom, LocalDateTime changedTo, String enhetId, String hsaId,
            Integer pageSize, Boolean questionFromFK, Boolean questionFromWC, LocalDate replyLatest, Integer startFrom, String vantarPa,
            Boolean vidarebefordrad) {
        QueryFragaSvarParameter res = new QueryFragaSvarParameter();
        res.setChangedFrom(changedFrom);
        res.setChangedTo(changedTo);
        res.setEnhetId(enhetId);
        res.setHsaId(hsaId);
        res.setPageSize(pageSize);
        res.setQuestionFromFK(questionFromFK);
        res.setQuestionFromWC(questionFromWC);
        res.setReplyLatest(replyLatest);
        res.setStartFrom(startFrom);
        res.setVantarPa(vantarPa);
        res.setVidarebefordrad(vidarebefordrad);
        return res;
    }
}
