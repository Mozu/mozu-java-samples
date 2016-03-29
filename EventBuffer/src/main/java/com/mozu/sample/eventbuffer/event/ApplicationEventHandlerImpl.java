package com.mozu.sample.eventbuffer.event;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mozu.api.ApiContext;
import com.mozu.api.contracts.event.Event;
import com.mozu.api.events.EventManager;
import com.mozu.api.events.handlers.ApplicationEventHandler;
import com.mozu.api.events.model.EventHandlerStatus;
import com.mozu.base.utils.ApplicationUtils;
import com.mozu.sample.eventbuffer.service.EventBufferService;
import com.mozu.sample.eventbuffer.service.TimerService;

@Component
public class ApplicationEventHandlerImpl implements ApplicationEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationEventHandlerImpl.class);

    @Autowired 
    EventBufferService eventBufferService;
    
    @Autowired
    TimerService timerService;

    @PostConstruct
    public void initialize() {
        EventManager.getInstance().registerHandler(this);
        logger.info("Application event handler initialized");
    }

    @Override
    public EventHandlerStatus disabled(ApiContext apiContext, Event event) {
        try {
            timerService.stopEventPolling(apiContext);
            logger.info("Scheduled job for sales import removed");
        } catch (Exception e) {
        	logger.info("Exception disabling sales import job: " + e.getMessage());
        }

        return new EventHandlerStatus(HttpStatus.SC_OK);
    }

    @Override
    public EventHandlerStatus enabled(ApiContext apiContext, Event event) {
        EventHandlerStatus status = new EventHandlerStatus(HttpStatus.SC_OK);
        timerService.startEventPolling(apiContext);
    
        return status;
    }

    @Override
    public EventHandlerStatus installed(ApiContext apiContext, Event event) {
        logger.info("Application installed event");
        EventHandlerStatus status;
        
        try {
            // enable the application
            ApplicationUtils.setApplicationToInitialized(apiContext);
            logger.info ("Application Initialized for tenant" + apiContext.getTenantId());
            
            // install the event buffer schema
            eventBufferService.installSchema(apiContext.getTenantId());
            logger.info ("Event Log schema installed for tenant id: " + apiContext.getTenantId());
            status = new EventHandlerStatus(HttpStatus.SC_OK);
        } catch (Exception e) {
            logger.error("Exception during installation: " + e.getMessage());
            status = new EventHandlerStatus(e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        return status;
    }

    @Override
    public EventHandlerStatus uninstalled(ApiContext apiContext, Event event) {
        try {
            eventBufferService.deleteSchema(apiContext.getTenantId());
            // product is uninstalled delete the Timer job 
            try {
            	timerService.stopEventPolling(apiContext);
                logger.info("Scheduled job for sales import removed");
            } catch (Exception e) {
            	logger.info("Exception disabling sales import job: " + e.getMessage());
            }
        } catch (Exception e) {
            logger.error("Exception during uninstall: " + e.getMessage());
        }

        return disabled(apiContext, event);
    }

    @Override
    public EventHandlerStatus upgraded(ApiContext apiContext, Event event) {
        logger.info("Application upgraded event");
        EventHandlerStatus status =new EventHandlerStatus(HttpStatus.SC_OK);
        try {
            eventBufferService.installSchema(apiContext.getTenantId());
            logger.info ("Event Log schema updated for tenant id: " + apiContext.getTenantId());
        } catch (Exception e) {
            logger.error("Exception during applicaiton upgrade: " + e.getMessage());
            status = new EventHandlerStatus(e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        return status;
    }
    
    @PreDestroy
    public void cleanup() {
        EventManager.getInstance().unregisterHandler(this.getClass());
        logger.info("Application event handler unregistered");
    }
    
}
