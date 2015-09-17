package co.higheraltitude.prizm.network;

import android.content.Context;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpAuthentication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpAuthentication;
import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpMethod;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import android.os.Handler;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;

import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.cache.PrizmCache;

/**
 * Created by boonej on 9/3/15.
 */
public class PrizmAPIService {

    private static final PrizmAPIService instance = new PrizmAPIService();
    protected Context context;

    private final String clientID = "67e1fe4f-db1b-4d5c-bdc7-56270b0832e2";
    private final String clientSecret = "f27198fb-689d-4965-acb0-0e9c5d61ddec";
    private final String redirectURL = "https://api.prizmapp.com/callback";

    private String authorizationCode;
    private String authorizationString;
    private String accessToken;
    private String refreshToken;
    private Date tokenExpires;
    private RestTemplate restTemplate;
    private String currentUserID;
    private AWSCredentialsProvider mCredentialsProvider;

    private PrizmAPIService() {

    }

    private RestTemplate restTemplate() {
        if (restTemplate == null) {
            restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
            restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
            restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());
        }
        return restTemplate;
    }

    public static PrizmAPIService getInstance() {
        instance.loadData();
        return instance;
    }

    public static void registerContext(Context context){
        instance.context = context;
    }



    private void storeTokens() {
        PrizmCache cache = PrizmCache.getInstance();
        if (accessToken != null)
            cache.objectCache.put("network_access_token", accessToken);
        if (refreshToken != null)
            cache.objectCache.put("network_refresh_token", refreshToken);
        if (authorizationCode != null)
            cache.objectCache.put("network_authorization_code", authorizationCode);
        if (tokenExpires != null)
            cache.objectCache.put("network_token_expires", tokenExpires);
        if (currentUserID != null)
            cache.objectCache.put("current_user_id", currentUserID);
    }

    private void loadData() {
        PrizmCache cache = PrizmCache.getInstance();
        try {
            accessToken = (String) cache.objectCache.get("network_access_token");
            refreshToken = (String) cache.objectCache.get("network_refresh_token");
            authorizationCode = (String) cache.objectCache.get("network_authorization_code");
            currentUserID = (String) cache.objectCache.get("current_user_id");
            tokenExpires = (Date) cache.objectCache.get("network_token_expires");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void authorize() throws JSONException{
        String path = context.getString(R.string.network_base_url)
                + context.getString(R.string.network_auth_endpoint)
                + "?client_id=" + clientID + "&redirect_uri=" + redirectURL
                + "&response_type=code";
        RestTemplate template = restTemplate();
        HttpEntity<String> request = new HttpEntity<>(getHeaders("none"));
        ResponseEntity<String> response = template.exchange(path, HttpMethod.GET, request, String.class);
        String returnString = response.getBody();
        if (returnString != null && !returnString.isEmpty()) {
            JSONObject object = new JSONObject(returnString);
            JSONArray dataArray = object.getJSONArray("data");
            if (dataArray.length() > 0) {
                authorizationCode = dataArray.getJSONObject(0).getString("authorization_code");
                Log.d("DEBUG", authorizationCode);
            }
        }

    }

    private void getToken() throws JSONException {
        String path = context.getString(R.string.network_base_url)
                + context.getString(R.string.network_token_endpoint);
        RestTemplate template = restTemplate();
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("code", authorizationCode);
        paramMap.add("grant_type", "authorization_code");
        paramMap.add("redirect_uri", redirectURL);
        HttpHeaders headers = getHeaders("token");
        HttpEntity<?> request = new HttpEntity<Object>(paramMap, headers);
        ResponseEntity<String> response = template.exchange(path, HttpMethod.POST, request, String.class);
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
        storeTokens();
    }

    private void refreshToken() throws JSONException {
        String path = context.getString(R.string.network_base_url)
                + context.getString(R.string.network_token_endpoint);
        RestTemplate rt = restTemplate();

        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("refresh_token", refreshToken);
        paramMap.add("grant_type", "refresh_token");
        paramMap.add("redirect_uri", redirectURL);
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
        storeTokens();
    }

    private String getAuthorizationString() throws UnsupportedEncodingException{
        if (authorizationString == null) {
            String baseString = clientID + ":" + clientSecret;
            byte[] data = null;
            data = baseString.getBytes("UTF-8");
            authorizationString =  Base64.encodeToString(data, Base64.DEFAULT);
        }
        Log.d("DEBUG", authorizationString);
        return authorizationString;
    }

    private HttpHeaders getHeaders(String type) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

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

    public void performAuthorizedRequest(String path, final MultiValueMap<String, String> parameters, final HttpMethod method, final Handler handler) {
        final String urlPath = context.getString(R.string.network_base_url) + path;
        Thread thread = new Thread(){
            @Override
            public void run() {
                JSONObject object = null;
                if (accessToken == null || refreshToken == null) {
                    try {
                        authorize();
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                        return;
                    }
                }
                if (tokenExpires != null && refreshToken != null) {
                    Calendar now = Calendar.getInstance();
                    if (!now.getTime().before(tokenExpires)) {
                        try {
                            refreshToken();
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                            return;
                        }
                    }
                } else {
                    try {
                        getToken();
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                        return;
                    }
                }
                RestTemplate template = restTemplate();
                HttpHeaders headers = getHeaders("bearer");
                HttpEntity<?> request = new HttpEntity<Object>(parameters, headers);
                try {
                    ResponseEntity<String> response = template.exchange(urlPath, method, request, String.class);

                    String responseString = response.getBody();
                    try {
                        object = new JSONObject(responseString);
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                        return;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                Message message = handler.obtainMessage(1, object);
                handler.sendMessage(message);
            }
        };
        thread.start();
    }


}
