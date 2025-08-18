package com.example.apibackend.payment;


import com.example.apibackend.user.User;
import com.example.apibackend.course.Course;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Getter
@Entity
@Table(
        name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payments_gateway_txn_id", columnNames = "gateway_txn_id")
        },
        indexes = {
                @Index(name = "idx_payments_user", columnList = "user_id, created_at"),
                @Index(name = "idx_payments_course", columnList = "course_id, created_at"),
                @Index(name = "idx_payments_created_at", columnList = "created_at")
        }
)

public class Payment {
    public static enum PaymentStatus { PENDING, SUCCESS, FAILED, REFUNDED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Setter
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // Gateway charge/payment ID (Stripe charge/intent, Razorpay payment, etc.)
    @Setter
    @Column(name = "gateway_txn_id", length = 64, unique = true)
    private String gatewayTxnId;

    @Setter
    @Min(0)
    @Column(name = "amount_cents", nullable = false)
    private Integer amountCents;

    @Setter
    @Pattern(regexp = "^[A-Za-z]{3}$")
    @Column(name = "currency", nullable = false, length = 3, columnDefinition = "char(3) default 'USD'")
    private String currency = "USD";

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16, columnDefinition = "enum('PENDING','COMPLETED','FAILED','REFUNDED')")
    private PaymentStatus status = PaymentStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Setter
    @Column(name = "refunded_at")
    private Instant refundedAt;


    @PrePersist @PreUpdate
    void normalize() {
        if (currency != null) currency = currency.toUpperCase();
    }
}