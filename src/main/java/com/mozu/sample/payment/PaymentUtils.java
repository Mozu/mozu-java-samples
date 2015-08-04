package com.mozu.sample.payment;

import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mozu.api.contracts.commerceruntime.orders.Order;
import com.mozu.api.contracts.commerceruntime.payments.BillingInfo;
import com.mozu.api.contracts.commerceruntime.payments.Payment;
import com.mozu.api.contracts.commerceruntime.payments.PaymentAction;
import com.mozu.api.contracts.commerceruntime.payments.PaymentInteraction;
import com.mozu.api.resources.commerce.orders.PaymentResource;


public class PaymentUtils {
    private static final Logger logger = LoggerFactory.getLogger(PaymentUtils.class);
  
    /** Demonstrates how to capture an authorized payment
     * @param paymentResource
     * @param orderId
     * @param authorizedPayment
     * @param paymentServiceTransactionId
     * @param amount
     */
    public void capturePayment(PaymentResource paymentResource,String orderId,Payment authorizedPayment, String paymentServiceTransactionId , Double amount){
    	PaymentAction paymentAction = new PaymentAction();
        paymentAction.setAmount(amount);
        paymentAction.setCurrencyCode("USD");
        paymentAction.setInteractionDate(DateTime.now());
        BillingInfo newBillingInfo = getNewBillingInfo(authorizedPayment);
        paymentAction.setNewBillingInfo(newBillingInfo);
        paymentAction.setReferenceSourcePaymentId(paymentServiceTransactionId);
        paymentAction.setActionName("CapturePayment");
       try {
			paymentResource.performPaymentAction(paymentAction, orderId, authorizedPayment.getId());
			logger.info ("Succesfully captured $"+amount+" of order Id "+ orderId);
		} catch (Exception e) {
			logger.error (String.format("Error while capturing payment for order %s: %s", orderId, e.getMessage()));
		}
	
    }
    
    /**
     * Demonstrates how to void an authorized payment
     * @param paymentResource
     * @param orderId
     * @param authorizedPayment
     * @param paymentServiceTransactionId
     */
    public void voidPayment(PaymentResource paymentResource,String orderId,Payment authorizedPayment, String paymentServiceTransactionId){
    	PaymentAction paymentAction = new PaymentAction();
        paymentAction.setAmount(authorizedPayment.getAmountRequested());
        paymentAction.setCurrencyCode("USD");
        paymentAction.setInteractionDate(DateTime.now());
        BillingInfo newBillingInfo = getNewBillingInfo(authorizedPayment);
        paymentAction.setNewBillingInfo(newBillingInfo);
        paymentAction.setReferenceSourcePaymentId(paymentServiceTransactionId);
        paymentAction.setActionName("VoidPayment");
       try {
			paymentResource.performPaymentAction(paymentAction, orderId, authorizedPayment.getId());
			logger.info ("Succesfully voided $"+authorizedPayment.getAmountRequested()+" of order Id "+ orderId);
		} catch (Exception e) {
			logger.error (String.format("Error while capturing payment for order %s: %s", orderId, e.getMessage()));
		}
	
    }
    
    /**
     * Demonstrates how to authorize payment
     * @param paymentResource
     * @param orderId
     * @param authorizedPayment
     * @param amount
     * @return
     */
    public Order authorizePayment(PaymentResource paymentResource,String orderId,Payment authorizedPayment, Double amount){
    	PaymentAction paymentAction = new PaymentAction();
    	double roundedValue = Math.round(amount*100.0)/100.0;
        paymentAction.setAmount(roundedValue);
        paymentAction.setCurrencyCode("USD");
        paymentAction.setInteractionDate(DateTime.now());
        BillingInfo newBillingInfo = getNewBillingInfo(authorizedPayment);
        paymentAction.setNewBillingInfo(newBillingInfo);
        paymentAction.setReferenceSourcePaymentId(null);
        paymentAction.setActionName("AuthorizePayment");
        Order newOrder = null;
       try {
			newOrder = paymentResource.createPaymentAction(paymentAction, orderId, authorizedPayment.getId());
			logger.info ("Succesfully authorized amount $"+roundedValue+" for order Id "+orderId);
		} catch (Exception e) {
			logger.error (String.format("Error while authorizing payment for order %s: %s", orderId, e.getMessage()));
		}
       return newOrder;
    }
    
    /**
     * @param authorizedPayment
     * @return
     */
    public BillingInfo getNewBillingInfo(Payment authorizedPayment){
    	  BillingInfo newBillingInfo = new BillingInfo();
          newBillingInfo.setAuditInfo(authorizedPayment.getAuditInfo());
          newBillingInfo.setBillingContact(authorizedPayment.getBillingInfo().getBillingContact());
          newBillingInfo.setCard(authorizedPayment.getBillingInfo().getCard());
          newBillingInfo.setIsSameBillingShippingAddress(authorizedPayment.getBillingInfo().getIsSameBillingShippingAddress());
          newBillingInfo.setPaymentType(authorizedPayment.getBillingInfo().getPaymentType());
          newBillingInfo.setStoreCreditCode(authorizedPayment.getBillingInfo().getStoreCreditCode());
		  return newBillingInfo;
    }
    
    /**
     * Get authorized payment
     * @param existingPaymentList
     * @return
     */
    
    public Payment getAuthorizedPayment(List<Payment> existingPaymentList){
    	Payment authorizedPayment=null;
    	for (Payment payment : existingPaymentList) {
			if(payment.getStatus().equals("Authorized")){
				authorizedPayment = payment;
				break;
			}
		}
		return authorizedPayment;
   }
    
    /**
     * Get authorized payment interaction
     * @param interactions
     * @return
     */
    public PaymentInteraction getAuthorizedPaymentInteraction(List<PaymentInteraction> interactions){
    	PaymentInteraction authorizedPaymentInteraction = null;
    	for (PaymentInteraction interaction: interactions) {
		       if (interaction.getStatus().equalsIgnoreCase("Authorized")) {
		    	  authorizedPaymentInteraction=interaction;
		          break;
		        }
		    }
		return authorizedPaymentInteraction;
    }

}
