package com.ericsson.scenarios;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.subFlow;
import static com.ericsson.enm.data.CommonDataSources.USERS_TO_CREATE;
import static com.ericsson.oss.rfa.teststeps.NodeTestSteps.ADD_NODE;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.api.ExceptionHandler;
import com.ericsson.cifwk.taf.scenarios.SummaryLogger;
import com.ericsson.enm.data.CommonDataSources;
import com.ericsson.enm.data.NetworkNode;
import com.ericsson.nms.security.flows.OpenIDMFlows;
import com.ericsson.nms.security.teststeps.OpenIDMTestSteps;
import com.ericsson.oss.clientcommon.login.teststeps.LoginLogoutTestSteps;
import com.ericsson.oss.rfa.teststeps.NodeTestSteps;

public class PonytailExpress extends TorTestCaseHelper {

    private static final Logger log = LoggerFactory.getLogger(PonytailExpress.class);
    @Inject
    OpenIDMFlows openIDMFlows;

    @Inject
    LoginLogoutTestSteps loginSteps;

    @Inject
    TestContext context;

    @Inject
    NodeTestSteps nodeSteps;

    TestScenarioRunner runner;

    @Inject
    OpenIDMTestSteps openIDMTestSteps;

    public TestStepFlow createUser() {
        return flow("Login as default user and create user for flow")
                .addTestStep(annotatedMethod(openIDMTestSteps, "createUser"))
                .withDataSources(dataSource(USERS_TO_CREATE)).build();
    }

    public static final String USERS_TO_LOGIN = "usersToLogin";
    public static final int SCENARIO_VUSERS = 1;

    public void setDataSources() {
        CommonDataSources.initializeDataSources();
        context.addDataSource(USERS_TO_LOGIN, TafDataSources.cyclic(context.dataSource(CommonDataSources.AVAILABLE_USERS)));
        context.dataSource(CommonDataSources.ADDED_NODES, NetworkNode.class);
        context.addDataSource(CommonDataSources.USERS_TO_CREATE, TafDataSources.fromCsv("data/usersToCreate.csv"));
        context.addDataSource(CommonDataSources.USERS_TO_DELETE, context.dataSource(CommonDataSources.AVAILABLE_USERS));
        context.addDataSource(CommonDataSources.NODES_TO_ADD, TafDataSources.shared(TafDataSources.fromCsv("data/nodeToAdd.csv")));
    }

    public TestStepFlow s1AddUsersAndNodes() {
        return flow("sequence of adding users and nodes")
                .addTestStep(subFlow(openIDMFlows.createUser()))
                .addTestStep(subFlow(flow("nodes operations")
                        .addTestStep(subFlow(addNodes()))
                        .addTestStep(subFlow(addSecurity()))
                        .addTestStep(subFlow(syncNodes()))
                        .addTestStep(annotatedMethod(loginSteps, "logout"))
                        .build()))

                .build();
    }

    public TestStepFlow addNodes() {
        return flow("login and add nodes")
                .addTestStep(annotatedMethod(loginSteps, "login"))
                .addTestStep(annotatedMethod(nodeSteps, ADD_NODE))
                .withDataSources(dataSource(USERS_TO_LOGIN).bindTo(CommonDataSources.AVAILABLE_USERS),
                        dataSource(CommonDataSources.NODES_TO_ADD))
                .build();
    }

    public TestStepFlow addSecurity() {
        return flow("Add security to added nodes")
                .addTestStep(annotatedMethod(nodeSteps, NodeTestSteps.ADD_SECURITY_TO_NODE))
                .withDataSources(dataSource(CommonDataSources.ADDED_NODES))
                .build();
    }

    public TestStepFlow syncNodes() {
        return flow("Sync added nodes")
                // .addTestStep(annotatedMethod(instance, "sync"))
                .withDataSources(dataSource(CommonDataSources.ADDED_NODES))
                .build();
    }

    public TestStepFlow s2DeleteNodesAndUsers() {
        return flow("delete nodes and users")
                .addTestStep(subFlow(flow("delete nodes")
                        .addTestStep(subFlow(
                                flow("delete nodes")
                                        .addTestStep(annotatedMethod(loginSteps, "login"))
                                        .addTestStep(annotatedMethod(nodeSteps, NodeTestSteps.DELETE_NODE))
                                        .withDataSources(dataSource(CommonDataSources.ADDED_NODES), dataSource(USERS_TO_LOGIN).bindTo(CommonDataSources.AVAILABLE_USERS))
                                        .build()
                                ))
                        .addTestStep(annotatedMethod(loginSteps, "logout"))
                        .build()))
                .addTestStep(subFlow(openIDMFlows.deleteUser()))
                .build();
    }

    @Test
    @Context(context = Context.REST)
    @TestId(id = "TAF_KVM_Func_1", title = "End To End Smoke test for KVM topology")
    public void ponytailExpressScenario() {

        SummaryLogger logger = new SummaryLogger();
        ExceptionHandler h = new SummaryLogger.ExceptionHandler();
        setDataSources();
        TestScenario scenario = scenario("ENM KVM Smoke test")
                .addFlow(s1AddUsersAndNodes())
                .addFlow(s2DeleteNodesAndUsers())
                .withTestStepExceptionHandler(h)
                .withVusers(SCENARIO_VUSERS)
                .build();

        runner = runner().withListener(logger)
                .withExceptionHandler(ExceptionHandler.IGNORE)
                .build();
        runner.start(scenario);
    }
}
