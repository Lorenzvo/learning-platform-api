package com.example.apibackend.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartCheckoutResponseDTO {
    private List<CartPaymentDTO> payments;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CartPaymentDTO {
        private Long paymentId;
        private Long courseId;
        private Integer amount;
        private String currency;
        private String status;
        private String clientSecret;
    }
}

