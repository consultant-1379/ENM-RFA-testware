package com.ericsson.enm.rfa;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.ericsson.enm.data.CommonDataSources.initializeDataSources;

import javax.inject.Inject;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.impl.LoggingScenarioListener;
import com.ericsson.enm.data.ENMUser;
import com.ericsson.enm.data.NetworkNode;

public class BasicRFATest extends TorTestCaseHelper {

    private static final String NODES_TO_ADD = "nodesToAdd";
    private static final String USERS_TO_ADD = "usersToAdd";

    //@BeforeTest
    public void initialize() {
        initializeDataSources();
        context.dataSource(NODES_TO_ADD, NetworkNode.class);
        context.dataSource(USERS_TO_ADD, ENMUser.class);
    }

    @Inject
    TestContext context;

    @Inject
    MockedFlows flows;

    //@Test
    public void basicFlowTest() {

        TestScenario rfaScenario = scenario("RFA flow")
                .addFlow(flows.addUsers())
                // requires list of users to add; produces AVAILABLE_USERS data
                // source
                .addFlow(flows.login())
                // requires AVAILABLE_USERS data source; provides UserSession
                // with logged in tool
                .addFlow(flows.addNodes())
                // requires NODES_TO_ADD list produces ADDED_NODES data source
                .addFlow(flows.syncNodes())
                // requires ADDED_NODES data source; produces SYNCHED_NODES data
                // source
                .addFlow(flows.manageSubscriptions())
                // requires PMIC_NODES data source and Subscriptions list
                .addFlow(flows.manageCollections())
                // requires ADDED_NODES data source and Collection lists
                .addFlow(flows.deleteNodes())
                // requires ADDED_NODES data source; modifies ADDED_NODES data
                // source by removing all the nodes
                .addFlow(flows.logout())
                // modifies UserSession
                .addFlow(flows.deleteUsers())
                // requires AVAILABLE_USERS data source; modifies
                // AVAILABLE_USERS data source by removing all of them
                .build();
        runner().withListener(new LoggingScenarioListener()).build()
                .start(rfaScenario);
    }
}
