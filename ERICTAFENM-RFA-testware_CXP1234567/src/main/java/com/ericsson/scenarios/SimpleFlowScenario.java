package com.ericsson.scenarios;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataSource;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.scenario.AllureScenarioListener;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.scenario.api.ExceptionHandler;
import com.ericsson.cifwk.taf.scenarios.SummaryLogger;
import com.ericsson.enm.data.CommonDataSources;
import com.ericsson.enm.provider.InitialUserProvider;
import com.ericsson.nms.node.NetworkNodeRestOperator;
import com.ericsson.nms.security.flows.OpenIDMFlows;
import com.ericsson.oss.clientcommon.login.teststeps.LoginLogoutTestSteps;
import com.ericsson.oss.rfa.data.metric.Metric;
import com.ericsson.oss.rfa.flows.CmEditorFlows;
import com.ericsson.oss.rfa.teststeps.CmEditorTestSteps;
import com.google.common.collect.Maps;

public class SimpleFlowScenario extends TorTestCaseHelper {

    public static AtomicInteger cnt = new AtomicInteger(0);
    Logger log = LoggerFactory.getLogger(SimpleFlowScenario.class);

    @Inject
    OpenIDMFlows openIDMFlows;

    @Inject
    NetworkNodeRestOperator op;

    @Inject
    LoginLogoutTestSteps loginSteps;

    @Inject
    CmEditorTestSteps cmSteps;

    TestContext context = TafTestContext.getContext();

    TestScenarioRunner runner;

    public static class CmCommandsDs {
        @DataSource
        public List<Map<String, String>> cmCommands() {
            Map<String, String> commands = Maps.newHashMap();
            commands.put("command", "cmedit get * MeContext.*");
            commands.put("commandSuccess", "true");
            return Collections.singletonList(commands);
        }
    }

    /**
     * Simple scenario to create a user using a datasource, then login using
     * that user, logout and delete the user that was created
     */

    @BeforeSuite
    public void prepareDs() {
        log.info("Adding data source for time {}", cnt.getAndIncrement());
        context.addDataSource(CmEditorFlows.CM_COMMANDS_DATASOURCE, TafDataSources.fromClass(CmCommandsDs.class));

    }

    @Test
    @Context(context = { Context.REST })
    @TestId(id = "SimpleFlow", title = "Create user, login, logout, delete user")
    public void simpleLoginScenario() {

        TestDataSource<DataRecord> initialUser =
                TafDataSources.fromClass(InitialUserProvider.class);
        context.addDataSource(CommonDataSources.AVAILABLE_USERS,
                initialUser);
        SummaryLogger logger = new SummaryLogger();
        ExceptionHandler h = new SummaryLogger.ExceptionHandler();
        TestScenario scenario = scenario("LoginLogoutScenario") //
                .addFlow(openIDMFlows.createUser()).addFlow(flow("login and logut")
                        .addTestStep(annotatedMethod(loginSteps, "login"))
                        .addTestStep(annotatedMethod(loginSteps, "verify user is logged in"))
                        .addTestStep(annotatedMethod(cmSteps,
                                CmEditorTestSteps.SIMPLE_EXECUTE_COMMAND))
                        .addTestStep(annotatedMethod(loginSteps, "verify user is logged in"))
                        .addTestStep(annotatedMethod(loginSteps, "logout"))
                        .addTestStep(annotatedMethod(loginSteps, "cleanUp"))
                        .withVusers(DataHandler.getConfiguration().getProperty("vusers", 20,
                                Integer.class))
                        .withDataSources(dataSource(CommonDataSources.AVAILABLE_USERS),
                                dataSource(CmEditorFlows.CM_COMMANDS_DATASOURCE)).build()) //
                .addFlow(openIDMFlows.deleteUser()).withTestStepExceptionHandler(h)

                .build();

        runner = runner().withListener(logger).withListener(new
                AllureScenarioListener()).withListener(new Metric()).build();

        runner.start(scenario);

    }
}
