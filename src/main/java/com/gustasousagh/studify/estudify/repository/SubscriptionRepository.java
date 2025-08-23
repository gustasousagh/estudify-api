package com.gustasousagh.studify.estudify.repository;

import com.gustasousagh.studify.estudify.entity.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {

    SubscriptionEntity findByUserId(String userId);
    SubscriptionEntity findByStripeCustomerId(String stripeCustomerId);
    SubscriptionEntity findByStripeSubscriptionId(String stripeSubscriptionId);
}
