package com.rheotv.android.data.network.codashop;

import android.os.Build;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Singleton;

@Singleton
public class CodaShopManager {
    private static volatile CodaShopManager codaShopManager;
    public static final String HMAC_SHA256 = "HmacSHA256";
    private static final String xApiKey = "YOUR_API_KEY";
    private static final String clientId = "YOUR_CLIENT_KEY";
    private static final String secret = "YOUR_SECRET";

    // voucher
    public static final String METHOD_PLACE_ORDER = "placeOrder";
    public static final String METHOD_GET_ORDER = "getOrder";

    // top-up
    public static final String METHOD_VALIDATE = "validate";
    public static final String METHOD_TUP_UP = "topup";
    public static final String METHOD_LIST_SERVER = "listServer";

    // common
    public static final String METHOD_LIST_SKU = "listSku";

    private CodaShopManager() {

    }

    public static CodaShopManager getInstance() {
        if (codaShopManager == null) {
            synchronized (CodaShopManager.class) {
                if (codaShopManager == null)
                    codaShopManager = new CodaShopManager();
            }
        }

        return codaShopManager;
    }

    public String getAuthToken(String method, @Nullable JSONObject payloadParam) {
        try {
            String encodedHeader = getHeader();
            String encodedPayload = getPayload(method, payloadParam);

            String token = encodedHeader + "." + encodedPayload;
            System.out.println("encodedHeader.encodedPayload: " + token);

            String signature = generate(token, secret);
            System.out.println("signature: " + signature);

            token = token + "." + signature;
            System.out.println("token: " + token);
            return token;
        } catch (NoSuchAlgorithmException | InvalidKeyException | JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String generate(final String data, final String key) throws NoSuchAlgorithmException, InvalidKeyException {
        if (key == null || data == null) throw new NullPointerException();
        final Mac hMacSHA256 = Mac.getInstance(HMAC_SHA256);
        byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
        final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, HMAC_SHA256);
        hMacSHA256.init(secretKey);
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        byte[] res = hMacSHA256.doFinal(dataBytes);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(res);
        } else {
            return android.util.Base64.encodeToString(res, android.util.Base64.NO_PADDING);
        }
    }

    private String getHeader() throws JSONException {
        JSONObject headersJson = new JSONObject();
        headersJson.put("alg", "HS256");
        headersJson.put("typ", "JWT");
        headersJson.put("x-api-key", xApiKey);
        headersJson.put("x-api-version", "2.0");
        headersJson.put("x-client-id", clientId);
        String headers = headersJson.toString();
        System.out.println("headers: " + headers);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(headers.getBytes(StandardCharsets.UTF_8));
        } else {
            return android.util.Base64.encodeToString(headers.getBytes(StandardCharsets.UTF_8), android.util.Base64.NO_PADDING);
        }
    }

    private String getPayload(String method, @Nullable JSONObject baseParams) throws JSONException {
        long iat = System.currentTimeMillis();
        JSONObject payloadJson = new JSONObject();
        payloadJson.put("jsonrpc", "2.0");
        payloadJson.put("id", "ID-TEST-" + iat);
        payloadJson.put("method", method);
        if (method.equals(METHOD_PLACE_ORDER))
            payloadJson.put("customerId", "");
        else if (method.equals(METHOD_VALIDATE) || method.equals(METHOD_TUP_UP)) {
            payloadJson.put("userAccount", "");
            payloadJson.put("customerId", "");

        }


        JSONObject paramsJson = baseParams != null ? baseParams : new JSONObject();
        paramsJson.put("iat", iat);
        payloadJson.put("params", paramsJson);
        String payload = payloadJson.toString();
        System.out.println("payload: " + payload);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        } else {
            return android.util.Base64.encodeToString(payload.getBytes(StandardCharsets.UTF_8), android.util.Base64.NO_PADDING);
        }
    }
}
