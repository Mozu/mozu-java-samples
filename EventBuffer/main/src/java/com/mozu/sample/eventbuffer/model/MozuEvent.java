package com.mozu.sample.eventbuffer.model;

import java.util.List;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mozu.api.contracts.event.EventExtendedProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MozuEvent {
    @SuppressWarnings("unused")
    private static final long serialVersionUID = 1L;

    /**
     * Unique id for the event 
     */
    protected String eventId;

    public String getEventId() {
        return this.eventId;
    }

    public void setEventId(String id) {
        this.eventId = id;
    }
    /**
     * The unique identifier of the API request associated with the event action, which might contain multiple actions.
     */
    protected String correlationId;

    public String getCorrelationId() {
        return this.correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    /**
     * The unique identifier of the entity that caused the event. For example, if the event is "product.created", the entity ID value represents the product code of the product that was created.
     */
    protected String entityId;

    public String getEntityId() {
        return this.entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    /**
     * Indicates if the event is a test request or test entity. If true, the generated and captured event record was generated as a test request for an application.
     */
    protected Boolean isTest;

    public Boolean getIsTest() {
        return this.isTest;
    }

    public void setIsTest(Boolean isTest) {
        this.isTest = isTest;
    }

    /**
     * The type of event that was performed, such as "product.created" or "category.deleted".
     */
    protected String topic;

    public String getTopic() {
        return this.topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * Extended properties. Note: This is purposefully not a CollectionBase type wrapper so consumers start to get used to not having counts returned.
     */
    protected List<EventExtendedProperty> extendedProperties;
    public List<EventExtendedProperty> getExtendedProperties() {
        return this.extendedProperties;
    }
    public void setExtendedProperties(List<EventExtendedProperty> extendedProperties) {
        this.extendedProperties = extendedProperties;
    }
    
    /**
     * Status of the event in Lightspeed Event Queue
     */
    String status = null;
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    String message = null;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    String eventCategory = null;

    public String getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(String eventCategory) {
        this.eventCategory = eventCategory;
    }
    

    DateTime timestamp;

    public DateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }
    
}
