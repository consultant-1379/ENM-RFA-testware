package com.ericsson.enm.rfa;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.enm.data.CommonDataSources.ADDED_NODES;
import static com.ericsson.enm.data.CommonDataSources.AVAILABLE_USERS;

import com.ericsson.cifwk.taf.scenario.TestStepFlow;

public class MockedFlows {
    public static final String SYNCHED_NODES = "SYNCHED_NODES";

    TestStepFlow addUsers() {
        return flow("Add Users").build();
    }

    TestStepFlow login() {
        return flow("Login").withDataSources(dataSource(AVAILABLE_USERS))
                .build();
    }

    TestStepFlow addNodes() {
        return flow("Add Nodes").build();
    }

    TestStepFlow syncNodes() {
        return flow("Synch Nodes").withDataSources(dataSource(ADDED_NODES))
                .build();
    }

    TestStepFlow manageSubscriptions() {
        return flow("Manage Subscriptions").withDataSources(
                dataSource(SYNCHED_NODES)).build();
    }

    TestStepFlow manageAlerts() {
        return flow("Manage Alerts").withDataSources(dataSource(SYNCHED_NODES))
                .build();
    }

    TestStepFlow manageCollections() {
        return flow("Manage Collections").withDataSources(
                dataSource(SYNCHED_NODES)).build();
    }

    TestStepFlow deleteNodes() {
        return flow("Delete Nodes").withDataSources(dataSource(ADDED_NODES))
                .build();
    }

    TestStepFlow logout() {
        return flow("Logout").build();
    }

    TestStepFlow deleteUsers() {
        return flow("Delete Users")
                .withDataSources(dataSource(AVAILABLE_USERS)).build();
    }
}
