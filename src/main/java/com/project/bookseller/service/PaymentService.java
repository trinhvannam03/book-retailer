package com.project.bookseller.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {
    String VNP_HASH_SECRET = "2UKWTXMGREIJOC6O6XG2J35QXVTRQU73";

    //create redirect url to vnpay payment gateway
    public String createRedirectUrl(String vnpAmount, String vnpBankCode) throws NoSuchAlgorithmException, InvalidKeyException {
        String vnpVersion = "2.1.0";
        String vnpCommand = "pay";
        String vnpTxnRef = "99128812";
        String vnpIpAddr = "118.70.131.111";
        String vnpTmnCode = "OVKDWLES";
        String vnpOrderInfo = "order_payment";
        String vnpOrderType = "other";
        String vnpLocale = "vn";
        int amount = Integer.parseInt(vnpAmount) * 100;
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnpVersion);
        vnp_Params.put("vnp_Command", vnpCommand);
        vnp_Params.put("vnp_TmnCode", vnpTmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        if (vnpBankCode != null && !vnpBankCode.isEmpty()) {
            vnp_Params.put("vnp_BankCode", vnpBankCode);
        }
        vnp_Params.put("vnp_TxnRef", vnpTxnRef);
        vnp_Params.put("vnp_OrderInfo", vnpOrderInfo);
        vnp_Params.put("vnp_OrderType", vnpOrderType);
        vnp_Params.put("vnp_Locale", vnpLocale);
        vnp_Params.put("vnp_ReturnUrl", "http://localhost:3000/order/fallback");
        vnp_Params.put("vnp_IpAddr", vnpIpAddr);
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                        .append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKey = new SecretKeySpec(VNP_HASH_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        sha512_HMAC.init(secretKey);
        byte[] hashBytes = sha512_HMAC.doFinal(queryUrl.getBytes(StandardCharsets.UTF_8));

        StringBuilder secureHash = new StringBuilder();
        for (byte b : hashBytes) {
            secureHash.append(String.format("%02x", b));
        }
        queryUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?" + queryUrl + "&vnp_SecureHash=" + secureHash;
        return queryUrl;
    }
}
