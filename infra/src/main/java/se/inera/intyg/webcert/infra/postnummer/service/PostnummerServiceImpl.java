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
package se.inera.intyg.webcert.infra.postnummer.service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.infra.postnummer.model.Omrade;
import se.inera.intyg.webcert.infra.postnummer.repository.PostnummerRepository;
import se.inera.intyg.webcert.infra.postnummer.repository.PostnummerRepositoryFactory;

@Service
public class PostnummerServiceImpl implements PostnummerService {

  private static final Logger LOG = LoggerFactory.getLogger(PostnummerRepositoryFactory.class);

  @Value("${postnummer.file}")
  private String sourceFile;

  @Autowired private PostnummerRepositoryFactory postnummerRepositoryFactory;

  private PostnummerRepository postnummerRepository;

  @PostConstruct
  public void init() {
    postnummerRepository =
        postnummerRepositoryFactory.createAndInitPostnummerRepository(sourceFile);
  }

  @Override
  public List<Omrade> getOmradeByPostnummer(String postnummer) {
    LOG.debug("Lookup omrade by postnummer '{}'", postnummer);
    return postnummerRepository.getOmradeByPostnummer(postnummer);
  }

  @Override
  public List<String> getKommunList() {
    return postnummerRepository.getKommunList();
  }
}
