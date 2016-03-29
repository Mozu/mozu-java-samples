package com.mozu.sample.eventbuffer.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mozu.api.ApiContext;
import com.mozu.sample.eventbuffer.controller.EventBufferController;
import com.mozu.sample.eventbuffer.event.EventTimerTask;

@Service
public class TimerService {
    private static final Logger logger = LoggerFactory.getLogger(EventBufferController.class);

    @Autowired 
    EventBufferService eventBufferService;

    @Autowired 
    DispatchEventService dispatchBufferService;

    Map <Integer, Timer> timerMap = new HashMap<> ();
    
    public void startEventPolling (ApiContext apiContext) {
        // only start one task per tenant.
        if (this.timerMap.get(apiContext.getTenantId()) == null) {
            TimerTask eventTimerTask = new EventTimerTask(apiContext, eventBufferService, dispatchBufferService);
            Timer timer = new Timer(true);
            // run job every 30 seconds.
            timer.scheduleAtFixedRate(eventTimerTask, 0, 30 * 1000);
            timerMap.put(apiContext.getTenantId(), timer);
            logger.info ("Event log timer task started: " + apiContext.getTenantId());
        }
    }
    
    public void stopEventPolling (ApiContext apiContext) {
        Timer timer = timerMap.get(apiContext.getTenantId());
        if (timer != null) {
            timer.cancel();
            timerMap.remove(apiContext.getTenantId());
            logger.info ("Event log timer task canceled: " + apiContext.getTenantId());
        }
    }
    
    public boolean isEventTimerPolling (ApiContext apiContext) {
        boolean isPolling = false;
        if (timerMap.get(apiContext.getTenantId()) != null) {
            isPolling = true;
        }
        
        return isPolling;
    }
}
