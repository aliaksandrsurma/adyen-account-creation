package com.asurma.account.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.asurma.account.Constants;
import com.asurma.account.WsCallResults;
import com.asurma.account.config.AdyenConfiguration;
import com.asurma.account.model.AdyenAccount;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.asurma.account.model.TempAccount;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Service
public class AccountManagementService {

    final Logger log = LoggerFactory.getLogger(AccountManagementService.class);

    private List<String> CUSTOMER_SERVICE_ROLES = Arrays.asList("Merchant_allowed_own_password_reset",
            "Merchant_standard_role", "View_Payments", "Merchant_view_payouts");

    private List<String> FINANCE_ROLES = Arrays.asList("Merchant_allow_bankrefund_role",
            "Merchant_allowed_own_password_reset", "Merchant_dispute_management", "Merchant_financial",
            "Merchant_manage_payments", "Merchant_Report_role", "Merchant_standard_role",
            "Merchant_submit_batch_modifications", "Merchant_view_POS_Payment_Report", "View_Payments");

    private List<String> ALL_MERCHANT_CODES = Arrays.asList("MerchantID_CHANGE_ME");

    private List<String> WE_ESITPL_MERCHANT_CODES = Arrays.asList("MerchantID_CHANGE_ME");


    private List<String> US_MERCHANT_CODES = Arrays.asList("MerchantID_CHANGE_ME");

    @Autowired
    private AdyenConfiguration configs;

    public Constants.WsCallStatus addUser(HttpServletRequest request, AdyenAccount account) {
        log.info("WS CALL START: login=" + account.getLogin());

        CloseableHttpClient client = createHttpClient(request);

        // Create new request
        JSONObject addUserJSONRequest = new JSONObject();
        addUserJSONRequest.put("active", "true");
        addUserJSONRequest.put("email", account.getEmail());
        addUserJSONRequest.put("userName", account.getLogin());
        addUserJSONRequest.put("timeZoneCode", "UTC");

        JSONObject nameObj = new JSONObject();
        nameObj.put("firstName", account.getFirstName());
        nameObj.put("lastName", account.getLastName());
        nameObj.put("gender", "UNKNOWN");

        addUserJSONRequest.put("name", nameObj);

        JSONArray roles = new JSONArray();
        if (Constants.TYPE_CUSTOMER_SERVICE.equals(account.getUserGroup())) {
            roles.addAll(CUSTOMER_SERVICE_ROLES);
        } else if (Constants.TYPE_FINANCE.equals(account.getUserGroup())) {
            roles.addAll(FINANCE_ROLES);
        } else {
            log.warn("Unknown user group!");
        }

        addUserJSONRequest.put("roles", roles);

        JSONArray addMerchantCodes = new JSONArray();
        if (Constants.MARKET_US.equals(account.getMarket())) {
            addMerchantCodes.addAll(US_MERCHANT_CODES);
        } else if (Constants.MARKET_FSS.equals(account.getMarket())) {
            addMerchantCodes.addAll(ALL_MERCHANT_CODES);
        } else if (Constants.MARKET_WE_ES_IT_PL.equals(account.getMarket())) {
            addMerchantCodes.addAll(WE_ESITPL_MERCHANT_CODES);
            // Getting from markets.properties
            // WE, PAC, LAM
        } else if (configs.getConfigValue(account.getMarket()) != null ) {
            String merchantAcoounts = configs.getConfigValue(account.getMarket());
            addMerchantCodes.addAll(Lists.newArrayList(Splitter.on(",").split(merchantAcoounts)));
        } else {
            log.warn("Unknown market!");
        }

        addUserJSONRequest.put("merchantCodes", addMerchantCodes);

        String apiUrl = configs.getConfigValue("endpoint.addUser");

        try {
            JSONObject addUserJSONResponse = callAPI(request, client, apiUrl, addUserJSONRequest);

            if (addUserJSONResponse.get("password") != null) {

                String password = (String) addUserJSONResponse.get("password");
                account.setPassword(password);
                account.setActive(true);
                account.setPspReference((String) addUserJSONResponse.get("pspReference"));

            }

            if (addUserJSONResponse.get("errors") != null) {
                log.error(addUserJSONResponse.get("errors").toString());
                log.error("WS CALL END: Failed");
                return Constants.WsCallStatus.FAILED;
            }

            log.info("WS CALL END: Success");
            return Constants.WsCallStatus.SUCCESS;

        } catch (IOException | ServletException e) {
            log.error("WS CALL END: Failed", e);
            return Constants.WsCallStatus.FAILED;
        }
    }
    
    
    public Constants.WsCallStatus getWebUser(HttpServletRequest request, TempAccount tempAccount) {
        log.info("WS getWebUser CALL START: login=" + tempAccount.getLogin());

        CloseableHttpClient client = createHttpClient(request);

        // Create new request
        JSONObject getWebUserJSONRequest = new JSONObject();
        getWebUserJSONRequest.put("userName", tempAccount.getLogin());
        

        String apiUrl = configs.getConfigValue("endpoint.getUser");

        try {
            JSONObject getUserJSONResponse = callAPI(request, client, apiUrl, getWebUserJSONRequest);

            if (getUserJSONResponse.get("email") != null) {

               
                tempAccount.setEmail((String)getUserJSONResponse.get("email"));
                tempAccount.setActive((Boolean)getUserJSONResponse.get("active"));
                
                JSONObject name = (JSONObject) getUserJSONResponse.get("name");
                tempAccount.setFirstName((String) name.get("firstName"));
                tempAccount.setLastName((String) name.get("lastName"));
            }

            if (getUserJSONResponse.get("errors") != null) {
                log.error(getUserJSONResponse.get("errors").toString());
                log.error("WS getWebUser CALL END: Failed");
                return Constants.WsCallStatus.FAILED;
            }

            log.info("WS getWebUser CALL END: Success");
            return Constants.WsCallStatus.SUCCESS;

        } catch (IOException | ServletException e) {
            log.error("WS getWebUser CALL END: Failed", e);
            return Constants.WsCallStatus.FAILED;
        }
    }

    public WsCallResults updateUser(HttpServletRequest request, String userName, String addRoles, String removeRoles, String addMerchants, String removeMerchants, boolean isActive) {

        CloseableHttpClient client = createHttpClient(request);

        // Create new request
        JSONObject updateUserJSONRequest = new JSONObject();
        updateUserJSONRequest.put("active", isActive);
        // updateUserJSONRequest.put("email", "TBD");
        updateUserJSONRequest.put("userName", userName.trim());

        if (!StringUtils.isEmpty(addRoles)) {
            JSONArray grantRoles = new JSONArray();
            grantRoles.add(addRoles);
            updateUserJSONRequest.put("grantRoles", grantRoles);
        }

        if (!StringUtils.isEmpty(removeRoles)) {

            JSONArray revokeRoles = new JSONArray();
            revokeRoles.add(removeRoles);
            updateUserJSONRequest.put("revokeRoles", revokeRoles);

        }
        

        if (!StringUtils.isEmpty(addMerchants)) {
            JSONArray addMerchantCodes = new JSONArray();
            //StringTokenizer st = new StringTokenizer("addMerchants",",");
            List<String> list = Lists.newArrayList(Splitter.on(",").split(addMerchants));
            
            addMerchantCodes.addAll(list);
            updateUserJSONRequest.put("addMerchantCodes", addMerchantCodes);
        }

        if (!StringUtils.isEmpty(removeMerchants)) {

            JSONArray deleteMerchantCodes = new JSONArray();
            List<String> list = Lists.newArrayList(Splitter.on(",").split(removeMerchants));

            deleteMerchantCodes.addAll(list);
            updateUserJSONRequest.put("deleteMerchantCodes", deleteMerchantCodes);

        }

        
        String apiUrl = configs.getConfigValue("endpoint.updateUser");
        try {
            JSONObject updateUserJSONResponse = callAPI(request, client, apiUrl, updateUserJSONRequest);

            if (updateUserJSONResponse.get("warnings") != null) {
                log.error(updateUserJSONResponse.get("warnings").toString());
                log.error("WS CALL END: Failed");
                return new WsCallResults(Constants.WsCallStatus.FAILED, updateUserJSONResponse.get("warnings").toString());
            }

            if (updateUserJSONResponse.get("errors") != null) {
                log.error(updateUserJSONResponse.get("errors").toString());
                log.error("WS CALL END: Failed");
                return new WsCallResults(Constants.WsCallStatus.FAILED, updateUserJSONResponse.get("errors").toString());
            }
        } catch (IOException | ServletException e) {
            log.error("WS CALL END: Failed", e);
            return new WsCallResults(Constants.WsCallStatus.FAILED, e.getMessage());
        }

        log.info("WS CALL END: Success");
        return new WsCallResults(Constants.WsCallStatus.SUCCESS, "");
    }

    // temporary method
    public void fixCSUser(HttpServletRequest request, String name) {

        log.info("Fixing CallCenterUserRole for login=" + name);

        CloseableHttpClient client = createHttpClient(request);

        // Create new request
        JSONObject updateUserJSONRequest = new JSONObject();
        // updateUserJSONRequest.put("active", "true");
        // updateUserJSONRequest.put("email", "TBD");
        updateUserJSONRequest.put("userName", name);

        JSONArray revokeRoles = new JSONArray();
        revokeRoles.add("CallCenter_user_role");
        updateUserJSONRequest.put("revokeRoles", revokeRoles);

        // JSONArray addMerchantCodes = new JSONArray();
        // grantRoles.add("");
        // updateUserJSONRequest.put("addMerchantCodes", addMerchantCodes);

        String apiUrl = configs.getConfigValue("endpoint.updateUser");
        log.info("WS CALL START: type=UpdateUser");
        try {
            JSONObject updateUserJSONResponse = callAPI(request, client, apiUrl, updateUserJSONRequest);
        } catch (IOException | ServletException e) {
            log.error("WS CALL END: Status=Failed", e);
        }
        log.info("WS CALL END: type=UpdateUser Status=Success");
    }

    private CloseableHttpClient createHttpClient(HttpServletRequest request) {

        /**
         * JSON settings - apiUrl: URL of the Adyen API you are using (Test/Live) -
         * wsUser: your web service user - wsPassword: your web service user's password
         */
        String wsUser = configs.getConfigValue("ws.user");
        String wsPassword = configs.getConfigValue("ws.password");

        /**
         * Create HTTP Client (using Apache HttpComponents library) and set up Basic
         * Authentication
         */
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(wsUser, wsPassword);
        provider.setCredentials(AuthScope.ANY, credentials);
        
        
        CloseableHttpClient client = null;
        
        if (AdyenConfiguration.isTestMode()) {
            client =
                    HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
            
        } else {
            // for PROD
            client = HttpClientBuilder.create().build();
        }

        return client;
    }

    void postAdyenCall(Map<String, Object> model, JSONObject paymentJSONRequest, JSONObject paymentJSONResponse) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // model.put("requestJson", gson.toJson(paymentJSONRequest));
        // model.put("responseJson", gson.toJson(paymentJSONResponse));
    }

    JSONObject callAPI(HttpServletRequest request, CloseableHttpClient client, String apiEndpoint, JSONObject apiJSONRequest)
            throws IOException, ClientProtocolException, ServletException {

        /**
         * Send the HTTP request with the specified variables in JSON.
         */
        HttpPost httpRequest = new HttpPost(apiEndpoint);
        httpRequest.addHeader("Content-Type", "application/json");
        httpRequest.addHeader("User-Agent", "PostmanRuntime/7.13.0");
        httpRequest.addHeader("Connection", "keep-alive");

        httpRequest.setEntity(new StringEntity(apiJSONRequest.toString(), "UTF-8"));

        log.info("jsonRequest = " + apiJSONRequest);
        log.info("calling Adyen endpoint = " + apiEndpoint);

        // if (true) return new JSONObject();
        HttpResponse httpResponse = client.execute(httpRequest);
        String response = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");

        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new ServletException("Http Response is " + httpResponse.getStatusLine().getStatusCode());
        }
        log.info("httpResponse = " + httpResponse);
        /**
         * Keep in mind that you should handle errors correctly. If the Adyen platform
         * does not accept or store a submitted request, you will receive a HTTP
         * response with status different than 200 OK. In this case, the error details
         * are populated in the paymentResponse.
         */

        // Parse JSON response
        JSONParser parser = new JSONParser();
        JSONObject userJSONResponse;

        try {
            userJSONResponse = (JSONObject) parser.parse(response);
        } catch (ParseException e) {
            log.error("Unable to parse JSON response", e);
            throw new ServletException(e);
        }

        log.info("jsonResponse = " + userJSONResponse);

        return userJSONResponse;
    }

    // void logRespose(JSONObject paymentJSONResponse) {
    //
    // log.info("- pspReference: " + paymentJSONResponse.get("pspReference"));
    // log.info("- resultCode: " + paymentJSONResponse.get("resultCode"));
    // log.info("- authCode: " + paymentJSONResponse.get("authCode"));
    // log.info("- refusalReason: " + paymentJSONResponse.get("refusalReason"));
    // }

}
