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


import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import se.inera.intyg.webcert.web.web.controller.api.dto.icf.IcfKod;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("classpath:IcfTextResourceImplTest/test.properties")
@ContextConfiguration(classes = {IcfTextResourceImpl.class})
public class IcfTextResourceImplTest {

    @Autowired
    private IcfTextResource icfTextResource;

    @Test
    public void testReadFromFileAndLookupMatchingKod() {

        final String matchingCode = "b114"; //den här koden finns i testfilen

        final String expectedBenamning = "Orientering";
        final String expectedBeskrivning = "Allmänna psykiska funktioner av att känna till och fastställa sin relation till tid, rum, sig själv och andra, till föremål och närmaste omgivning";
        final String expectedInnefattar = "funktioner av orientering till tid, rum och person; orientering till sig själv och andra; desorientering till tid, rum och person";


        final Optional<IcfKod> icfKod = icfTextResource.lookupTextByIcfKod(matchingCode);

        assertThat(icfKod).isPresent();
        assertThat(icfKod.get().getKod()).isEqualTo(matchingCode);
        assertThat(icfKod.get().getBenamning()).isEqualTo(expectedBenamning);
        assertThat(icfKod.get().getBeskrivning()).isEqualTo(expectedBeskrivning);
        assertThat(icfKod.get().getInnefattar()).isEqualTo(expectedInnefattar);
    }

}
