package com.mozu.sample.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mozu.api.ApiContext;
import com.mozu.api.contracts.commerceruntime.orders.Order;
import com.mozu.api.resources.commerce.OrderResource;

public class OrderSample {
    private static final Logger logger = LoggerFactory.getLogger(OrderSample.class);
    
    /**
     * Demonstrates how to get the order done  
     * @param apiContext
     * @param orderId
     */
    public void getOrderFromMozu(ApiContext apiContext, String orderId) { 
        
        OrderResource orderResource = new OrderResource(apiContext);
        try {
            // 
            Order order = orderResource.getOrder(orderId);
            
            logger.info (String.format("Succesfully retrieved order number %d.   The total sale is: %f", order.getOrderNumber(), order.getTotal()));
        } catch (Exception e) {
            logger.error (String.format("Error getting order from tenant %d: %s", apiContext.getTenantId(), e.getMessage()));
        }

    }

}
