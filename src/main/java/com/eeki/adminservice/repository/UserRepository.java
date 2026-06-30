package com.eeki.adminservice.repository;

import com.eeki.adminservice.entity.User;
import com.eeki.adminservice.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findByEmailIgnoreCase(String email);
    
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    
    Long countByActive(Boolean active);
    Long countByRole(UserRole role);
    List<User> findByRole(UserRole role);
}
