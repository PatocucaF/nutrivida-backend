package com.duoc.nutrivida.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    Page<User> findByDeletedAtIsNull(Pageable pageable);

    Page<User> findByEnabledAndDeletedAtIsNull(boolean enabled, Pageable pageable);

    long countByRoles_NameAndEnabledTrueAndDeletedAtIsNull(RoleName name);
}