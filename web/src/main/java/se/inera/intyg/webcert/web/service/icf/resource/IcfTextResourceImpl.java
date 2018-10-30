package se.inera.intyg.webcert.web.service.icf.resource;

import static java.lang.invoke.MethodHandles.lookup;

import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.control.Try;
import jxl.Sheet;
import jxl.Workbook;
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
import se.inera.intyg.webcert.web.web.controller.api.dto.IcfKod;

@Component
public class IcfTextResourceImpl implements IcfTextResource {

    private static final Logger LOG = LoggerFactory.getLogger(lookup().lookupClass());
    private static final String ACTION = "Initiate ICF Text Resources";

    private static final int ICF_KODER_COLUMN = 8;
    private static final int BENAMNING_COLUMN = 10;
    private static final int ALTERNATIV_TERM_COLUMN = 11;
    private static final int BESKRIVNING_COLUMN = 12;

    private HashMap<String, IcfKod> icfKoder = HashMap.empty();

    @Value("${icf.text.resource.path}")
    private Resource resource;

    @PostConstruct
    public void init() {
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
    public IcfKod lookupTextByIcfKod(final String icfKod) {
        return icfKoder.get(StringUtils.lowerCase(icfKod))
                .getOrNull();
    }

    private void initIcfTextResources() throws IOException, BiffException {
        final HashMap<Integer, HashMap<Integer, String>> rowsContent = getRowContent();
        parseIcfKodInfo(rowsContent);
    }

    private HashMap<Integer, HashMap<Integer, String>> getRowContent() throws IOException, BiffException {

        final Workbook workbook = Workbook.getWorkbook(resource.getFile());

        //Sheet 1 är det sheet som innehåller diagnoskoder + texter
        final Sheet sheet = workbook.getSheet(1);

        final int rows = sheet.getRows();
        final int columns = sheet.getColumns();

        //Starta inläsning från row 1, row 0 innehåller endast headers
        final int startRow = 1;

        //Starta inläsning från column 1, column 0 innehåller endast radIndex
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

            final String icfKod = StringUtils.trim(StringUtils.lowerCase(rowColumns.get(ICF_KODER_COLUMN).get()));
            final String benamning = StringUtils.trim(rowColumns.get(BENAMNING_COLUMN).get());
            final String alternativTerm = StringUtils.trim(rowColumns.get(ALTERNATIV_TERM_COLUMN).get());
            final String beskrivning = StringUtils.trim(rowColumns.get(BESKRIVNING_COLUMN).get());

            //om alternativTerm finns ska den alltid trumfa vanlig benämning
            final String benamningToReturn = StringUtils.isNotEmpty(alternativTerm)
                    ? alternativTerm
                    : benamning;

            icfKoder = icfKoder.put(icfKod, IcfKod.of(icfKod, benamningToReturn, beskrivning));
        });
    }
}
