package com.kaiser.spring_backend.dto.request;

import jakarta.validation.constraints.Size;
import com.kaiser.spring_backend.constants.AccountType;
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
}
