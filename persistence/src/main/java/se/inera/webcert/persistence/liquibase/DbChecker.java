package se.inera.webcert.persistence.liquibase;


import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

public class DbChecker {
    private static final Logger LOG = LoggerFactory.getLogger(DbChecker.class);

    public DbChecker(DataSource dataSource, String script) {
        try {
            DatabaseConnection connection = new JdbcConnection(dataSource.getConnection());
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
            Liquibase liquibase = new Liquibase(script, new ClassPathResourceAccessor(), database);
            LOG.info(database.getConnection().getURL());
            List<ChangeSet> changeSets = liquibase.listUnrunChangeSets(null);
            if (!changeSets.isEmpty()) {
                StringBuilder errors = new StringBuilder();
                for (ChangeSet changeSet : changeSets) {
                    errors.append('>').append(changeSet.toString()).append('\n');
                }
                throw new Error("Database version mismatch. Check liquibase status. Errors:\n" + errors.toString() + database.getDatabaseProductName() + ", " + database);
            }
        } catch (liquibase.exception.LiquibaseException | SQLException e) {
            throw new Error("Database not ok, aborting startup.", e);
        }
        LOG.info("Liquibase ok");
    }

    /**
     * We have used SpringLiquibase, which has the undesirable trait that it prepends "classpath:" to filenames stored
     * in liquibase.
     *
     * This class is a workaround to make non-Springliquibase code work with SpringLiquibase-managed databases.
     * An alternative solution would be to scrap SpringLiquibase altogether, but then the FILENAME needs to be
     * updated in the database. (E.g. update databasechangelog set filename='changelog/changelog.xml')
     */
    private class ClassPathResourceAccessor extends ClassLoaderResourceAccessor {
        @Override
        public InputStream getResourceAsStream(String file) throws IOException {
            if (file.startsWith("classpath:")) {
                file = file.substring(file.indexOf(':') + 1);
            }
            return super.getResourceAsStream(file);
        }
    }
}
