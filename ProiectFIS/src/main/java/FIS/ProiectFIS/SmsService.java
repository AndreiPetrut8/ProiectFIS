package FIS.ProiectFIS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    @Autowired
    private EmailService emailService;

    public void sendOtp(String phoneNumber, String otp) {
        String carrierGateway = phoneNumber + "@digi-mobil.ro";
        emailService.sendOtp(carrierGateway, otp);
        
        System.out.println("SMS (via email gateway) sent to " + carrierGateway);
    }
}
