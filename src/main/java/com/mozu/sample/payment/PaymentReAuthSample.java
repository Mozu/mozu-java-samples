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

public class PaymentReAuthSample {
    private static final Logger logger = LoggerFactory.getLogger(PaymentReAuthSample.class);
    
    /**
     * Demonstrates how to partially capture already authorized payment and re-authorize remaining amount
     * @param apiContext
     * @param orderId
     */
    public void singleOrderWithPartalCaptureAndReAuth(ApiContext apiContext, String orderId) { 
        
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
            	
            	// Capture partial payment of an already authorized payment
                paymentUtils.capturePayment(paymentResource, orderId, authorizedPayment, paymentServiceTransactionId,authorizedPayment.getAmountRequested()/2);
                    
                 //Authorize remaining payment
                paymentUtils.authorizePayment(paymentResource, orderId, authorizedPayment, authorizedPayment.getAmountRequested() / 2);
           }
        }
     }
    
    /** 
     * Code to run the SingleOrderWithPartalCaptureAndReAuth Sample
     * @param tenantId
     * @param scanner
     */
    public void runSample(Integer tenantId, Scanner scanner) {
    	System.out.print("Enter site ID:");
        Integer siteId = scanner.nextInt();
        
        System.out.print("Enter order ID:");
        String orderId = scanner.next();
       
        singleOrderWithPartalCaptureAndReAuth(new MozuApiContext(tenantId,siteId), orderId);
    }

}
