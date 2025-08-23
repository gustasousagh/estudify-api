package com.gustasousagh.studify.estudify.integrations.client.impl;

import com.gustasousagh.studify.estudify.integrations.client.StripeClient;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.stereotype.Service;

@Service
public class StripeClientImpl implements StripeClient {

    @Override
    public Customer createCustomer(String email, String name) {
        try {
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setEmail(email)
                    .setName(name)
                    .putMetadata("app", "minha-app")
                    .build();

            return Customer.create(params);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar customer no Stripe", e);
        }
    }

    @Override
    public String createCheckoutSession(String customerId, String priceId,
                                        String successUrl, String cancelUrl) {
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setCustomer(customerId)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPrice(priceId)
                                    .setQuantity(1L)
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);
            return session.getUrl();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar checkout session no Stripe", e);
        }
    }
}
