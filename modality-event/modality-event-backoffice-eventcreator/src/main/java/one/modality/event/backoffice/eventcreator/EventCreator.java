package one.modality.event.backoffice.eventcreator;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.windowhistory.WindowHistory;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.UpdateStore;
import one.modality.base.shared.entities.*;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.event.backoffice.activities.pricing.EventPricingRouting;
import one.modality.event.client.event.fx.FXEventId;

import java.time.LocalDate;

/**
 * @author Bruno Salmon
 */
public final class EventCreator {

    public static Future<Void> createEvent(String eventName, Object eventType, Site venue, LocalDate startDate, LocalDate endDate, UpdateStore updateStore) {
        Organization organization = FXOrganization.getOrganization();
        // Creating the festival event
        Event event = updateStore.insertEntity(Event.class);
        // For now (2025) we do only Online Festivals with KBS3 (in 2026 the same event will be for both in-person & online)
        event.setName(eventName);
        event.setOrganization(organization);
        event.setCorporation(1); // TODO: remove this from database
        event.setType(eventType);
        event.setStartDate(startDate);
        event.setEndDate(endDate);
        event.setKbs3(true);
        event.setTeachingsDayTicket(true);
        event.setAudioRecordingsDayTicket(true);
        if (venue.isNew())
            venue.setEvent(event);
        // Bookable scheduled items
        EntityId teachingDayTicketItemId = organization.getTeachingsDayTicketItemId();  // Should be Festival for NKT
        for (LocalDate date = event.getStartDate(); !date.isAfter(event.getEndDate()); date = date.plusDays(1)) {
            ScheduledItem si = updateStore.insertEntity(ScheduledItem.class);
            si.setEvent(event);
            si.setSite(venue);
            si.setItem(teachingDayTicketItemId);
            si.setDate(date);
        }
        return updateStore.submitChanges()
            // Setting venue afterwards TODO: Improve EntityChangesToSubmitBatchGenerator to solve cyclic references
            .compose(ignored -> {
                event.setVenue(venue);
                return updateStore.submitChanges();
            })
            .onFailure(Console::error)
            .onSuccess(ignored -> {
                // Automatically selecting this new event
                EntityId eventId = event.getId();
                FXEventId.setEventId(eventId);
                // Automatically routing to the pricing activity
                new EventPricingRouting.RouteToEventPricingRequest(eventId, WindowHistory.getProvider()).execute();
            })
            .mapEmpty();
    }

    public static Site insertNewVenue(String venueName, boolean createAudioRecordingSiteItemFamily, UpdateStore updateStore) {
        // Main site
        Site site = updateStore.insertEntity(Site.class);
        site.setName(venueName);
        site.setOrganization(FXOrganization.getOrganization());
        site.setItemFamily(KnownItemFamily.TEACHING.getPrimaryKey());
        site.setMain(true);
        site.setOrd(10);
        //event.setVenue(site); // cyclic reference issue => postponed below
        // Creating SiteItemFamily for teachings & audio recordings (so we can see the rates in KBS2
        // back-office, but probably not necessary for KBS3).
        SiteItemFamily sif = updateStore.insertEntity(SiteItemFamily.class);
        sif.setSite(site);
        sif.setItemFamily(KnownItemFamily.TEACHING.getPrimaryKey());
        if (createAudioRecordingSiteItemFamily) {
            sif = updateStore.insertEntity(SiteItemFamily.class);
            sif.setSite(site);
            sif.setItemFamily(KnownItemFamily.AUDIO_RECORDING.getPrimaryKey());
        }
        return site;
    }

}
