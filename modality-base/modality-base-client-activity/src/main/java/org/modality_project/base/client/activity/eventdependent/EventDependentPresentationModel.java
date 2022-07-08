package org.modality_project.base.client.activity.eventdependent;

import org.modality_project.base.client.activity.organizationdependent.OrganizationDependentPresentationModel;
import org.modality_project.base.client.presentationmodel.HasEventIdProperty;

/**
 * @author Bruno Salmon
 */
public interface EventDependentPresentationModel
        extends OrganizationDependentPresentationModel,
        HasEventIdProperty {

}
