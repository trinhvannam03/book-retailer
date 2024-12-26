package com.project.bookseller.dto.order;

import lombok.Data;

@Data
public class PaymentMethodDTO {
    private int paymentMethodId;
    private String paymentMethodName;
    private int subMethod;
}
