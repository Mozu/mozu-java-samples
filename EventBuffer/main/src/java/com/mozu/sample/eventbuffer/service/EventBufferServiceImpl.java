/**
 * ***********************************************************************
 * Assembly         : EventBufferServiceImpl.java
 * Author           : bob_hewett
 * Created          : Feb 17, 2015
 *
 * Last Modified By : bob_hewett
 * Last Modified On : Feb 17, 2015
 * ***********************************************************************
 * <copyright file="EventBufferServiceImpl.java" company="Volusion">
 *    Copyright (c) Volusion 2015. All rights reserved.
 * </copyright>
 * <summary></summary>
 *
 */
package com.mozu.sample.eventbuffer.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mozu.api.ApiContext;
import com.mozu.api.MozuApiContext;
import com.mozu.api.contracts.mzdb.EntityList;
import com.mozu.api.contracts.mzdb.IndexedProperty;
import com.mozu.api.resources.platform.EntityListResource;
import com.mozu.base.handlers.EntityHandler;
import com.mozu.base.handlers.EntitySchemaHandler;
import com.mozu.base.models.EntityCollection;
import com.mozu.base.models.EntityScope;
import com.mozu.base.utils.ApplicationUtils;
import com.mozu.sample.eventbuffer.model.EventStatus;
import com.mozu.sample.eventbuffer.model.MozuEvent;

/**
 * @author bob_hewett
 *
 */
@Service
public class EventBufferServiceImpl implements EventBufferService {
    private static final String EVENT_BUFFER_LIST = "lightspeedEventBuffer";
    private static final Logger logger = LoggerFactory.getLogger(EventBufferServiceImpl.class);

    @Autowired
    EntitySchemaHandler entitySchemaHandler;
    
    public EventBufferServiceImpl() {
    }
    
    /* (non-Javadoc)
     * @see com.mozu.lightspeed.service.EventBufferService#installSchema(java.lang.Integer)
     */
    @Override
    public void installSchema(Integer tenantId) throws Exception {
        EntityList entityList = new EntityList();
        entityList.setNameSpace(ApplicationUtils.getAppInfo().getNameSpace());
        entityList.setName(EVENT_BUFFER_LIST);
        entityList.setIsVisibleInStorefront(false);
        entityList.setIsLocaleSpecific(false);
        entityList.setIsSandboxDataCloningSupported(false);
        entityList.setIsShopperSpecific(false);

        IndexedProperty idProperty = getIndexedProperty("eventId", "string");
        
        IndexedProperty statusProperty = getIndexedProperty("status", "string");
        IndexedProperty entityIdProperty = getIndexedProperty("entityId", "string");
        IndexedProperty eventCategory = getIndexedProperty("eventCategory", "string");
    
        List<IndexedProperty> indexedProperties = new ArrayList<IndexedProperty>();
        
        indexedProperties.add(statusProperty);
        indexedProperties.add(entityIdProperty);
        indexedProperties.add(eventCategory);

        entitySchemaHandler.installSchema(new MozuApiContext(tenantId), entityList, EntityScope.Site, idProperty, indexedProperties);
    }

    /* (non-Javadoc)
     * @see com.mozu.lightspeed.service.EventBufferService#deleteSchema(java.lang.Integer)
     */
    @Override
    public void deleteSchema(Integer tenantId) throws Exception {
        EntityListResource entityListResource = new EntityListResource(new MozuApiContext(tenantId));
        entityListResource.deleteEntityList(EVENT_BUFFER_LIST + "@" + ApplicationUtils.getAppInfo().getNameSpace());
    }

    /* (non-Javadoc)
     * @see com.mozu.lightspeed.service.EventBufferService#addEvent(com.mozu.api.contracts.event.Event)
     */
    @Override
    public void addEvent(ApiContext apiContext, MozuEvent lsEvent) throws Exception {
        EntityHandler<MozuEvent> entityHandler = new EntityHandler<MozuEvent>(MozuEvent.class);
        String topic[] = lsEvent.getTopic().split("\\.");
        String eventCategory = topic[0].substring(0, 1).toUpperCase() + topic[0].substring(1);

        String filter = String.format("status eq PENDING and entityId eq %s ", lsEvent.getEntityId());
        EntityCollection<MozuEvent> existingEvent = entityHandler.getEntityCollection(apiContext, EVENT_BUFFER_LIST, filter, null, 0, 10);
        
        lsEvent.setEventCategory(eventCategory);
        lsEvent.setStatus(EventStatus.PENDING.toString());
        
        boolean skipEvent = false;
        if (existingEvent != null && existingEvent.getItems() != null && existingEvent.getItems().size() > 0) {
            // process to see if we already have the event for the entity ID
            for (MozuEvent oldLsEvent : existingEvent.getItems()) {
                String oldTopic = oldLsEvent.getTopic(); 
                // if we find the same topic do not add the event
                if (oldTopic.equals(lsEvent.getTopic())) {
                    skipEvent = true;
                }
                //TODO: do some more processing to skip on following:
                // if update and have an existing create event or delete event
            }
        } 
        
        if (!skipEvent) {
            entityHandler.upsertEntity(apiContext, EVENT_BUFFER_LIST, lsEvent.getEventId(), lsEvent);
        }

    }

    /* (non-Javadoc)
     * @see com.mozu.lightspeed.service.EventBufferService#updateEventStatus(com.mozu.api.contracts.event.Event, java.lang.String)
     */
    @Override
    public void updateEventStatus(ApiContext apiContext, String eventId, EventStatus eventStatus) {
        this.updateEventStatus(apiContext, eventId, eventStatus, null);
    }
    
    @Override
    public void updateEventStatus(ApiContext apiContext, String eventId, EventStatus eventStatus, String message) {
        EntityHandler<MozuEvent> entityHandler = new EntityHandler<MozuEvent>(MozuEvent.class);
        
        try {
            MozuEvent event = entityHandler.getEntity(apiContext, EVENT_BUFFER_LIST, eventId);
            event.setStatus(eventStatus.toString());
            event.setMessage(message);
            entityHandler.upsertEntity(apiContext, EVENT_BUFFER_LIST, eventId, event);
        } catch (Exception e) {
            logger.error("Unable to save event status for event ID: " + eventId);
        }

    }

    /* (non-Javadoc)
     * @see com.mozu.lightspeed.service.EventBufferService#deleteEvent(com.mozu.api.contracts.event.Event)
     */
    @Override
    public void deleteEvent(ApiContext apiContext, String eventId) throws Exception {
        EntityHandler<MozuEvent> entityResource = new EntityHandler<MozuEvent>(MozuEvent.class);
        entityResource.deleteEntity(apiContext, EVENT_BUFFER_LIST, eventId);

    }

    @Override
    public List<MozuEvent> getPendingEvents (ApiContext apiContext) {
        EntityHandler<MozuEvent> entityHandler = new EntityHandler<MozuEvent>(MozuEvent.class);
        List<MozuEvent> events = null;
        String filterCriteria = String.format("status eq %s", EventStatus.PENDING.toString());
        try {
            EntityCollection<MozuEvent> pendingEvents = entityHandler.getEntityCollection(apiContext, EVENT_BUFFER_LIST, filterCriteria, null, 0, 200);
            events = pendingEvents.getItems();
        } catch (Exception e) {
            logger.error("Unable to get events from the event buffer in Mozu: " + e.getMessage());
        }
        
        return events;
    }
    
    @Override
    public MozuEvent getNextPendingEvent(ApiContext apiContext) {
        EntityHandler<MozuEvent> entityHandler = new EntityHandler<MozuEvent>(MozuEvent.class);
        String filterCriteria = String.format("status eq %s", EventStatus.PENDING.toString());
        MozuEvent nextEvent = null;
        try {
            // get on
            EntityCollection<MozuEvent> pendingEvents = entityHandler.getEntityCollection(apiContext,EVENT_BUFFER_LIST, filterCriteria, null, 0, 1);
            if (pendingEvents != null  && pendingEvents.getItems() != null && pendingEvents.getItems().size() == 1) {
                nextEvent = pendingEvents.getItems().get(0);
            }
        } catch (Exception e) {
            logger.error("Unable to get events from the event buffer in Mozu: " + e.getMessage());
        }
        
        return nextEvent;
    };
    
    @Override
    public List<MozuEvent> getFailedEvents (ApiContext apiContext) {
        EntityHandler<MozuEvent> entityHandler = new EntityHandler<MozuEvent>(MozuEvent.class);
        List<MozuEvent> events = null;
        String filterCriteria = String.format("status eq %s", EventStatus.FAILED.toString());
        try {
            EntityCollection<MozuEvent> failedEvents = entityHandler.getEntityCollection(apiContext, EVENT_BUFFER_LIST, filterCriteria, null, 0, 200);
            events = failedEvents.getItems();
        } catch (Exception e) {
            logger.error("Unable to get events from the event buffer in Mozu: " + e.getMessage());
        }
        
        return events;
    }
    
    protected IndexedProperty getIndexedProperty(String name, String type) {
        IndexedProperty property = new IndexedProperty();
        property.setPropertyName(name);
        property.setDataType(type);
            
        return property;
    }
}
