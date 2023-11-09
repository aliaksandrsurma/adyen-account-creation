package com.asurma.account.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:adyen_${env}.properties")
@PropertySource("markets.properties")
public class AdyenConfiguration {

    @Autowired
    private Environment env;

    public String getConfigValue(String name) {
        return env.getProperty(name);
    }

    public static String getEnvironment() {
        String env = System.getProperty("env");
        if (env == null) {
            env = System.getenv("env");
        }
        return env;
    }
    
    public static boolean isTestMode () {
        return "test".equals(getEnvironment());
    }
}
