/**
 * ***********************************************************************
 * Assembly         : ProductEventHandler.java
 * Author           : bob_hewett
 * Created          : Aug 12, 2014
 *
 * Last Modified By : bob_hewett
 * Last Modified On : Aug 12, 2014
 * ***********************************************************************
 * <copyright file="FacetTests.cs" company="Volusion">
 *    Copyright (c) Volusion 2013. All rights reserved.
 * </copyright>
 * <summary></summary>
 *
 */
package com.mozu.sample.eventbuffer.event;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mozu.api.ApiContext;
import com.mozu.api.contracts.event.Event;
import com.mozu.api.events.EventManager;
import com.mozu.api.events.handlers.ProductEventHandler;
import com.mozu.api.events.model.EventHandlerStatus;

/**
 * @author bob_hewett
 * 
 */
@Component
public class ProductEventHandlerImpl implements ProductEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(ProductEventHandlerImpl.class);
    public ProductEventHandlerImpl() {

    }

    @PostConstruct
    public void initialize() {
        EventManager.getInstance().registerHandler(this);
        logger.info("Product event handler initialized");
    }

    @Override
    public EventHandlerStatus created(ApiContext apiContext, Event event) {
        logger.info("Product Created event handled");
        return new EventHandlerStatus(HttpStatus.SC_OK);
    }

    @Override
    public EventHandlerStatus deleted(ApiContext apiContext, Event event) {
        logger.info("Product Deleted event handled");
        return new EventHandlerStatus(HttpStatus.SC_OK);
    }

    @Override
    public EventHandlerStatus updated(ApiContext apiContext, Event event) {
        logger.info("Product Updated event handled");
        return new EventHandlerStatus(HttpStatus.SC_OK);
    }

    @Override
    public EventHandlerStatus coderenamed(ApiContext apiContext, Event event) {
        logger.info("Code Renamed Event - Not Implemented");
        return new EventHandlerStatus(HttpStatus.SC_OK);
    }

    @PreDestroy
    public void cleanup() {
        EventManager.getInstance().unregisterHandler(this.getClass());
        logger.debug("Product event handler unregistered");
    }

}
