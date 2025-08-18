package com.example.apibackend.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * StripeConfig initializes Stripe.apiKey from application config at startup.
 * Use only test mode keys in development. Never hardcode or commit production secrets.
 * For production, load secrets from environment variables or a secure vault.
 */
@Configuration
@RequiredArgsConstructor
public class StripeConfig {
    @Value("${stripe.secretKey}")
    private String stripeSecretKey;

    @PostConstruct
    public void initStripe() {
        Stripe.apiKey = stripeSecretKey;
    }
}

