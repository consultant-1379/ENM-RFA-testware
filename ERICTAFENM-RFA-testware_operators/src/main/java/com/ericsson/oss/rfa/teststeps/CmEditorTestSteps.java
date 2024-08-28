package com.ericsson.oss.rfa.teststeps;

import static com.ericsson.cifwk.taf.assertions.TafAsserts.assertThat;
import static org.hamcrest.Matchers.is;

import javax.inject.Inject;

import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.Output;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.enm.UserSession;
import com.ericsson.oss.rfa.data.metric.Metric.MetricDataBuilder;
import com.ericsson.oss.services.cm.rest.CmEditorRestOperator;
import com.google.common.base.Preconditions;

public class CmEditorTestSteps {

    @Inject
    private OperatorRegistry<CmEditorRestOperator> cmEditorRegistry;

    @Inject
    TestContext context;

    public static final String SIMPLE_EXECUTE_COMMAND = "execute command";

    @TestStep(id = SIMPLE_EXECUTE_COMMAND)
    public void executeCommand(@Input("command") String command, @Output("commandSuccess") boolean result) {
        UserSession session = context.getAttribute(UserSession.SESSION);
        Preconditions.checkState(session != null, String.format("User session with looged in tool is required for step %s", SIMPLE_EXECUTE_COMMAND));
        getCmEditor().setTool(session.getTool().get());
        MetricDataBuilder.builder().data("query", command).report();
        assertThat(getCmEditor().commandExecuteWithSuccessOrFail(command), is(result));

    }

    private CmEditorRestOperator getCmEditor() {
        return cmEditorRegistry.provide(CmEditorRestOperator.class);
    }
}
