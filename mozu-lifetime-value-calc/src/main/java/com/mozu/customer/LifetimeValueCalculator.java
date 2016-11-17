package com.mozu.customer;

import java.util.Scanner;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.common.util.concurrent.RateLimiter;
import com.mozu.api.ApiContext;
import com.mozu.api.MozuApiContext;
import com.mozu.api.contracts.customer.CustomerAccount;
import com.mozu.api.contracts.customer.CustomerAccountCollection;
import com.mozu.api.resources.commerce.customer.CustomerAccountResource;
import com.mozu.customer.appauthenticate.CustomerAppAuthenticator;

public class LifetimeValueCalculator {
    public static final String HELP_OPTION = "?";
    public static final String TENENT_OPTION = "t";
    public static final String SITE_OPTION = "s";
    public static final String CUSTOMER_ID = "cid";
    public static final String TRANSACTIONS_PER_SEC = "tps";
    public static final String INCLUDE_ANONYMOUS = "a";
  
    public static final String APPLICATION_KEY = "c367d49.cust_lifetime_calc.1.0.0.Release";
    public static final String SHARED_SECRET = "25cbcec7df0c4ecc9b1a780016135b0b";
    
    public static void main(String[] args) {
        Options options = addOptions ();
        
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException pe) {
            System.out.println ("Unable to parse command line: " + pe.getMessage());
            printHelp(options);
            return;
        }
        if (cmd.hasOption(HELP_OPTION)) {
            printHelp(options);
            return;
        }
        
        Scanner scanner = new Scanner(System.in);
        try {

            CustomerAppAuthenticator sampleAppAuthenticator = new CustomerAppAuthenticator(
                    APPLICATION_KEY, SHARED_SECRET);
            sampleAppAuthenticator.appAuthentication();
            Integer tenantId = null;
            Integer siteId = null;
            if (cmd.hasOption(TENENT_OPTION)) {
                tenantId = Integer.valueOf(cmd.getOptionValue(TENENT_OPTION));
            } else {
                System.out.print("Enter the tenant ID:");
                tenantId = scanner.nextInt();
            }
            if (cmd.hasOption(SITE_OPTION)) {
                siteId = Integer.valueOf(cmd.getOptionValue(SITE_OPTION));
            } else {
                System.out.print("Enter the site ID:");
                siteId = scanner.nextInt();
            }
            
            // recalculate give
            if (cmd.hasOption(CUSTOMER_ID)) {
                Integer customerId = scanner.nextInt();
                recalculateSingleCustomerLifetimeValues(tenantId, siteId, customerId);
            } else {
                RateLimiter rateLimiter = null;
                if (cmd.hasOption(TRANSACTIONS_PER_SEC)) {
                    double transPerSec = Double.valueOf(cmd.getOptionValue(TRANSACTIONS_PER_SEC));
                    rateLimiter = RateLimiter.create(transPerSec);
                }

                recalculateAllTenantCustomerLifetimeValues (tenantId, siteId, rateLimiter, cmd.hasOption(INCLUDE_ANONYMOUS));
            }
        } catch (Exception e) {
            System.out.println("Exception calculating lifetime values: " + e.getMessage());
          

        } finally {
            scanner.close();
        }
    } 
    
    private static void recalculateAllTenantCustomerLifetimeValues(Integer tenantId, Integer siteId, RateLimiter rateLimter, boolean isAnonymous) throws Exception{
        ApiContext apiContext = new MozuApiContext(tenantId, siteId);
        CustomerAccountResource customerAccountResource = new CustomerAccountResource(apiContext);
        int pageSize = 20;
        int startIndex = 0;
        int totalCount = 0;
        CustomerAccountCollection customerAccts = null;
        do {
            customerAccts = customerAccountResource.getAccounts(startIndex, pageSize, null, null, null, null, null, isAnonymous, null);
            startIndex += pageSize;
            if (customerAccts.getItems() != null) {
                totalCount = customerAccts.getTotalCount();
                for (CustomerAccount customerAccount : customerAccts.getItems()) {
                    if (rateLimter != null) {
                        rateLimter.acquire();
                    }
                    System.out.println("Processing Customer: " + customerAccount.getLastName() + ", " + customerAccount.getFirstName());
                    customerAccountResource.recomputeCustomerLifetimeValue(customerAccount.getId());
                }
            }
        } while (startIndex < totalCount);
    }

    private static void recalculateSingleCustomerLifetimeValues(Integer tenantId, Integer siteId, Integer customerId) throws Exception{
        ApiContext apiContext = new MozuApiContext(tenantId, siteId);

        CustomerAccountResource customerAccountResource = new CustomerAccountResource(apiContext);
        customerAccountResource.recomputeCustomerLifetimeValue(customerId);
    }

    @SuppressWarnings("static-access")
    private static Options addOptions() {
        Options options = new Options();
        options.addOption(HELP_OPTION, "help", false, "Print this message.");

        Option tenantValue = OptionBuilder.withArgName("value").hasArg().withDescription("The tenant ID.")
                .create(TENENT_OPTION);
        options.addOption( tenantValue);

        Option siteValue = OptionBuilder.withArgName("value").hasArg().withDescription("The site ID.")
                .create(SITE_OPTION);
        options.addOption( siteValue);

        Option cidValue = OptionBuilder.withArgName("value").hasArg().withDescription("If present, the application recalculates a single customer ID.")
                .create(CUSTOMER_ID);
        options.addOption( cidValue);
        
        Option tpsValue = OptionBuilder.withArgName("value").hasArg().withDescription("If present, the lifetime calculation is limited to the transactions per second indicated here.")
                .create(TRANSACTIONS_PER_SEC);
        options.addOption( tpsValue);

        options.addOption(INCLUDE_ANONYMOUS, "include_anonymous", false, "Include anonymous shoppers.");

        return options;
    }

    private static void printHelp(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("LifetimeValueCalculator", options);
    }
}
