package com.fastbite.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemRequest {
    
    @NotBlank(message = "ProductId es requerido")
    private String productId;
    
    @Min(value = 1, message = "Cantidad debe ser al menos 1")
    private int quantity;
}
