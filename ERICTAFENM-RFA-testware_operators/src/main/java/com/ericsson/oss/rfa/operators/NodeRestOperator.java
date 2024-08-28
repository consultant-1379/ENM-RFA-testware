package com.ericsson.oss.rfa.operators;

import static com.ericsson.oss.mediation.cm.operator.api.common.constants.Constants.COMMA;
import static com.ericsson.oss.mediation.cm.operator.api.common.constants.Constants.EMPTY_STRING;
import static com.ericsson.oss.mediation.cm.operator.api.common.constants.Constants.EQUALS;
import static com.ericsson.oss.mediation.cm.operator.api.common.constants.Constants.SPACE;
import static com.ericsson.oss.mediation.cm.operator.api.lte.constants.LteConstants.CPPCONNECTIVITYINFORMATION_ATTRS;
import static com.ericsson.oss.mediation.cm.operator.api.lte.constants.LteConstants.CPPCONNECTIVITYINFORMATION_RDN;
import static com.ericsson.oss.mediation.cm.operator.api.lte.constants.LteConstants.CPPCONNINFO_MODEL_TYPE;
import static com.ericsson.oss.mediation.cm.operator.api.lte.constants.LteConstants.CPPCONNINFO_MODEL_VERSION;
import static com.ericsson.oss.mediation.cm.operator.api.lte.constants.LteConstants.MECONTEXT;
import static com.ericsson.oss.mediation.cm.operator.api.lte.constants.LteConstants.MECONTEXTID;
import static com.ericsson.oss.mediation.cm.operator.api.lte.constants.LteConstants.MECONTEXT_ATTRS;
import static com.ericsson.oss.mediation.cm.operator.api.lte.constants.LteConstants.NETWORKELEMENT;
import static com.ericsson.oss.mediation.cm.operator.api.lte.constants.LteConstants.NETWORKELEMENTID;
import static com.ericsson.oss.mediation.cm.operator.api.lte.constants.LteConstants.NETWORKELEMENT_ATTRS;
import static com.ericsson.oss.mediation.cm.operator.api.lte.constants.LteConstants.OSS_NE_DEF_MODEL_TYPE;
import static com.ericsson.oss.mediation.cm.operator.api.lte.constants.LteConstants.OSS_NE_DEF_MODEL_VERSION;
import static com.ericsson.oss.mediation.cm.operator.api.lte.constants.LteConstants.OSS_TOP_MODEL_TYPE;
import static com.ericsson.oss.mediation.cm.operator.api.lte.constants.LteConstants.OSS_TOP_MODEL_VERSION;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.annotations.VUserScoped;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.enm.data.NetworkNode;
import com.ericsson.oss.mediation.cm.operator.util.CmEditorCommandUtil;
import com.ericsson.oss.services.cm.rest.CmEditorRestOperator;
import com.ericsson.oss.services.scriptengine.spi.dtos.AbstractDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseDto;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

@Operator(context = { Context.REST, Context.UNKNOWN })
@VUserScoped
public class NodeRestOperator {

    private static final Logger log = LoggerFactory.getLogger(NodeRestOperator.class);
    @Inject
    OperatorRegistry<CmEditorRestOperator> provider;

    CmEditorRestOperator cmEditorRestOperator;

    CmEditorRestOperator getCmEditorRestOperator() {
        if (cmEditorRestOperator == null) {
            cmEditorRestOperator = provider.provide(CmEditorRestOperator.class);
        }
        return cmEditorRestOperator;
    }

    public void setTool(HttpTool tool) {
        getCmEditorRestOperator().setTool(tool);
    }

    /**
     * Add Node
     * 
     * @param node
     * @return
     */
    public ResponseDto addNode(NetworkNode node) {
        List<AbstractDto> result = Lists.newArrayList();
        result.add(createMeContextMo(node));
        result.add(createNetworkElementMo(node));
        result.add(createCppConnectivityInformationMo(node));
        return new ResponseDto(result);
    }

    /**
     * Delete Node
     * 
     * @param node
     * @return
     */
    public ResponseDto removeNode(NetworkNode node) {
        List<AbstractDto> result = Lists.newArrayList();
        result.add(deleteMeContextMo(node));
        result.add(deleteNetworkElementMo(node));
        return new ResponseDto(result);
    }

    /**
     *
     * @param node
     * @return
     */
    public ResponseDto createSecurityCredentials(final NetworkNode node) {
        final String createSecurityCredentials = createCredentials(node);
        log.info("Running sec command {} ", createSecurityCredentials);
        return getCmEditorRestOperator().executeCommand(createSecurityCredentials);
    }

    // Add Node Methods
    private ResponseDto createMeContextMo(final NetworkNode node) {
        final String meContextAttributes = MECONTEXTID + EQUALS + node.getNetworkElementId() + MECONTEXT_ATTRS;
        final String meContextFdn = createMeContextFdnFromOssPrefix(getOssPrefix(node), node.getNetworkElementId());
        return getCmEditorRestOperator().executeCommand(createManagedObject(meContextFdn,
                meContextAttributes, OSS_TOP_MODEL_TYPE, OSS_TOP_MODEL_VERSION));
    }

    private ResponseDto createNetworkElementMo(final NetworkNode node) {
        final String networkElementFdn = NETWORKELEMENT + EQUALS + node.getNetworkElementId();
        final String networkElementAttributes = getNodeAttributes(node);
        return getCmEditorRestOperator().executeCommand(createManagedObject(networkElementFdn, networkElementAttributes, OSS_NE_DEF_MODEL_TYPE, OSS_NE_DEF_MODEL_VERSION));
    }

    private ResponseDto createCppConnectivityInformationMo(final NetworkNode node) {
        final String cppConnInfoFdn = getNetworkElementFdn(node) + COMMA + CPPCONNECTIVITYINFORMATION_RDN;
        final String cppConnInfoAttrs = CPPCONNECTIVITYINFORMATION_ATTRS + node.getIpAddress() + "\"";
        return getCmEditorRestOperator().executeCommand(createManagedObject(cppConnInfoFdn, cppConnInfoAttrs,
                CPPCONNINFO_MODEL_TYPE, CPPCONNINFO_MODEL_VERSION));
    }

    protected String createManagedObject(final String fdn, final String attributes,
            final String model, final String version) {
        return CmEditorCommandUtil.createMo(fdn, attributes, model, version);
    }

    // Delete Node Methods
    private ResponseDto deleteMeContextMo(final NetworkNode node) {
        final String meContextFdn = MECONTEXT + EQUALS + node.getNetworkElementId();
        return getCmEditorRestOperator().executeCommand(deleteManagedObject(meContextFdn));
    }

    private ResponseDto deleteNetworkElementMo(final NetworkNode node) {
        final String networkElementFdn = NETWORKELEMENT + EQUALS + node.getNetworkElementId();
        return getCmEditorRestOperator().executeCommand(deleteManagedObject(networkElementFdn));
    }

    private String deleteManagedObject(final String fdn) {
        return CmEditorCommandUtil.deleteMo(fdn);
    }

    // Create Security Credentials Methods
    private String createCredentials(final NetworkNode node) {
        final StringBuilder securityCredentials = new StringBuilder();
        securityCredentials.append("secadm credentials create").append(SPACE)
                .append(" --rootusername").append(SPACE).append((node.getRootUserName() == null) ? "root" : node.getRootUserName())
                .append(" --rootuserpassword").append(SPACE)
                .append((node.getRootUserPassword() == null) ? "shroot" : node.getRootUserPassword()).append(" --secureusername")
                .append(SPACE).append((node.getSecureUserName() == null) ? "secadm" : node.getSecureUserName())
                .append(" --secureuserpassword").append(SPACE)
                .append((node.getSecureUserPassword() == null) ? "secpass" : node.getSecureUserPassword()).append(" --normalusername")
                .append(SPACE).append((node.getNormalUserName() == null) ? "admin" : node.getNormalUserName())
                .append(" --normaluserpassword").append(SPACE)
                .append((node.getNormalUserPassword() == null) ? "adminpass" : node.getNormalUserPassword()).append(" -n").append(SPACE)
                .append(node.getNetworkElementId());

        return securityCredentials.toString();

    }

    // OSS Prefix
    private String getOssPrefix(final NetworkNode node) {
        return node.getOssPrefix() == null ? EMPTY_STRING : node.getOssPrefix();
    }

    private String createMeContextFdnFromOssPrefix(String ossPrefix, final String networkElementId) {
        String meContextFdn;
        if (ossPrefix == null || ossPrefix.isEmpty()) {
            meContextFdn = MECONTEXT + EQUALS + networkElementId;
        } else if (ossPrefix.contains(MECONTEXT)) {
            ossPrefix = removeTrailingCommaIfNecessary(ossPrefix);
            meContextFdn = ossPrefix;
        } else {
            meContextFdn = ossPrefix + COMMA + MECONTEXT + EQUALS + networkElementId;
        }
        return meContextFdn;
    }

    private String removeTrailingCommaIfNecessary(String ossPrefix) {
        if (ossPrefix.endsWith(COMMA)) {
            ossPrefix = ossPrefix.substring(0, ossPrefix.lastIndexOf(COMMA));
        }
        return ossPrefix;
    }

    // Get node elements
    private String getNetworkElementFdn(final NetworkNode node) {
        return NETWORKELEMENT + EQUALS + node.getNetworkElementId();
    }

    private String getNodeAttributes(final NetworkNode node) {
        String networkElementAttributes = NETWORKELEMENTID + EQUALS + node.getNetworkElementId() + NETWORKELEMENT_ATTRS + getOssPrefix(node) + "\"";
        if (node.getMimInfo() != null && !node.getMimInfo().isEmpty())
            networkElementAttributes = networkElementAttributes + COMMA + "neVersion=\"" + node.getMimInfo() + "\"";
        return networkElementAttributes;
    }
}
