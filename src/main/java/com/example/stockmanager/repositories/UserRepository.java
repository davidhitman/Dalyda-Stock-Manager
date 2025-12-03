package com.example.stockmanager.repositories;

import com.example.stockmanager.entities.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<Users, UUID> {

    @Query("SELECT u FROM Users u WHERE u.email = :email")
    Optional<Users> findByEmail(@Param("email") String email);

    @Query("SELECT COUNT(s) > 0 FROM Users s WHERE s.email = :email")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT u FROM Users u WHERE u.id = :id")
    Optional<Users> findUserById(@Param("id") UUID id);

    @Query("SELECT u FROM Users u")
    Page<Users> findAllUsers(Pageable pageable);

}
