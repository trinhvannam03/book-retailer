package com.project.bookseller.dto;

import lombok.Data;

@Data
public class PaymentMethodDTO {
    private int paymentMethodId;
    private String paymentMethodName;
    private int subMethod;
}
