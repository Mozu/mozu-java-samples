package com.mozu.sample.order;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mozu.api.ApiContext;
import com.mozu.api.MozuApiContext;
import com.mozu.api.contracts.commerceruntime.orders.Order;
import com.mozu.api.resources.commerce.OrderResource;

public class GetOrderSample {
    private static final Logger logger = LoggerFactory.getLogger(GetOrderSample.class);
    
    /**
     * Demonstrates how to get an order from Mozu given an order ID
     * @param apiContext
     * @param orderId
     */
    public void getOrderFromMozu(ApiContext apiContext, String orderId) { 
        
        OrderResource orderResource = new OrderResource(apiContext);
        try {
            // get the order with the give order ID from Mozu 
            Order order = orderResource.getOrder(orderId);
            
            //
            logger.info (String.format("Succesfully retrieved order number %d.   The total sale is: %f", 
                    order.getOrderNumber(), order.getTotal()));
        } catch (Exception e) {
            logger.error (String.format("Error getting order from tenant %d: %s", apiContext.getTenantId(), e.getMessage()));
        }

    }

    /** 
     * Code to run the Get Order sample
     * @param tenantId
     * @param scanner
     */
    public void runSample(Integer tenantId, Scanner scanner) {
        System.out.print("Enter order ID:");
        String orderId = scanner.next();

        getOrderFromMozu(new MozuApiContext(tenantId), orderId);
    }
}
