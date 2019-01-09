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
                throw new Error("Database version mismatch. Check liquibase status. Errors:\n" + errors.toString()
                        + database.getDatabaseProductName() + ", " + database);
            }
        } catch (liquibase.exception.LiquibaseException | SQLException e) {
            throw new Error("Database not ok, aborting startup.", e);
        }
        LOG.info("Liquibase ok");
    }
}
