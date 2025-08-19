package com.example.apibackend.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartCheckoutResponseDTO {
    private Long paymentId;
    private Integer totalAmount;
    private String currency;
    private String status;
    private String clientSecret;
    private List<Long> courseIds;
}
