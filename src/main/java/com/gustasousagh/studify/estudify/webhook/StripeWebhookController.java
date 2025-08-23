package com.gustasousagh.studify.estudify.webhook;

import com.gustasousagh.studify.estudify.services.UserPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final UserPaymentService paymentService;

    @PostMapping(consumes = "application/json")
    public ResponseEntity<String> handleStripeEvent(
            @RequestBody String payload,
            @RequestHeader(name = "Stripe-Signature", required = false) String sigHeader) {

        try {
            paymentService.handlePaymentSuccessWebhook(payload, sigHeader);
        } catch (Exception e) {
            // não propaga 500 pro Stripe; só loga
            e.printStackTrace();
        }
        return ResponseEntity.ok("success");
    }
}
