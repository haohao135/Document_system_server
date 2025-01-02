package com.document.demo.service.impl;

import com.document.demo.service.OtpService;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class OtpServiceImpl implements OtpService {
    public String generateNumberOtp(int length) {
        Random random = new Random();
        StringBuilder otp = new StringBuilder(length);

        for(int i = 0; i < length; i++){
            char digit = (char) (random.nextInt(10) + '0');
            otp.append(digit);
        }

        return otp.toString();
    }

    public String generateCharacterOtp(int length) {
        Random random = new Random();
        StringBuilder otp = new StringBuilder(length);

        // char with uppercase & lowercase
        for(int i = 0; i < length; i++){
            char character = (char) (random.nextInt(26) + 'a');
            otp.append(character);
        }

        return otp.toString();
    }
}
