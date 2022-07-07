package org.modality_project.ecommerce.backoffice.activities.moneyflows.routing;

import org.modality_project.base.client.util.routing.MongooseRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class MoneyFlowsRouting {

    private final static String PATH = "/money-flows/organization/:organizationId";

    public static String getPath() {
        return PATH;
    }

    public static String getOrganizationIncomePath(Object organizationId) {
        return MongooseRoutingUtil.interpolateEventIdInPath(organizationId, PATH);
    }

}
