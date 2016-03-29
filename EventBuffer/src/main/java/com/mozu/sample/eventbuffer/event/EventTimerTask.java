package com.mozu.sample.eventbuffer.event;

import java.util.Date;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mozu.api.ApiContext;
import com.mozu.sample.eventbuffer.service.DispatchEventService;
import com.mozu.sample.eventbuffer.service.EventBufferService;

public class EventTimerTask extends TimerTask {
    private static final Logger logger = LoggerFactory.getLogger(EventTimerTask.class);

    EventBufferService eventBufferService;
    ApiContext apiContext;
    DispatchEventService eventService;
    
    public EventTimerTask (ApiContext apiContext, EventBufferService eventBufferService, DispatchEventService eventService) {
        this.apiContext = apiContext;
        this.eventBufferService = eventBufferService;
        this.eventService = eventService;
    }
    
    @Override
    public void run() {
        logger.info("Start Event Log Processing:" + new Date());
        eventService.dispatchPendingEvents(apiContext);
        logger.info("End Event Log Processing:" + new Date());
    }

}
