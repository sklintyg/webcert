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
package se.inera.intyg.webcert.web.service.fmb.icf.resource;

import static java.lang.invoke.MethodHandles.lookup;
import static org.apache.commons.lang3.StringUtils.lowerCase;

import io.vavr.collection.HashMap;
import io.vavr.control.Try;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
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
    private String location;

    @Autowired
    ResourceLoader resourceLoader;

    @PostConstruct
    void init() {
        LOG.info(MessageFormat.format("Starting: {0}", ACTION));

        if (!ResourceUtils.isUrl(location)) {
            location = "file:" + location;
        }

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

    private void initIcfTextResources() throws IOException {
        final HashMap<Integer, HashMap<Integer, String>> rowsContent = getRowContent();
        parseIcfKodInfo(rowsContent);
    }

    private HashMap<Integer, HashMap<Integer, String>> getRowContent() throws IOException {

        try (InputStream is = resourceLoader.getResource(location).getInputStream();
            Workbook workbook = WorkbookFactory.create(is)) {

            final Sheet diagnoseCodesSheet = workbook.getSheetAt(1);

            final int lastRowNum = diagnoseCodesSheet.getLastRowNum();
            final int rows = lastRowNum + 1;

            final int startRow = 1;
            final int startColumn = 1;

            HashMap<Integer, HashMap<Integer, String>> data = HashMap.empty();
            for (int rowNum = startRow; rowNum < rows; rowNum++) {
                final Row row = diagnoseCodesSheet.getRow(rowNum);
                if (row == null) {
                    continue;
                }

                // Check if row has any data in the ICF_KODER_COLUMN (column 8) - skip empty rows
                final Cell icfKodCell = row.getCell(ICF_KODER_COLUMN);
                if (icfKodCell == null || StringUtils.isBlank(getCellValueAsString(icfKodCell))) {
                    continue;
                }

                HashMap<Integer, String> rowContent = HashMap.empty();
                final int lastCellNum = row.getLastCellNum();

                for (int column = startColumn; column < lastCellNum; column++) {
                    final Cell cell = row.getCell(column);
                    final String cellValue = getCellValueAsString(cell);
                    rowContent = rowContent.put(column, cellValue);
                }
                data = data.put(rowNum, rowContent);
            }
            return data;
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        CellType cellType = cell.getCellType();
        if (cellType == CellType.FORMULA) {
            cellType = cell.getCachedFormulaResultType();
        }

        switch (cellType) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    private void parseIcfKodInfo(final HashMap<Integer, HashMap<Integer, String>> rowsContent) {
        rowsContent.forEach(rowContent -> {
            final HashMap<Integer, String> rowColumns = rowContent._2;

            final String icfKod = StringUtils.trim(lowerCase(rowColumns.get(ICF_KODER_COLUMN).getOrElse("")));
            final String benamning = StringUtils.trim(rowColumns.get(BENAMNING_COLUMN).getOrElse(""));
            final String alternativTerm = StringUtils.trim(rowColumns.get(ALTERNATIV_TERM_COLUMN).getOrElse(""));
            final String beskrivning = StringUtils.trim(rowColumns.get(BESKRIVNING_COLUMN).getOrElse(""));
            final String innefattar = StringUtils.trim(rowColumns.get(INNEFATTAR_COLUMN).getOrElse(""));

            final String benamningToReturn = StringUtils.isNotEmpty(alternativTerm)
                ? alternativTerm
                : benamning;

            icfKoder = icfKoder.put(icfKod, IcfKod.of(icfKod, benamningToReturn, beskrivning, innefattar));
        });
    }
}
