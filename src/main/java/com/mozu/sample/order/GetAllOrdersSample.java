package com.mozu.sample.order;

import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mozu.api.ApiContext;
import com.mozu.api.MozuApiContext;
import com.mozu.api.contracts.commerceruntime.orders.Order;
import com.mozu.api.contracts.commerceruntime.orders.OrderCollection;
import com.mozu.api.resources.commerce.OrderResource;

public class GetAllOrdersSample {
    private static final Logger logger = LoggerFactory.getLogger(GetAllOrdersSample.class);
    
    /** the page size to return.  Note the max value is 200. */
    private static final int PAGE_SIZE = 25;
    
    /**
     * Demonstrates how to get the order done  
     * @param apiContext
     * @param orderId
     */
    public boolean getOrdersFromMozu(ApiContext apiContext, int pageNo) { 
        boolean hasMoreOrders = true;
        OrderResource orderResource = new OrderResource(apiContext);
        try {
            int startIndex = pageNo * PAGE_SIZE;
            // Get 25 orders at at time and sort by orderNumber.
            // filters on completed orders only
            OrderCollection orderCollection = orderResource.getOrders(startIndex, PAGE_SIZE, "orderNumber", "status eq Completed", null, null, null);
            List<Order> orders = orderCollection.getItems ();
            if (orders.size() != 0) {
                for (Order order : orders) {
                    // Note: billing info is used in Mozu for the Customer Name in the Order List so it is important to always include the Billing Address...
                    String firstName = "N/A";
                    String lastName = "N/A";
                    if (order.getBillingInfo() != null && order.getBillingInfo().getBillingContact()!= null) {
                        firstName = order.getBillingInfo().getBillingContact().getFirstName();
                        lastName = order.getBillingInfo().getBillingContact().getLastNameOrSurname();
                    }
                    String currencyCode = order.getCurrencyCode() != null ? order.getCurrencyCode() : "$"; 

                    System.out.println(String.format("Order number %d Total sale is: %s %.2f Customer Name: %s %s", order.getOrderNumber(), currencyCode, order.getTotal(), firstName, lastName));
                }
            } else {
                hasMoreOrders = false;
            }
        } catch (Exception e) {
            logger.error (String.format("Error getting order from tenant %d: %s\n", apiContext.getTenantId(), e.getMessage()));
            hasMoreOrders = false;
        }
        return hasMoreOrders;
    }

    /** 
     * Code to run the Get Order sample
     * @param tenantId
     * @param scanner
     */
    public void runSample(Integer tenantId, Scanner scanner) {
        System.out.print("List of All Orders...\n");

        int pageNo = 0;
        boolean getMoreOrders = true;
        while (getMoreOrders) {
            getMoreOrders = getOrdersFromMozu(new MozuApiContext(tenantId), pageNo++);
            if (getMoreOrders) {
                System.out.print("Continue (Y/N)?");
                String option = scanner.next();
                if (option.toLowerCase().equals("n") ) {
                    getMoreOrders = false;
                }
            }
        }
    }
}
