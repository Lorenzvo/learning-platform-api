package com.example.apibackend.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO returned to frontend for cart checkout (bulk purchase).
 * Contains a list of line items for Stripe confirmation and display.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartCheckoutResponseDTO {
    /** List of line items in the cart, each with payment and course info */
    private List<CartPaymentItemDTO> items;

    /**
     * DTO for each cart payment item (course)
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CartPaymentItemDTO {
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
}
