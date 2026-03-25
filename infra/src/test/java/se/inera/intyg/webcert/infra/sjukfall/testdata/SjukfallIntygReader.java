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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/** Created by Magnus Ekstrand on 2016-02-11. */
public class SjukfallIntygReader {

  private List<String> lines = new ArrayList<>();

  private String location;
  private int linesToSkip;

  public SjukfallIntygReader(String location, int linesToSkip) {
    this.location = location;
    this.linesToSkip = linesToSkip;
  }

  public List<String> read() throws IOException {
    loadData();
    return lines;
  }

  private void loadData() throws IOException {
    // Read data
    ResourceLoader resourceLoader = new PathMatchingResourcePatternResolver();
    Resource resource = resourceLoader.getResource(location);

    // Parse data
    chunkifyData(resource);
  }

  private void chunkifyData(Resource resource) throws IOException {
    InputStream inputStream = resource.getInputStream();
    Scanner scanner = null;
    try {
      scanner = new Scanner(inputStream);
      int counter = 0;
      while (scanner.hasNext()) {
        String line = scanner.nextLine();
        if (!(counter < linesToSkip)) {
          lines.add(line);
        }
        counter++;
      }
    } finally {
      if (null != inputStream) {
        inputStream.close();
      }
      if (null != scanner) {
        scanner.close();
      }
    }
  }
}
