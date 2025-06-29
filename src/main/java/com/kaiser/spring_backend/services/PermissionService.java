package com.kaiser.spring_backend.services;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.kaiser.spring_backend.dto.reponse.PaginatedResponse;
import com.kaiser.spring_backend.dto.reponse.PermissionResponse;
import com.kaiser.spring_backend.dto.request.PermissionRequest;
import com.kaiser.spring_backend.entities.Permission;
import com.kaiser.spring_backend.exception.AppException;
import com.kaiser.spring_backend.exception.ErrorCode;
import com.kaiser.spring_backend.mapper.PermissionMapper;
import com.kaiser.spring_backend.repositories.PermissionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE, makeFinal = true)
public class PermissionService {
    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;

    public PermissionResponse createPermission(PermissionRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean check = permissionRepository.existsByApiPathAndMethod(request.getApiPath(), request.getMethod());
        if(check){
            throw new AppException(ErrorCode.PERMISSION_EXIST);
        }

        Permission permission = permissionMapper.toCreatePermission(request);
        permission.setCreatedBy(authentication.getName());

        permissionRepository.save(permission);

        return permissionMapper.toPermissionResponse(permission);
    }

    public PermissionResponse updatePermission(String id, PermissionRequest request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Permission permission = permissionRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_EXIST));

        boolean check = permissionRepository.existsByApiPathAndMethodAndIdNot(request.getApiPath(), request.getMethod(), id);

        if(check){
            throw new AppException(ErrorCode.PERMISSION_EXIST);
        }

        permissionMapper.toUpdatePermission(permission, request);
        permission.setUpdatedBy(authentication.getName());

        permissionRepository.save(permission);

        return permissionMapper.toPermissionResponse(permission);
    }

    public PaginatedResponse<PermissionResponse> getPermissionPaginated(Pageable pageable){
        Page<Permission> permissionPage = permissionRepository.findAll(pageable);

        List<PermissionResponse> permissionResponses = permissionPage.getContent()
            .stream()
            .map(permissionMapper::toPermissionResponse)
            .toList();

        return PaginatedResponse.<PermissionResponse>builder()
            .pageNumber(permissionPage.getNumber() + 1)
            .pageSize(permissionPage.getSize())
            .totalPages(permissionPage.getTotalPages())
            .data(permissionResponses)
            .build();
    }

    public PermissionResponse deletePermission(String id){
        Permission permission = permissionRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_EXIST));

        permissionRepository.delete(permission);

        return permissionMapper.toPermissionResponse(permission);
    }
}
