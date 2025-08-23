package com.gustasousagh.studify.estudify.controllers.user;

import com.gustasousagh.studify.estudify.model.response.CheckoutSessionUrl;
import com.gustasousagh.studify.estudify.model.response.SubscriptionPlanResponse;
import com.gustasousagh.studify.estudify.services.UserPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class UserPaymentController {

    private final UserPaymentService paymentService;

    @PostMapping("/create-checkout-session")
    public ResponseEntity<CheckoutSessionUrl> createCheckoutSession() {
        String sessionUrl = paymentService.createCheckoutSession();
        CheckoutSessionUrl response = new CheckoutSessionUrl();
        response.setUrl(sessionUrl);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/subscription-plan")
    public ResponseEntity<SubscriptionPlanResponse> getSubscriptionPlan() {
        SubscriptionPlanResponse plan = paymentService.getSubscriptionPlan();
        return new ResponseEntity<>(plan, HttpStatus.OK);
    }
}
