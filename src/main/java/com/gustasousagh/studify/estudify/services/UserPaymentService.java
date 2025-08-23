package com.gustasousagh.studify.estudify.services;

import com.gustasousagh.studify.estudify.model.response.SubscriptionPlanResponse;

public interface UserPaymentService {
    String createCheckoutSession();
    void handlePaymentSuccessWebhook(String payload, String signatureHeader);
    SubscriptionPlanResponse getSubscriptionPlan();
}
