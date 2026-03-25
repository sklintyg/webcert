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
package se.inera.intyg.webcert.infra.sjukfall.testdata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import se.inera.intyg.webcert.infra.sjukfall.dto.IntygData;

/** Created by Magnus Ekstrand on 2016-02-10. */
public class SjukfallIntygGenerator {

  private final int linesToSkip = 1;

  private SjukfallIntygReader reader;
  private List<IntygData> intygData;

  public SjukfallIntygGenerator(String location) {
    this.reader = new SjukfallIntygReader(location, linesToSkip);
    this.intygData = new ArrayList<>();
  }

  public SjukfallIntygGenerator generate() throws IOException {
    List<String> csvlines = reader.read();
    intygData = SjukfallIntygLineMapper.map(csvlines);
    return this;
  }

  public List<IntygData> get() {
    return this.intygData;
  }

  public List<IntygData> get(Predicate<? super IntygData> predicate) {
    return this.intygData.stream().filter(predicate).collect(Collectors.toList());
  }
}
