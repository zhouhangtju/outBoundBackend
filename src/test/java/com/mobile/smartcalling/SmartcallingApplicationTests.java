package com.mobile.smartcalling;


import com.mobile.smartcalling.util.XAccessSignUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.UnsupportedEncodingException;

import static org.apache.commons.codec.digest.DigestUtils.md5;

@SpringBootTest
@Slf4j
class SmartcallingApplicationTests {

    @Test
    void test() {
        String aaaaa = DigestUtils.md5Hex("aaaaa");
        byte[] aaaaas = md5("aaaaa");
        System.out.println(aaaaa);
        System.out.println(aaaaas);
    }

}
