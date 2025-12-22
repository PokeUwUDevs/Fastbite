package com.fastbite.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    
    @NotBlank(message = "Dirección de entrega es requerida")
    private String deliveryAddress;
    
    private String notes;
    
    @NotEmpty(message = "Debe incluir al menos un producto")
    @Valid
    private List<CartItemRequest> items;
}
