package com.eeki.adminservice.dto;

import com.eeki.adminservice.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private Boolean phoneVerified;
    private Boolean emailVerified;
    private Boolean active;
    private UserRole role;
    private LocalDateTime createdAt;
}
