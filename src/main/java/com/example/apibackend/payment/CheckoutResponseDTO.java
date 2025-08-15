package com.example.apibackend.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutResponseDTO {
    private Long paymentId;
    private String clientSecret;
    private Integer amount;
    private String currency;
    private String status;
}

