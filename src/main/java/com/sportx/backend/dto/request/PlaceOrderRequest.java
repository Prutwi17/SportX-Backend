package com.sportx.backend.dto.request;

import com.sportx.backend.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlaceOrderRequest {

    @NotNull
    private Long addressId;

    @NotNull
    private PaymentMethod paymentMethod;
}
