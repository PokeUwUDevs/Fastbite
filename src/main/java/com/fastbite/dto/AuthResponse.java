package com.fastbite.dto;

import com.fastbite.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String token;
    
    private String userId;
    
    private String email;
    
    private String name;
    
    private Role role;
}
