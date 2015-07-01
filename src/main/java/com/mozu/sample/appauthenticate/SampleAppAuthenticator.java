package com.mozu.sample.appauthenticate;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mozu.api.MozuConfig;
import com.mozu.api.contracts.appdev.AppAuthInfo;
import com.mozu.api.security.AppAuthenticator;

public class SampleAppAuthenticator {
    private static final Logger logger = LoggerFactory.getLogger(SampleAppAuthenticator.class);
    private String applicationId = null;
    private String sharedSecret = null;
    private String baseAppAuthUrl = null;
    
    public SampleAppAuthenticator (String applicationId, String sharedSecret) {
        this.applicationId = applicationId;
        this.sharedSecret = sharedSecret;
    }
    
    public void appAuthentication() {
        
        logger.info("Authenticating Application in Mozu...");
        try {
            String realSharedSecret = sharedSecret;
            AppAuthInfo appAuthInfo = new AppAuthInfo();
            appAuthInfo.setApplicationId(applicationId);
            appAuthInfo.setSharedSecret(realSharedSecret);
            
            if (!StringUtils.isEmpty(baseAppAuthUrl))
                MozuConfig.setBaseUrl(baseAppAuthUrl);
            
            AppAuthenticator.initialize(appAuthInfo);
            logger.info("Auth ticket : " + AppAuthenticator.getInstance().getAppAuthTicket().getAccessToken());
            logger.info("Application authenticated");
            realSharedSecret = "";
        } catch(Exception exc) {
            logger.error(exc.getMessage(), exc);
        }
        
    }
}
