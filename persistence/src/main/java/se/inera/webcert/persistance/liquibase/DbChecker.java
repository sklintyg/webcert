package se.inera.webcert.persistance.liquibase;

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
import java.sql.SQLException;
import java.util.List;

public class DbChecker {
    private static final Logger LOG = LoggerFactory.getLogger(DbChecker.class);

    public DbChecker(DataSource dataSource, String script) {
        try {
            DatabaseConnection connection = new JdbcConnection(dataSource.getConnection());
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
            Liquibase liquibase = new Liquibase(script, new ClassLoaderResourceAccessor(), database);
            LOG.info(database.getConnection().getURL());
            List<ChangeSet> changeSets = liquibase.listUnrunChangeSets(null);
            if (!changeSets.isEmpty()) {
                StringBuilder errors = new StringBuilder();
                for (ChangeSet changeSet : changeSets) {
                    errors.append('>').append(changeSet.toString()).append('\n');
                }
                throw new Error("Database version mismatch. Check liquibase status. Errors:\n" + errors.toString() + database.getDatabaseProductName() + ", " + database);
            }
        } catch (liquibase.exception.LiquibaseException e) {
            throw new Error("Database not ok, aborting startup.", e);
        } catch (SQLException e) {
            throw new Error("Database not ok, aborting startup.", e);
        }
        LOG.info("Liquibase ok");
    }
}
