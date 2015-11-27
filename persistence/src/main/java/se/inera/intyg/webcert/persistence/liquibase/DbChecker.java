package se.inera.intyg.webcert.persistence.liquibase;

import java.sql.SQLException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

public class DbChecker {

    private static final Logger LOG = LoggerFactory.getLogger(DbChecker.class);

    private DataSource dataSource;

    private String script;

    public DbChecker(DataSource dataSource, String script) {
        super();
        this.dataSource = dataSource;
        this.script = script;
    }

    @PostConstruct
    public void checkDb() {
        try {
            DatabaseConnection connection = new JdbcConnection(dataSource.getConnection());
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
            Liquibase liquibase = new Liquibase(script, new ClassLoaderResourceAccessor(), database);
            LOG.info("Checking database: {} URL:{}", database.getDatabaseProductName(), database.getConnection().getURL());
            List<ChangeSet> changeSets = liquibase.listUnrunChangeSets(null, liquibase.getChangeLogParameters().getLabels());
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
}
