package com.mozu.sample.eventbuffer.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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
import com.mozu.sample.eventbuffer.service.DispatchEventService;
import com.mozu.sample.eventbuffer.service.EventBufferService;

@Component
public class ApplicationEventHandlerImpl implements ApplicationEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationEventHandlerImpl.class);

    @Autowired 
    EventBufferService eventBufferService;

    @Autowired 
    DispatchEventService dispatchBufferService;

    Map <Integer, Timer> timerMap = new HashMap<> ();
    
    @PostConstruct
    public void initialize() {
        EventManager.getInstance().registerHandler(this);
        logger.info("Application event handler initialized");
    }

    @Override
    public EventHandlerStatus disabled(ApiContext apiContext, Event event) {
        try {
        	delJob(apiContext);
            logger.debug("Scheduled job for sales import removed");
        } catch (Exception e) {
        	logger.info("Exception disabling sales import job: " + e.getMessage());
        }

        return new EventHandlerStatus(HttpStatus.SC_OK);
    }

    @Override
    public EventHandlerStatus enabled(ApiContext apiContext, Event event) {
        EventHandlerStatus status = new EventHandlerStatus(HttpStatus.SC_OK);

         this.startEventPolling(apiContext);
            
        return status;
    }

    @Override
    public EventHandlerStatus installed(ApiContext apiContext, Event event) {
        logger.debug("Application installed event");
        EventHandlerStatus status;
        try {
            eventBufferService.installSchema(apiContext.getTenantId());
            
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
            // product is uninstalld? delete the QRTZ job 
            try {
            	delJob(apiContext);
                logger.debug("Scheduled job for sales import removed");
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
        logger.debug("Application upgraded event");
        EventHandlerStatus status =new EventHandlerStatus(HttpStatus.SC_OK);
        try {
            eventBufferService.installSchema(apiContext.getTenantId());
        } catch (Exception e) {
            logger.error("Exception during applicaiton upgrade: " + e.getMessage());
            status = new EventHandlerStatus(e.getMessage(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        return status;
    }
    
    @PreDestroy
    public void cleanup() {
        EventManager.getInstance().unregisterHandler(this.getClass());
        logger.debug("Application event handler unregistered");
    }
    
    protected void startEventPolling (ApiContext apiContext) {
        TimerTask eventTimerTask = new EventTimerTask(apiContext, eventBufferService, dispatchBufferService);
        Timer timer = new Timer(true);
        // run job every 30 seconds.
        timer.scheduleAtFixedRate(eventTimerTask, 0, 30 * 1000);
        timerMap.put(apiContext.getTenantId(), timer);
    }
    
    protected void delJob (ApiContext apiContext) {
        Timer timer = timerMap.get(apiContext.getTenantId());
        timer.cancel();
        timerMap.remove(apiContext.getTenantId());
    }
}
