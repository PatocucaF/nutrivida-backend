package com.duoc.nutrivida.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserQueryService {

    private final UserRepository userRepository;
    private final UserService userService;

    public UserQueryService(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public Page<UserAdminResponse> list(Boolean enabled, Pageable pageable) {
        Page<User> page = (enabled == null)
                ? userRepository.findByDeletedAtIsNull(pageable)
                : userRepository.findByEnabledAndDeletedAtIsNull(enabled, pageable);
        return page.map(UserAdminResponse::from);
    }

    public UserAdminResponse getById(Long id) {
        return UserAdminResponse.from(userService.findById(id));
    }
}