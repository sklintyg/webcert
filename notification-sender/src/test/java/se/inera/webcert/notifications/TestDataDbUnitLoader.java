package se.inera.webcert.notifications;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.repository.UtkastRepository;

public class TestDataDbUnitLoader {

    private static final Logger LOG = LoggerFactory.getLogger(TestDataDbUnitLoader.class);

    private String dataFile;

    private List<String> intygsIds = new ArrayList<String>();

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UtkastRepository utkastRepository;

    public void loadTestData() throws Exception {
        loadBasicData();
        loadModelsOnIntyg();
    }

    public void loadModelsOnIntyg() {

        List<String> intygsIdList = getIntygsIds();

        LOG.info("Inserting model data into intyg {}", intygsIdList);

        for (String intygsId : intygsIdList) {
            Utkast utkast = utkastRepository.findOne(intygsId);
            utkast.setModel(getModelData(intygsId));
            utkastRepository.save(utkast);
        }

    }

    private String getModelData(String intygsId) {
        String modelFilePath = "utlatande/utlatande-intyg-1.json";
        return TestDataUtil.readRequestFromFile(modelFilePath);
    }

    public void loadBasicData() throws Exception {

        String testDataFile = getDataFile();

        LOG.info("Loading test data into database from '{}'", testDataFile);

        IDataSet dataSet = getDataSet(testDataFile);

        IDatabaseConnection dbConn = new DatabaseDataSourceConnection(dataSource);
        DatabaseOperation.INSERT.execute(dbConn, dataSet);
    }

    private IDataSet getDataSet(String filePath) throws DatabaseUnitException, IOException {
        ClassPathResource resource = new ClassPathResource(filePath);
        return new FlatXmlDataSetBuilder().build(resource.getInputStream());
    }

    public String getDataFile() {
        return dataFile;
    }

    public void setDataFile(String dataFile) {
        this.dataFile = dataFile;
    }

    public List<String> getIntygsIds() {
        return intygsIds;
    }

    public void setIntygsIds(List<String> intygsIds) {
        this.intygsIds = intygsIds;
    }
}
