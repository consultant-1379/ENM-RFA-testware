package com.ericsson.scenarios;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.scenario.impl.LoggingScenarioListener;
import com.ericsson.nms.security.flows.OpenIDMFlows;
import com.ericsson.oss.clientcommon.login.flows.LoginLogoutFlows;

public class SimpleENMPerfFlowScenario extends TorTestCaseHelper {

    Logger log = LoggerFactory.getLogger(SimpleENMPerfFlowScenario.class);

    @Inject
    OpenIDMFlows openIDMFlows;

    @Inject
    LoginLogoutFlows launcherFlows;

    TestContext context = TafTestContext.getContext();

    TestScenarioRunner runner;

    /**
     * Simple scenario to create a user using a datasource, then login using
     * that user, logout and delete the user that was created
     */
    @Test
    @Context(context = { Context.REST })
    @TestId(id = "SimpleFlowPerf", title = "Create user, login, logout, delete user")
    public void simpleENMPerfFlowScenario() {

        TestScenario scenario = scenario("LoginLogoutScenarioPerf")
                .addFlow(openIDMFlows.createUser())
                .addFlow(launcherFlows.login())
                .addFlow(launcherFlows.logout())
                .addFlow(openIDMFlows.deleteUser())
                .withVusers(3)
                .build();

        runner = runner().withListener(new LoggingScenarioListener()).build();

        runner.start(scenario);

    }

}
