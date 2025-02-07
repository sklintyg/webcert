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
package se.inera.intyg.webcert.web.service.diagnos.repo;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import se.inera.intyg.webcert.web.service.diagnos.IcdCodeConverter;
import se.inera.intyg.webcert.web.service.diagnos.model.Diagnos;

/**
 * Factory responsible for creating the DiagnosRepository out of supplied code files.
 *
 * @author npet
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DiagnosRepositoryFactory {

    private static final String BOM = "\uFEFF";
    private static final String ASTERISK_TAB_OR_DAGGER_TAB = "\\u002A\t|\u2020\t";
    private static final char SPACE = ' ';

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosRepositoryFactory.class);

    private final IcdCodeConverter icdCodeConverter;
    private final ResourceLoader resourceLoader;

    public DiagnosRepository createAndInitDiagnosRepository(List<String> filesList, Charset fileEncoding) {
        try {

            DiagnosRepositoryImpl diagnosRepository = new DiagnosRepositoryImpl();

            log.info("Creating DiagnosRepository from {} files using encoding '{}'", filesList.size(), fileEncoding);

            for (String file : filesList) {
                populateRepoFromDiagnosisCodeFile(file, diagnosRepository, fileEncoding);
            }

            diagnosRepository.openLuceneIndexReader();

            log.info("Created DiagnosRepository containing {} diagnoses", diagnosRepository.nbrOfDiagosis());

            return diagnosRepository;

        } catch (IOException e) {
            throw new IllegalStateException("Failure initiating DiagnosRepository", e);
        }
    }

    public void populateRepoFromDiagnosisCodeFile(String fileUrl, DiagnosRepositoryImpl diagnosRepository,
        Charset fileEncoding) {

        if (Strings.nullToEmpty(fileUrl).trim().isEmpty()) {
            return;
        }

        final String location = ResourceUtils.isUrl(fileUrl) ? fileUrl : "file:" + fileUrl;

        LOG.debug("Loading diagnosis from: '{}'", location);

        try {
            Resource resource = resourceLoader.getResource(location);

            if (!resource.exists()) {
                LOG.error("Could not load diagnosis file since '{}' does not exists", location);
                return;
            }

            int count = 0;
            IndexWriterConfig idxWriterConfig = new IndexWriterConfig(new StandardAnalyzer());
            try (IndexWriter idxWriter = new IndexWriter(diagnosRepository.getLuceneIndex(), idxWriterConfig);
                BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(),
                    fileEncoding))) {
                while (reader.ready()) {
                    String line = reader.readLine();
                    if (line != null) {
                        Diagnos diagnos = createDiagnosFromString(line, count == 0, fileEncoding);
                        if (diagnos != null) {
                            Document doc = new Document();
                            doc.add(new StringField(DiagnosRepository.CODE, diagnos.getKod(), Field.Store.YES));
                            doc.add(new TextField(DiagnosRepository.DESC, diagnos.getBeskrivning(), Field.Store.YES));
                            idxWriter.addDocument(doc);

                            count++;
                        }
                    }
                }
            }
            LOG.info("Loaded {} codes from file {}", count, fileUrl);

        } catch (IOException e) {
            LOG.error("IOException occured when loading diagnosis file '{}'", fileUrl);
            throw new IllegalStateException("Failure loading diagnosis file '%s".formatted(fileUrl), e);
        }
    }

    public Diagnos createDiagnosFromString(String line, boolean firstLineInFile, Charset fileEncoding) {

        if (Strings.nullToEmpty(line).trim().isEmpty()) {
            return null;
        }

        if (fileEncoding.equals(StandardCharsets.UTF_8)) {
            return icdCodeConverter.convert(line);
        }

        String cleanedLine = removeUnwantedCharacters(line, firstLineInFile);

        int firstSpacePos = cleanedLine.indexOf(SPACE);

        if (firstSpacePos == -1) {
            return null;
        }

        String kodStr = cleanedLine.substring(0, firstSpacePos);
        String beskStr = cleanedLine.substring(firstSpacePos + 1);

        Diagnos d = new Diagnos();
        d.setKod(kodStr.toUpperCase());
        d.setBeskrivning(beskStr);

        return d;
    }

    private String removeUnwantedCharacters(String line, boolean firstLineInFile) {
        String cleanedLine = line;
        if (firstLineInFile) {
            cleanedLine = cleanedLine.replaceFirst(BOM, "");
        }
        cleanedLine = cleanedLine.replaceFirst(ASTERISK_TAB_OR_DAGGER_TAB, String.valueOf(SPACE));
        return CharMatcher.whitespace().trimAndCollapseFrom(cleanedLine, SPACE);
    }
}
