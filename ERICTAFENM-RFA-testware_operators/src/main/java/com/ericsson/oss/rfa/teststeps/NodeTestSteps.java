package com.ericsson.oss.rfa.teststeps;

import static com.ericsson.cifwk.taf.assertions.TafAsserts.assertThat;
import static com.ericsson.enm.data.CommonDataSources.ADDED_NODES;
import static com.ericsson.enm.data.CommonDataSources.NODES_TO_ADD;
import static com.ericsson.oss.services.cm.matchers.ResponseDtoMatcher.hasError;
import static org.hamcrest.core.IsNot.not;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.enm.UserSession;
import com.ericsson.enm.data.NetworkNode;
import com.ericsson.oss.rfa.operators.NodeRestOperator;
import com.google.inject.Inject;

public class NodeTestSteps {

    private static final Logger log = LoggerFactory.getLogger(NodeTestSteps.class);
    public static final String ADD_NODE = "add node";
    public static final String DELETE_NODE = "delete node";
    public static final String ADD_SECURITY_TO_NODE = "add node security";

    @Inject
    OperatorRegistry<NodeRestOperator> provider;

    NodeRestOperator nodeOperator;

    private NodeRestOperator getNodeOperator() {
        if (nodeOperator == null) {
            nodeOperator = provider.provide(NodeRestOperator.class);
        }
        return nodeOperator;
    }

    @Inject
    TestContext context;

    @TestStep(id = ADD_NODE)
    public void addNode(@Input(NODES_TO_ADD) NetworkNode node) {
        setTool();
        log.info("Adding node {} ", node);
        assertThat(getNodeOperator().addNode(node), not(hasError()));
        log.info("Node added");
        context.dataSource(ADDED_NODES).addRecord().setFields(node);
    }

    @TestStep(id = DELETE_NODE)
    public void deleteNode(@Input(ADDED_NODES) NetworkNode node) {
        setTool();
        assertThat(getNodeOperator().removeNode(node), not(hasError()));
    }

    @TestStep(id = ADD_SECURITY_TO_NODE)
    public void createSecurityCredentials(@Input(ADDED_NODES) NetworkNode node) {
        log.info("Node {} ", node);
        setTool();
        assertThat(getNodeOperator().createSecurityCredentials(node), not(hasError()));
    }

    private void setTool() {
        UserSession session = context.getAttribute(UserSession.SESSION);
        getNodeOperator().setTool(session.getTool().getAs(HttpTool.class));
    }
}
