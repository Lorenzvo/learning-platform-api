package com.example.apibackend.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO returned to frontend for single course checkout.
 * Used for Stripe payment confirmation and displaying line item details.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutResponseDTO {
    /** Payment row ID in database */
    private Long paymentId;
    /** Stripe client secret for confirming payment on frontend */
    private String clientSecret;
    /** Stripe PaymentIntent ID (gatewayTxnId) */
    private String piId;
    /** Course ID being purchased */
    private Long courseId;
    /** Course title for display */
    private String courseTitle;
    /** Price in cents for this course */
    private Integer priceCents;
    /** ISO currency code (e.g. USD) */
    private String currency;
    /** Payment status (PENDING, SUCCESS, etc.) */
    private String status;
}
