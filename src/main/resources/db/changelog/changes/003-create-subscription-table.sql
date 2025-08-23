CREATE TABLE IF NOT EXISTS subscription (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    external_user_id VARCHAR(255) NOT NULL UNIQUE,
    stripe_customer_id VARCHAR(255),
    stripe_subscription_id VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'pending',
    current_period_start TIMESTAMP NULL,
    current_period_end TIMESTAMP NULL,
    canceled_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uniq_stripe_subscription (stripe_subscription_id),
    INDEX idx_external_user (external_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
