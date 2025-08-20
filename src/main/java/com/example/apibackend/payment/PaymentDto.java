package com.example.apibackend.payment;

import java.time.Instant;

public class PaymentDto {
    public Long id;
    public Instant date;
    public Integer amountCents;
    public String userEmail;
    public String status;
    public String currency;
    public String gatewayTxnId;
    public String courseSlug;

    public PaymentDto(Long id, Instant date, Integer amountCents, String userEmail, String status, String currency, String gatewayTxnId, String courseSlug) {
        this.id = id;
        this.date = date;
        this.amountCents = amountCents;
        this.userEmail = userEmail;
        this.status = status;
        this.currency = currency;
        this.gatewayTxnId = gatewayTxnId;
        this.courseSlug = courseSlug;
    }
}

