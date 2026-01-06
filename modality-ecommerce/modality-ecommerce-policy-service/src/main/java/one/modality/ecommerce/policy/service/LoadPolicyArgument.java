package one.modality.ecommerce.policy.service;

import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Organization;

import java.time.LocalDate;

/**
 * @author Bruno Salmon
 */
public final class LoadPolicyArgument {

    private final Object organizationPk;
    private final Object eventPk;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public LoadPolicyArgument(Event event) {
        this(event.getPrimaryKey());
    }

    public LoadPolicyArgument(Object eventPk) {
        this(null, eventPk, null, null);
    }

    public LoadPolicyArgument(Organization organization, LocalDate startDate, LocalDate endDate) {
        this(organization.getPrimaryKey(), startDate, endDate);
    }

    public LoadPolicyArgument(Object organizationPk, LocalDate startDate, LocalDate endDate) {
        this(organizationPk, null, startDate, endDate);
    }

    public LoadPolicyArgument(Object organizationPk, Object eventPk, LocalDate startDate, LocalDate endDate) {
        this.endDate = endDate;
        this.eventPk = eventPk;
        this.organizationPk = organizationPk;
        this.startDate = startDate;
    }

    public Object getOrganizationPk() {
        return organizationPk;
    }

    public Object getEventPk() {
        return eventPk;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }
}
