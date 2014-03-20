package se.inera.certificate.mc2wc.dbunit;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.xml.FlatDtdDataSet;
import org.dbunit.dataset.xml.FlatXmlWriter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ExportFromMedcertDb {

    private static final String IDS = "'0008f4f6-a081-497c-9f23-942525219536','006377cd-a080-4532-875a-8d26f46162a6','012dd499-b3a8-49b2-884a-a731b2511e9a','01ae2e41-4284-4813-a5d6-45f1cbeef6ea','02413979-c0d4-48a3-91b6-021cbf20b26f','02f4713f-2ea1-46c1-b55e-fbb396ec02b9','039ac74f-b1a0-408c-86ab-d4abf91e0bab','04344b30-3875-4677-ac0a-dd84c0769843','047cbc70-f689-430a-a583-9b201e97d25f','04aa8ca6-62c1-4662-a38e-a8884449b4da','0506db69-8b5d-4e5a-9bd9-17122b0386cd','05083943-997b-4c2e-8bd0-f8fc896b5697','0564b5a1-649b-42f9-b9f9-1678279c766f','056f7e7d-e5dc-4cb8-bd58-54ecb99de801','056fcd13-71c2-430a-9b33-0f3ca2debbea','05b46ce4-d8b5-4081-8cab-f05bead94175','05fec8bd-ddf6-4203-b888-153f604ddf46','06636cf2-f508-46ca-bfa9-548558d19a64','069f0978-a2d3-4158-9a74-7985ddd3e5af','077243cf-674d-4a54-8807-1f9027e01a43','080462c0-178e-40ec-9914-10f586cb1792','08ba04e6-00dc-4842-ab6c-0b1d91ae4c57','09dcb61f-0e2f-4314-b0e6-0630375bb5db','0a2847b0-0575-40e1-9c56-48395116b174','0b134369-46f3-4d7d-8f63-63a3bec1db40'";

    public ExportFromMedcertDb() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param args
     * @throws IOException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws DatabaseUnitException
     * @throws ClassNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, SQLException, DatabaseUnitException, ClassNotFoundException {

        System.out.println("Exporting dataset...");

        Class.forName("com.mysql.jdbc.Driver");

        Connection jdbcConnection = DriverManager.getConnection("jdbc:mysql://localhost/medcert_sll", "inera", "inera");
        IDatabaseConnection connection = new DatabaseConnection(jdbcConnection);

        QueryDataSet partialDataSet = new QueryDataSet(connection);

        // Mention all the tables here for which you want data to be extracted
        // take note of the order to prevent FK constraint violation when re-inserting
        partialDataSet.addTable("certificate", "select * from certificate where ID in (" + IDS + ")");
        partialDataSet.addTable("question", "select * from question where certificate_id in (" + IDS + ")");
        partialDataSet.addTable("answer", "select * from answer where QUESTION_ID in (select ID from question where certificate_id in (" + IDS + "))");
        partialDataSet.addTable("complement", "select * from complement where QUESTION_ID in (select ID from question where certificate_id in (" + IDS + "))");

        // XML file into which data needs to be extracted
        FlatXmlWriter xmlWriter = new FlatXmlWriter(new FileOutputStream("src/test/resources/data/certificate_dataset_25.xml"), "UTF-8");
        xmlWriter.setIncludeEmptyTable(true);
        xmlWriter.write(partialDataSet);

        System.out.println("Dataset written!");

        FlatDtdDataSet.write(connection.createDataSet(), new FileOutputStream("src/test/resources/data/certificate_dataset.dtd"));

        System.out.println("DTD written!");
        System.out.println("Done!");

    }

}
