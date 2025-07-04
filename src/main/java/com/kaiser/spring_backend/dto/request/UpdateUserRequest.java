package com.kaiser.spring_backend.dto.request;

import com.kaiser.spring_backend.enums.AccountType;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateUserRequest {
    @Size(min = 4, message = "USERNAME_INVALID")
    String username;

    String image;

    Boolean isActive;

    AccountType accountType;

    String roleId;
}
