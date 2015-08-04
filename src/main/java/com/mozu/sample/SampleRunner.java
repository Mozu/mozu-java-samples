package com.mozu.sample;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mozu.sample.appauthenticate.SampleAppAuthenticator;
import com.mozu.sample.order.GetAllOrdersSample;
import com.mozu.sample.order.GetOrderSample;
import com.mozu.sample.payment.EditOrderAfterInitialAuthSample;
import com.mozu.sample.payment.PaymentReAuthSample;

public class SampleRunner {
    private static final Logger logger = LoggerFactory.getLogger(SampleRunner.class);

    private static String       APPLICATION_ID = "mzint.java_sample.1.0.0.release";
    private static String       SHARED_SECRET  = "9402779107ec4c8fa7b35ebe0ef1b155";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {

            SampleAppAuthenticator sampleAppAuthenticator = new SampleAppAuthenticator(
                    APPLICATION_ID, SHARED_SECRET);
            sampleAppAuthenticator.appAuthentication();

            System.out.print("Enter the tenantId of the application:");
            Integer tenantId = scanner.nextInt();
            
            printSampleMenu();

            for (;;) {
                System.out.print("Enter menu selection:");
                String option = scanner.next();
                if (option.equals("q")) {
                    break;
                }
                runSample(option, tenantId, scanner);
            }

        } catch (Exception e) {
            logger.error("Exception selecting sample: " + e.getMessage());
            ;

        } finally {
            scanner.close();
        }

    }

    private static void runSample(String option, Integer tenantId,
            Scanner scanner) {
        switch (option) {
        case "1":
            GetOrderSample orderSample = new GetOrderSample();
            orderSample.runSample(tenantId, scanner);
            break;
        case "2":
            GetAllOrdersSample allOrdersSample = new GetAllOrdersSample();
            allOrdersSample.runSample(tenantId, scanner);
            break;
        case "3":
            PaymentReAuthSample paymentReAuthSample = new PaymentReAuthSample();
            paymentReAuthSample.runSample(tenantId, scanner);
          
            break;
        case "4":
        	EditOrderAfterInitialAuthSample editOrderAfterInitialAuthSample = new EditOrderAfterInitialAuthSample();
            editOrderAfterInitialAuthSample.runSample(tenantId, scanner);
            break;
        default:
            System.out.print("Bad command");

        }

    }

    private static void printSampleMenu() {
        String sampleMenu = "1 - Get Order By Order ID\n"
                + "2 - Get All Orders\n" + "3 - Payment with partial capture and re-auth \n"+ "4 - Add additional item to authorized payment \n" + "q - quit\n";
        System.out.print(sampleMenu);
    }
}
