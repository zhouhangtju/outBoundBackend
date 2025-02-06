package com.mobile.smartcalling.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;

import static org.apache.commons.codec.digest.DigestUtils.md5;
@Slf4j
public class XAccessSignUtil {

    public static String getXAccessSign(String openapi_secret) {
        try {
            String x_access_sing = Base64.encodeBase64String(("x-access-key" + md5(openapi_secret)).getBytes("UTF-8"));
            return x_access_sing;
        } catch (UnsupportedEncodingException e) {
            log.info("getXAccessSign error{}",e);
            return null;
        }
    }
}
