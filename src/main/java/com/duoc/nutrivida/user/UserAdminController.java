package com.duoc.nutrivida.user;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class UserAdminController {

    private final UserQueryService queryService;

    public UserAdminController(UserQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    public ResponseEntity<PagedModel<UserAdminResponse>> list(
            @RequestParam(required = false) Boolean enabled,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<UserAdminResponse> page = queryService.list(enabled, pageable);
        return ResponseEntity.ok(new PagedModel<>(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserAdminResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(queryService.getById(id));
    }
}