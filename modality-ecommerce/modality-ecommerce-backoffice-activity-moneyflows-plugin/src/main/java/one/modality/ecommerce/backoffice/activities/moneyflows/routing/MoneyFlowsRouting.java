package one.modality.ecommerce.backoffice.activities.moneyflows.routing;

import one.modality.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class MoneyFlowsRouting {

    private final static String ANY_PATH = "/money-flows(/organization/:organizationId)?";

    private final static String ORGANIZATION_PATH = "/money-flows/organization/:organizationId";

    public static String getAnyPath() {
        return ANY_PATH;
    }

    public static String getOrganizationIncomePath(Object organizationId) {
        return organizationId == null ? "/money-flows" : ModalityRoutingUtil.interpolateOrganizationIdInPath(organizationId, ORGANIZATION_PATH);
    }

}
