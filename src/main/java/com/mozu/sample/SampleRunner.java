package com.mozu.sample;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mozu.api.MozuApiContext;
import com.mozu.sample.appauthenticate.SampleAppAuthenticator;
import com.mozu.sample.order.OrderSample;

public class SampleRunner {
    private static final Logger  logger         = LoggerFactory
                                                        .getLogger(SampleRunner.class);

    private static String        APPLICATION_ID = "mzint.java_sample.1.0.0.release";
    private static String        SHARED_SECRET  = "9402779107ec4c8fa7b35ebe0ef1b155";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {

            SampleAppAuthenticator sampleAppAuthenticator = new SampleAppAuthenticator(APPLICATION_ID, SHARED_SECRET);
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
            logger.error("Exception selecting sample: " + e.getMessage());;
            
        } finally {
            scanner.close();
        }

    }

    private static void runSample(String option, Integer tenantId, Scanner scanner) {
        switch (option) {
        case "1":
            OrderSample orderSample = new OrderSample();
            System.out.print("Enter order ID:");
            String orderId = scanner.next();

            orderSample.getOrderFromMozu(new MozuApiContext(tenantId), orderId);
            break;
        default: 
            System.out.print ("Bad command");
            
        }
        
    }

    private static void printSampleMenu() {
        String sampleMenu = "1 - Get Order Sample\n" + "q - quit\n";
        System.out.print(sampleMenu);
    }
}
