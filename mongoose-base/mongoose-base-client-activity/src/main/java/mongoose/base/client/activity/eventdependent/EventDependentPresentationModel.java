package mongoose.base.client.activity.eventdependent;

import mongoose.base.client.presentationmodel.HasOrganizationIdProperty;
import mongoose.base.client.presentationmodel.HasEventIdProperty;

/**
 * @author Bruno Salmon
 */
public interface EventDependentPresentationModel
        extends HasEventIdProperty,
        HasOrganizationIdProperty {

}
