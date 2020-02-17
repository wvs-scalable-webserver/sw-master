package de.wvs.sw.master.application;

import de.progme.athena.Athena;
import de.progme.athena.query.core.CreateQuery;
import de.progme.athena.query.core.SelectQuery;
import de.progme.iris.IrisConfig;
import de.progme.iris.config.Header;
import de.wvs.sw.master.Master;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marvin Erkes on 12.02.20.
 */
public class ApplicationManager {

    private Athena athena;
    private List<Application> applications;

    public ApplicationManager() {

        this.applications = new ArrayList<>();

        this.athena = Master.getInstance().getAthena();
        this.setupTables();
    }

    public void startApplication(Application application) {

    }

    private void loadApplications() {

        this.athena.query(new SelectQuery.Builder()
                .select("*")
                .from("sw_applications")
                .build());
    }

    private void setupTables() {

        this.athena.execute(new CreateQuery.Builder()
                .create("sw_applications")
                .primaryKey("id")
                .value("id", "int", "auto_increment")
                .value("name", "varchar(255)")
                .value("minimum", "int")
                .value("maximum", "int")
                .build());
    }
}
