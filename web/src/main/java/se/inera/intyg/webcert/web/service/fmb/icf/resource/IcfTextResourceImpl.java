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

import static java.lang.invoke.MethodHandles.lookup;
import static org.apache.commons.lang3.StringUtils.lowerCase;

import io.vavr.collection.HashMap;
import io.vavr.control.Try;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Optional;
import se.inera.intyg.webcert.web.web.controller.api.dto.icf.IcfKod;

@Component
public class IcfTextResourceImpl implements IcfTextResource {

    private static final Logger LOG = LoggerFactory.getLogger(lookup().lookupClass());
    private static final String ACTION = "Initiate ICF Text Resources";

    private static final int ICF_KODER_COLUMN = 8;
    private static final int BENAMNING_COLUMN = 10;
    private static final int ALTERNATIV_TERM_COLUMN = 11;
    private static final int BESKRIVNING_COLUMN = 12;
    private static final int INNEFATTAR_COLUMN = 13;

    private HashMap<String, IcfKod> icfKoder = HashMap.empty();

    @Value("${icf.text.resource.path}")
    private Resource resource;

    @PostConstruct
    void init() {
        LOG.info(MessageFormat.format("Starting: {0}", ACTION));

        final Try<Void> initJob = Try.run(this::initIcfTextResources);

        if (initJob.isFailure()) {
            LOG.warn(MessageFormat.format("Could not {0}", ACTION));
            initJob.getCause().printStackTrace();
        } else {
            LOG.info(MessageFormat.format("Done: {0}", ACTION));
        }

    }

    @Override
    public Optional<IcfKod> lookupTextByIcfKod(final String icfKod) {
        return icfKoder.get(lowerCase(icfKod)).toJavaOptional();
    }

    private void initIcfTextResources() throws IOException, BiffException {
        final HashMap<Integer, HashMap<Integer, String>> rowsContent = getRowContent();
        parseIcfKodInfo(rowsContent);
    }

    private HashMap<Integer, HashMap<Integer, String>> getRowContent() throws IOException, BiffException {

        WorkbookSettings settings = new WorkbookSettings();
        settings.setEncoding("Cp1252");

        final Workbook workbook = Workbook.getWorkbook(resource.getFile(), settings);

        //Sheet 1 är det sheet som innehåller diagnoskoder + texter
        final Sheet sheet = workbook.getSheet(1);

        final int rows = sheet.getRows();
        final int columns = sheet.getColumns();

        //Starta inläsning från row 1, row 0 innehåller endast headers
        final int startRow = 1;

        //Starta inläsning från column 1, column 0 innehåller endast radindex
        final int startColumn = 1;

        HashMap<Integer, HashMap<Integer, String>> data = HashMap.empty();
        for (int row = startRow; row < rows; row++) {
            HashMap<Integer, String> rowContent = HashMap.empty();
            for (int column = startColumn; column < columns; column++) {
                rowContent = rowContent.put(column, sheet.getCell(column, row).getContents());
            }
            data = data.put(row, rowContent);
        }
        return data;
    }

    private void parseIcfKodInfo(final HashMap<Integer, HashMap<Integer, String>> rowsContent) {

        rowsContent.forEach(rowContent -> {
            final HashMap<Integer, String> rowColumns = rowContent._2;

            final String icfKod = StringUtils.trim(lowerCase(rowColumns.get(ICF_KODER_COLUMN).get()));
            final String benamning = StringUtils.trim(rowColumns.get(BENAMNING_COLUMN).get());
            final String alternativTerm = StringUtils.trim(rowColumns.get(ALTERNATIV_TERM_COLUMN).get());
            final String beskrivning = StringUtils.trim(rowColumns.get(BESKRIVNING_COLUMN).get());
            final String innefattar = StringUtils.trim(rowColumns.get(INNEFATTAR_COLUMN).get());

            //om alternativTerm finns ska den alltid trumfa vanlig benämning
            final String benamningToReturn = StringUtils.isNotEmpty(alternativTerm)
                    ? alternativTerm
                    : benamning;

            icfKoder = icfKoder.put(icfKod, IcfKod.of(icfKod, benamningToReturn, beskrivning, innefattar));
        });
    }
}
