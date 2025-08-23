package com.gustasousagh.studify.estudify.integrations.client;

import com.stripe.model.Customer;

public interface StripeClient {
    Customer createCustomer(String email, String name);
    String createCheckoutSession(String customerId, String priceId, String successUrl, String cancelUrl);
}
