package com.kaiser.spring_backend.dto.request;

import com.kaiser.spring_backend.constants.ApiMethod;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PermissionRequest {
    String name;

    @Pattern(regexp = "^/api/v1/[a-zA-Z0-9_-]+(/.*)?$", message = "INVALID_API_PATH")
    String apiPath;

    ApiMethod method;

    String module;
}
