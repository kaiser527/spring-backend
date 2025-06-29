package com.kaiser.spring_backend.controllers;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.kaiser.spring_backend.dto.reponse.ApiResponse;
import com.kaiser.spring_backend.dto.reponse.PaginatedResponse;
import com.kaiser.spring_backend.dto.reponse.PermissionResponse;
import com.kaiser.spring_backend.dto.request.PermissionRequest;
import com.kaiser.spring_backend.services.PermissionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("permission")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE, makeFinal = true)
public class PermissionController {
    PermissionService permissionService;

    @PostMapping
    ApiResponse<PermissionResponse> createPermission(@RequestBody @Valid PermissionRequest request){
        PermissionResponse result = permissionService.createPermission(request);

        return ApiResponse.<PermissionResponse>builder()
            .message("Create permission")
            .result(result)
            .build();
    }

    @PatchMapping("/{id}")
    ApiResponse<PermissionResponse> updatePermission(@PathVariable("id") String id, @RequestBody @Valid PermissionRequest request ){
        PermissionResponse result = permissionService.updatePermission(id, request);

        return ApiResponse.<PermissionResponse>builder()
            .message("Update permission")
            .result(result)
            .build();
    }

    @GetMapping
    ApiResponse<PaginatedResponse<PermissionResponse>> getPermissionPaginated(@RequestParam int pageSize, @RequestParam int pageNumber){
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        PaginatedResponse<PermissionResponse> result = permissionService.getPermissionPaginated(pageable);

        return ApiResponse.<PaginatedResponse<PermissionResponse>>builder()
            .message("Fetch permission paginate")
            .result(result)
            .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<PermissionResponse> deletePermission(@PathVariable("id") String id){
        PermissionResponse result = permissionService.deletePermission(id);

        return ApiResponse.<PermissionResponse>builder()
            .message("Delete permission")
            .result(result)
            .build();
    }
}
