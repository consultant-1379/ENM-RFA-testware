package com.ericsson.scenarios;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;

import javax.inject.Inject;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.scenario.impl.LoggingScenarioListener;
import com.ericsson.oss.pmic.flows.PMICFlows;

public class PMICScenario extends TorTestCaseHelper {

    @Inject
    PMICFlows pmicFlows;

    TestContext context = TafTestContext.getContext();

    TestScenarioRunner runner;

    public static final String USERS = "Users";
    public static final String ADDED_NODES = "addedNode";

    @BeforeClass
    public void setUp() {
        context.dataSource(USERS);
        context.dataSource(ADDED_NODES);
        TestScenario scenario = scenario()
                .addFlow(pmicFlows.createUser())
                .addFlow(pmicFlows.login())
                .addFlow(pmicFlows.addNode())
                .build();

        runner = runner().withListener(new LoggingScenarioListener()).build();

        runner.start(scenario);
    }

    @Test
    @TestId(id = "CIP-6315", title = "Description")
    public void createSubscription() {
        TestScenario scenario = scenario()
                .addFlow(pmicFlows.createSubscription())
                .addFlow(pmicFlows.activateSubscription())
                .addFlow(pmicFlows.monitorFiles())
                .addFlow(pmicFlows.deactivateSubscription())
                .addFlow(pmicFlows.deleteSubscription())
                .build();

        runner = runner().withListener(new LoggingScenarioListener()).build();

        runner.start(scenario);
    }

    @AfterClass
    public void tearDown() {
        TestScenario scenario = scenario()
                .addFlow(pmicFlows.postConditions())
                .build();

        runner.start(scenario);
    }

}
