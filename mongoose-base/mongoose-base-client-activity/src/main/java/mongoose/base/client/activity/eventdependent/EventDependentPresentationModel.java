package mongoose.base.client.activity.eventdependent;

import mongoose.base.client.activity.organizationdependent.OrganizationDependentPresentationModel;
import mongoose.base.client.presentationmodel.HasEventIdProperty;

/**
 * @author Bruno Salmon
 */
public interface EventDependentPresentationModel
        extends OrganizationDependentPresentationModel,
        HasEventIdProperty {

}
