package com.mozu.sample.eventbuffer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mozu.api.ApiContext;
import com.mozu.api.contracts.event.Event;
import com.mozu.api.events.service.EventService;
import com.mozu.sample.eventbuffer.model.EventStatus;
import com.mozu.sample.eventbuffer.model.MozuEvent;

/**
 * A class that handles firing the Mozu events stored in the EventBuffer in MZDB 
 * @author bob_hewett
 *
 */
@Service
public class DispatchEventService extends EventService {
    private static final Logger logger = LoggerFactory.getLogger(DispatchEventService.class);

    @Autowired
    EventBufferService eventBufferService;
    
    /**
     * Dispatches the pending events in MZDB to the mozu event handler.
     * @param apiContext
     */
    public void dispatchPendingEvents (ApiContext apiContext) {
        MozuEvent currentEvent = null;
        do {
           currentEvent = eventBufferService.getNextPendingEvent(apiContext);
           if (currentEvent != null) {
               Event mozuEvent = convertToMozuEvent(currentEvent);
               String message = String.format("Processing the following event: {eventId: %s, correlationId: %s, tenantId: %s, entityId: %s, topic %s", 
                       currentEvent.getEventId(), currentEvent.getCorrelationId(), apiContext.getTenantId(), currentEvent.getEntityId(), currentEvent.getTopic());
               logger.info(message);
               eventBufferService.updateEventStatus(apiContext, currentEvent.getEventId(), EventStatus.PROCESSING);
               try {
                   invokeHandler(mozuEvent, apiContext);
                   eventBufferService.updateEventStatus(apiContext, currentEvent.getEventId(), EventStatus.PROCESSED);
                   message = String.format("The following event was processed successfully: {eventId: %s, correlationId: %s, tenantId: %s, entityId: %s, topic %s}", 
                           currentEvent.getEventId(), currentEvent.getCorrelationId(), apiContext.getTenantId(), currentEvent.getEntityId(), currentEvent.getTopic());
                   logger.info(message);
               } catch (Exception e) {
                   eventBufferService.updateEventStatus(apiContext, currentEvent.getEventId(), EventStatus.FAILED);
                   message = String.format("The following event failed to be processed: {eventId: %s, correlationId: %s, tenantId: %s, entityId: %s, topic %s}: %s", 
                           currentEvent.getEventId(), currentEvent.getCorrelationId(), apiContext.getTenantId(), currentEvent.getEntityId(), currentEvent.getTopic(), e.getMessage());
                   logger.info(message);
               }
           }
        } while (currentEvent != null);
       
    }
    
    private Event convertToMozuEvent(MozuEvent lsEvent) {
        Event event = new Event ();
        
        event.setCorrelationId(lsEvent.getCorrelationId());
        event.setEntityId(lsEvent.getEntityId());
        event.setExtendedProperties(lsEvent.getExtendedProperties());
        event.setId(lsEvent.getEventId());
        event.setIsTest(lsEvent.getIsTest());
        event.setTopic(lsEvent.getTopic());
        
        return event;
        
    }
}
