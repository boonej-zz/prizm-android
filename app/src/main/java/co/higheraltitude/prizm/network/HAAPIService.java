package co.higheraltitude.prizm.network;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.joanzapata.android.asyncservice.api.annotation.ApplicationContext;
import com.joanzapata.android.asyncservice.api.annotation.AsyncService;
import com.joanzapata.android.asyncservice.api.annotation.CacheThenCall;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpAuthentication;
import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import co.higheraltitude.prizm.ProfileActivity;
import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.models.User;

/**
 * Created by boonej on 8/24/15.
 */
@AsyncService
public class HAAPIService {
    @ApplicationContext
    static public Context context;
    static private JSONObject authorizationCode = null;
    static private String clientID = "67e1fe4f-db1b-4d5c-bdc7-56270b0832e2";
    static private String clientSecret = "f27198fb-689d-4965-acb0-0e9c5d61ddec";
    static private String redirectUrl = "https://api.prizmapp.com/callback";
    static private String accessToken = null;
    static private String refreshToken = null;
    static private Date tokenExpires = null;
    static private String authorizationString = null;
    static private RestTemplate restTemplate = null;

    private RestTemplate restTemplate() {
        if (restTemplate == null) {
            restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
            restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
            restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());
        }
        return restTemplate;
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {


        HttpComponentsClientHttpRequestFactory s = new HttpComponentsClientHttpRequestFactory();
        s.setReadTimeout(5000);
        s.setConnectTimeout(1000);


        return s;
    }

    private void getToken() {
            String path = context.getString(R.string.network_base_url)
                    + context.getString(R.string.network_auth_endpoint)
                    + "?client_id=" + clientID + "&redirect_uri=" + redirectUrl
                    + "&response_type=code";
            RestTemplate rt = restTemplate();

            try {
                HttpEntity<String> request = new HttpEntity<>(getHeaders("none"));
                ResponseEntity<String> response = rt.exchange(path, HttpMethod.GET, request, String.class);
                String returnString = response.getBody();
                JSONObject object = new JSONObject(returnString);
                JSONArray dataArray = object.getJSONArray("data");
                if (dataArray.length() > 0) {
                    authorizationCode = dataArray.getJSONObject(0);
                }
                Log.d("DEBUG", authorizationCode.getString("authorization_code"));

            } catch (Exception e) {
                Log.d("DEBUG", e.getMessage());
            }

        fetchAccessToken();
    }

    private void fetchAccessToken(){
        try {
            String path = context.getString(R.string.network_base_url)
                    + context.getString(R.string.network_token_endpoint);
            RestTemplate rt = restTemplate();

            MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
            paramMap.add("code", authorizationCode.getString("authorization_code"));
            paramMap.add("grant_type", "authorization_code");
            paramMap.add("redirect_uri", redirectUrl);
            HttpHeaders headers = getHeaders("token");
            Log.d("DEBUG", headers.toString());
            HttpEntity<?> request = new HttpEntity<Object>(paramMap, headers);


            ResponseEntity<String> response = rt.exchange(path, HttpMethod.POST, request, String.class);
            String responseString = response.getBody();
            JSONObject object = new JSONObject(responseString);
            JSONArray dataArray = object.getJSONArray("data");
            if (dataArray.length() > 0) {
                JSONObject authObject = dataArray.getJSONObject(0);
                accessToken = authObject.getString("access_token");
                double expires = authObject.getDouble("expires_in");
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.SECOND, (int)Math.floor(expires));
                tokenExpires = calendar.getTime();
            }
            Log.d("DEBUG", responseString);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void refreshToken() {
        try {
            String path = context.getString(R.string.network_base_url)
                    + context.getString(R.string.network_token_endpoint);
            RestTemplate rt = restTemplate();

            MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
            paramMap.add("refresh_token", refreshToken);
            paramMap.add("grant_type", "refresh_token");
            paramMap.add("redirect_uri", redirectUrl);
            HttpHeaders headers = getHeaders("token");
            HttpEntity<?> request = new HttpEntity<Object>(paramMap, headers);


            ResponseEntity<String> response = rt.exchange(path, HttpMethod.POST, request, String.class);
            String responseString = response.getBody();
            JSONObject object = new JSONObject(responseString);
            JSONArray dataArray = object.getJSONArray("data");
            if (dataArray.length() > 0) {
                JSONObject authObject = dataArray.getJSONObject(0);
                accessToken = authObject.getString("access_token");
                double expires = authObject.getDouble("expires_in");
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.SECOND, (int)Math.floor(expires));
                tokenExpires = calendar.getTime();
            }
            Log.d("DEBUG", responseString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getAuthorizationString(){
        if (authorizationString == null) {
            String baseString = clientID + ":" + clientSecret;
            byte[] data = null;
            try {
                data = baseString.getBytes("UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            authorizationString =  Base64.encodeToString(data, Base64.DEFAULT);
        }
        Log.d("DEBUG", authorizationString);
        return authorizationString;
    }

    private HttpHeaders getHeaders(String type) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpAuthentication authentication = new HttpAuthentication() {
            @Override
            public String getHeaderValue() {
                return "Basic " + getAuthorizationString();
            }
        };
        if (type == "token") {
            HttpAuthentication authHeader = new HttpBasicAuthentication(clientID, clientSecret);
            headers.setAuthorization(authHeader);
        } else if (type == "bearer") {
            HttpAuthentication authHeader = new HttpAuthentication() {
                @Override
                public String getHeaderValue() {
                    return "Bearer " + accessToken;
                }
            };
            headers.setAuthorization(authHeader);

        }

        return headers;
    }

    private Object performAuthorizedRequest(String path, Object params) {
        if (tokenExpires != null) {
            Calendar now = Calendar.getInstance();
            if (!now.getTime().before(tokenExpires)) {
                refreshToken();
            }
        } else {
            getToken();
        }
        try {
            path = context.getString(R.string.network_base_url)
                    + path;
            RestTemplate template = restTemplate();
            HttpHeaders headers = getHeaders("bearer");
            HttpEntity<?> request = new HttpEntity<Object>(params, headers);
            ResponseEntity<String> response = template.exchange(path, HttpMethod.POST, request, String.class);
            String responseString = response.getBody();
            JSONObject object = new JSONObject(responseString);
            Log.d("DEBUG", object.toString());
            return object;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getLogin(String email, String password){
        MultiValueMap<String, String> login = new LinkedMultiValueMap<>();
        login.add("email", email);
        login.add("password", password);
        JSONObject returnData = (JSONObject)performAuthorizedRequest("/oauth2/login", login);
        JSONObject userProfile = null;
        try {
            JSONArray dataArray = returnData.getJSONArray("data");
            if (dataArray.length() > 0) {
                userProfile = dataArray.getJSONObject(0);
                return new User(userProfile);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;

    }




}


