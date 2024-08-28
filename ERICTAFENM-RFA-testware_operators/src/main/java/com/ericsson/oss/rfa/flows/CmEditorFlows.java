package com.ericsson.oss.rfa.flows;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;

import javax.inject.Inject;

import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.oss.rfa.teststeps.CmEditorTestSteps;

public class CmEditorFlows {

    public static final String CM_COMMANDS_DATASOURCE = "cm commands data";

    @Inject
    CmEditorTestSteps cmSteps;

    public TestStepFlow executeSimpleCommands() {
        return flow("Simple CM command execution")
                .addTestStep(annotatedMethod(cmSteps, CmEditorTestSteps.SIMPLE_EXECUTE_COMMAND))
                .withDataSources(dataSource(CM_COMMANDS_DATASOURCE))
                .build();
    }

}
