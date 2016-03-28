package com.mozu.sample.eventbuffer.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mozu.api.ApiContext;
import com.mozu.api.MozuApiContext;
import com.mozu.api.security.Crypto;
import com.mozu.api.utils.JsonUtils;
import com.mozu.sample.eventbuffer.model.MozuEvent;
import com.mozu.sample.eventbuffer.service.EventBufferService;

@Controller
public class EventBufferController {
    private static final Logger logger = LoggerFactory.getLogger(EventBufferController.class);
    private static final ObjectMapper mapper = JsonUtils.initObjectMapper();

    @Autowired
    EventBufferService eventBufferService;
    
    /**
     * Event handler top level controller. This sample controller uses 
     * Spring annotation to inject the controller into the servlet container. 
     * 
     * This sample event controller receives all events for which the 
     * application has registered. The controller only serves as the 
     * top level entry point in the servlet container and passes the
     * event handling off to the SDK via the EventService.
     *
     * @param httpRequest Event notification request
     * @return a ResponseEntity containing the event processing status 
     * as an HTTP code and optionally an error message
     */
    @RequestMapping(value = "/eventbuffer", method = RequestMethod.POST, consumes="application/json" )
    public ResponseEntity<String> processEventRequest (HttpServletRequest httpRequest) {
        ApiContext apiContext = new MozuApiContext(httpRequest);
        MozuEvent event = null;

        // get the event from the request and validate
        try {
            String body = IOUtils.toString(httpRequest.getInputStream());
            logger.debug("Event body: " + body);
            event = mapper.readValue(body, MozuEvent.class);
            if (!Crypto.isRequestValid(apiContext, body)) {
                StringBuilder msg = new StringBuilder ("Event is not authorized.");
                logger.warn(msg.toString());
                return new ResponseEntity<String>("Event is not authorized.", HttpStatus.UNAUTHORIZED);
            }
        } catch (IOException exception) {
            StringBuilder msg = new StringBuilder ("Unable to read event: ").append(exception.getMessage());
            logger.error(msg.toString());
            return( new ResponseEntity<String>(msg.toString(), HttpStatus.INTERNAL_SERVER_ERROR));
        }

        try {
            logger.info("Adding to Event Queue.  Site ID: " + apiContext.getSiteId() + " Correlation ID: " + event.getCorrelationId());
             eventBufferService.addEvent(apiContext, event);
        } catch (Exception e) {
            if (e.getMessage() != null) {
                return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<String>(HttpStatus.OK);
    }
}
