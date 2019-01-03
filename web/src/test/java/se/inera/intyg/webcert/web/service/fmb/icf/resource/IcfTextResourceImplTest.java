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
package se.inera.intyg.webcert.web.service.fmb.icf.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.core.io.ResourceLoader.CLASSPATH_URL_PREFIX;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Optional;
import se.inera.intyg.webcert.web.web.controller.api.dto.icf.IcfKod;

@RunWith(MockitoJUnitRunner.class)
public class IcfTextResourceImplTest {

    private static final String FILE_PATH = "IcfTextResourceImplTest/klassifikationer-koder-for-funktionstillstand-icf-2018.2.xls";

    @InjectMocks
    private IcfTextResourceImpl icfTextResource;

    @Before
    public void setup() {
        final ResourceLoader loader = new FileSystemResourceLoader();
        final Resource testResource = loader.getResource(CLASSPATH_URL_PREFIX + FILE_PATH);
        ReflectionTestUtils.setField(icfTextResource, "resource", testResource);
    }

    @Test
    public void testReadFromFileAndLookupMatchingKod() {

        final String matchingCode = "b114"; //den här koden finns i testfilen

        final String expectedBenamning = "Orientering";
        final String expectedBeskrivning = "Allmänna psykiska funktioner av att känna till och fastställa sin relation till tid, rum, sig själv och andra, till föremål och närmaste omgivning";
        final String expectedInnefattar = "funktioner av orientering till tid, rum och person; orientering till sig själv och andra; desorientering till tid, rum och person";

        icfTextResource.init();

        final Optional<IcfKod> icfKod = icfTextResource.lookupTextByIcfKod(matchingCode);

        assertThat(icfKod).isPresent();
        assertThat(icfKod.get().getKod()).isEqualTo(matchingCode);
        assertThat(icfKod.get().getBenamning()).isEqualTo(expectedBenamning);
        assertThat(icfKod.get().getBeskrivning()).isEqualTo(expectedBeskrivning);
        assertThat(icfKod.get().getInnefattar()).isEqualTo(expectedInnefattar);
    }

    @Test
    public void testReadFromFileAndLookupNonMatchingKod() {

        final String nonMatchingCode = "helt-fel-kod"; //felaktig kod ska generera ett Optional.empty()

        icfTextResource.init();
        final Optional<IcfKod> icfKod = icfTextResource.lookupTextByIcfKod(nonMatchingCode);

        assertThat(icfKod).isNotPresent();
    }

    @Test
    public void testFailReadFromFileAndLookupKod() {

        final String kod = "en-kod";

        ReflectionTestUtils.setField(icfTextResource, "resource", null); //ser till att fil inte läses in

        icfTextResource.init(); //ska inte slänga exception utan endast resultera i en tom lista av koder
        final Optional<IcfKod> icfKod = icfTextResource.lookupTextByIcfKod(kod);

        assertThat(icfKod).isNotPresent();
    }
}
