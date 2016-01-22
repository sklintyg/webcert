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

package se.inera.intyg.webcert.web.service.texts.repo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import se.inera.intyg.webcert.web.service.texts.model.IntygTexts;
import se.inera.intyg.webcert.web.service.texts.model.Tillaggsfraga;

@Repository
public class IntygTextsRepositoryImpl implements IntygTextsRepository {
    private static final String TEXT_SUFFIX = "RBK";

    private static final String HELP_TEXT_SUFFIX = "HLP";

    private static final Logger LOG = LoggerFactory.getLogger(IntygTextsRepository.class);

    /**
     * The in-memory database of the texts available.
     *
     * Gets updated on a schedule defined in properties.
     */
    protected Set<IntygTexts> intygTexts;

    @Value("${texts.file.directory}")
    private String fileDirectory;

    /**
     * Initial setup of the in-memory database.
     */
    @PostConstruct
    public void init() {
        intygTexts = new HashSet<>();
        update();
    }

    /**
     * Updates the texts.
     *
     * Will parse all files once more and add those not already in memory.
     */
    @Scheduled(cron = "${texts.update.cron}")
    public void update() {
        try {
            Files.walk(Paths.get(fileDirectory)).forEach((file) -> {
                try {
                    Document doc = DocumentBuilderFactory.newInstance()
                            .newDocumentBuilder()
                            .parse(Files.newInputStream(file));

                    Element root = doc.getDocumentElement();
                    String version = root.getAttribute("version");
                    String intygsTyp = root.getAttribute("typ");
                    LocalDate giltigFrom = getDate(root, "giltigFrom");
                    LocalDate giltigTo = getDate(root, "giltigTom");
                    SortedMap<String, String> texts = getTexter(root);
                    List<Tillaggsfraga> tillaggsFragor = getTillaggsfragor(doc);

                    IntygTexts newIntygTexts = new IntygTexts(version, intygsTyp, giltigFrom, giltigTo, texts, tillaggsFragor);
                    if (!intygTexts.contains(newIntygTexts)) {
                        LOG.debug("Adding new version of {} with version name {}", intygsTyp, version);
                        intygTexts.add(newIntygTexts);
                    }
                } catch (IllegalArgumentException e) {
                    LOG.error("Bad file in directory {}: {}", fileDirectory, e.getMessage());
                    e.printStackTrace();
                } catch (IOException | ParserConfigurationException | SAXException e) {
                    LOG.error("Error while reading file {}", file.getFileName(), e);
                }
            });
        } catch (IOException e) {
            LOG.error("Error while reading from directory {}", fileDirectory, e);
        }
    }

    private LocalDate getDate(Element root, String id) {
        String date = root.getAttribute(id);
        return date == null || "".equals(date) ? null : LocalDate.parse(date);
    }

    private SortedMap<String, String> getTexter(Element element) {
        SortedMap<String, String> texts = new TreeMap<>();
        NodeList textsList = element.getElementsByTagName("text");
        for (int i = 0; i < textsList.getLength(); i++) {
            Element textElement = (Element) textsList.item(i);
            texts.put(textElement.getAttribute("id"), textElement.getTextContent());
        }
        return texts;
    }

    private List<Tillaggsfraga> getTillaggsfragor(Document doc) {
        List<Tillaggsfraga> tillaggsFragor = new ArrayList<>();
        NodeList tillaggList = doc.getElementsByTagName("tillaggsfraga");
        for (int i = 0; i < tillaggList.getLength(); i++) {
            Tillaggsfraga tillaggTexts = getTillaggsFraga((Element) tillaggList.item(i));
            tillaggsFragor.add(tillaggTexts);
        }
        return tillaggsFragor;
    }

    private Tillaggsfraga getTillaggsFraga(Element element) {
        String id = element.getAttribute("id");
        String text = "";
        String help = "";

        NodeList textsList = element.getElementsByTagName("text");
        for (int i = 0; i < textsList.getLength(); i++) {
            Element textElement = (Element) textsList.item(i);
            String textId = textElement.getAttribute("id");

            // In tillaggsfragor the ids of the tags are of fixed format. The texts WILL end with RBK and the helptexts
            // WILL end with HLP.
            if (textId.endsWith(HELP_TEXT_SUFFIX)) {
                help = textElement.getTextContent();
            } else if (textId.endsWith(TEXT_SUFFIX)) {
                text = textElement.getTextContent();
            } else {
                throw new IllegalArgumentException("Could not parse the id " + textId + " as a tillaggsfraga");
            }
        }
        return new Tillaggsfraga(id, text, help);
    }

    @Override
    public String getLatestVersion(String intygsTyp) {
        IntygTexts res = intygTexts.stream()
                .filter((s) -> s.getIntygsTyp().equals(intygsTyp))
                .filter((s) -> s.getValidFrom() == null || !s.getValidFrom().isAfter(LocalDate.now()))
                .max(IntygTexts::compareVersions).orElse(null);
        return res == null ? null : res.getVersion();
    }

    @Override
    public IntygTexts getTexts(String intygsTyp, String version) {
        try {
            IntygTexts wanted = new IntygTexts(version, intygsTyp, null, null, null, null);
            for (IntygTexts intygText : intygTexts) {
                if (wanted.equals(intygText)) {
                    return intygText;
                }
            }
        } catch (IllegalArgumentException e) {
            LOG.error("Malformed version number {}, message: {}", version, e.getMessage());
            return null;
        }
        LOG.error("Tried to access texts for intyg of type {} and version {}, but this does not exist", intygsTyp, version);
        return null;
    }
}
