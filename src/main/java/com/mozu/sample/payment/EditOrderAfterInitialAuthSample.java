package com.mozu.sample.payment;


import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mozu.api.ApiContext;
import com.mozu.api.MozuApiContext;
import com.mozu.api.contracts.commerceruntime.orders.Order;
import com.mozu.api.contracts.commerceruntime.payments.Payment;
import com.mozu.api.contracts.commerceruntime.payments.PaymentInteraction;
import com.mozu.api.resources.commerce.OrderResource;
import com.mozu.api.resources.commerce.orders.PaymentResource;

public class EditOrderAfterInitialAuthSample {
    private static final Logger logger = LoggerFactory.getLogger(EditOrderAfterInitialAuthSample.class);
    
    /**
     * Demonstrates how to add additional amount to order after initial authorization.
     * For this, void the initial authorized payment and re-authorize a new payment that has original requested amount + new amount
     * @param apiContext
     * @param orderId
     * @param amount
     */
    public void editOrderAfterInitialAuth(ApiContext apiContext, String orderId, double amount) { 
    	PaymentUtils paymentUtils = new PaymentUtils();
        OrderResource orderResource = new OrderResource(apiContext);
        PaymentResource paymentResource = new PaymentResource(apiContext);
        Order existingOrder= null;
        try {
           	existingOrder = orderResource.getOrder(orderId);
            logger.info (String.format("Succesfully retrieved order number %d. ", existingOrder.getOrderNumber()));
        }catch (Exception e) {
            logger.error (String.format("Error getting order from tenant %d: %s", apiContext.getTenantId(), e.getMessage()));
        }  
        Payment authorizedPayment =paymentUtils.getAuthorizedPayment(existingOrder.getPayments()) ;
        String paymentServiceTransactionId = null;
        if(authorizedPayment!=null){
           PaymentInteraction authorizedPaymentInteraction=paymentUtils.getAuthorizedPaymentInteraction(authorizedPayment.getInteractions());
           if(authorizedPaymentInteraction !=null){
            	paymentServiceTransactionId = authorizedPaymentInteraction.getGatewayTransactionId();
            	
            	// Void the initial authorized payment
                paymentUtils.voidPayment(paymentResource, orderId, authorizedPayment, paymentServiceTransactionId);
                    
                //Authorize new payment(original requested amount + new amount )
                logger.info ("Adding $20 to already authorized order");
                paymentUtils.authorizePayment(paymentResource, orderId, authorizedPayment, (authorizedPayment.getAmountRequested()+ amount));
           }
        }
    }
    
    /** 
     * Code to run the EditOrderAfterInitialAuth Sample
     * @param tenantId
     * @param scanner
     */
    public void runSample(Integer tenantId, Scanner scanner) {
    	System.out.print("Enter site ID:");
        Integer siteId = scanner.nextInt();
        
        System.out.print("Enter order ID:");
        String orderId = scanner.next();
       
        editOrderAfterInitialAuth(new MozuApiContext(tenantId,siteId), orderId, 20.00);
    }
     
}
