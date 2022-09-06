package one.modality.base.client.activity.eventdependent;

import one.modality.base.client.activity.organizationdependent.OrganizationDependentPresentationModel;
import one.modality.base.client.presentationmodel.HasEventIdProperty;

/**
 * @author Bruno Salmon
 */
public interface EventDependentPresentationModel
        extends OrganizationDependentPresentationModel,
        HasEventIdProperty {

}
