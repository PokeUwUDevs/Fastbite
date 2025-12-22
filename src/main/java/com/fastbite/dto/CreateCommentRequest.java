package com.fastbite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {
    
    @NotBlank(message = "Mensaje es requerido")
    @Size(max = 500, message = "Mensaje no puede exceder 500 caracteres")
    private String message;
}
