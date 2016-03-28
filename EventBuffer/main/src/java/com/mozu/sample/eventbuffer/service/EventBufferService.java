package com.mozu.sample.eventbuffer.service;

import java.util.List;

import com.mozu.api.ApiContext;
import com.mozu.sample.eventbuffer.model.EventStatus;
import com.mozu.sample.eventbuffer.model.MozuEvent;

public interface EventBufferService {
    /**
     * Create or update the Event buffer schema in MZDB for the tenant
     * 
     * @param tenantId
     * @throws Exception
     */
    public void installSchema(Integer tenantId) throws Exception;

    /**
     * Delete the event buffer schema in MZDB for the tenant
     * 
     * @param tenantId
     * @throws Exception
     */
    public void deleteSchema(Integer tenantId) throws Exception;

    /**
     * Determines whether we need to add or update and event to
     * 
     * @param event
     * @throws Exception
     */
    void addEvent(ApiContext apiContext, MozuEvent event) throws Exception;

    /**
     * Update the status of the event with the given eventId. .
     * 
     * @param eventId
     * @param eventStatus
     */
    public void updateEventStatus(ApiContext apiContext, String eventId, EventStatus eventStatus);

    /**
     * Update the status of the event with the given eventId. Set a message for
     * the status change.
     * 
     * @param eventId
     * @param eventStatus
     * @param message
     */
    public void updateEventStatus(ApiContext apiContext, String eventId, EventStatus eventStatus,
            String message);

    /**
     * Delete an event from the buffer
     * 
     * @param event
     */
    public void deleteEvent(ApiContext apiContext, String eventId) throws Exception;

    /**
     * Get all pending events in the eventQueue sorted by oldest first.
     * 
     * @param apiContext
     * @return
     */
    public List<MozuEvent> getPendingEvents(ApiContext apiContext);

    /**
     * Get next pending event in the eventQueue this should be the oldest event in the pending state.
     * 
     * @param apiContext - the apiContext needs to contain the tenantID and the SiteId
     * @return
     */
    public MozuEvent getNextPendingEvent (ApiContext apiContext);

    /**
     * Get all pending events in the eventQueue sorted by oldest first.
     * 
     * @param apiContext
     * @return
     */
    public List<MozuEvent> getFailedEvents(ApiContext apiContext);
}
