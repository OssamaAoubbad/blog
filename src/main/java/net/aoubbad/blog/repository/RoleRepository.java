package net.aoubbad.blog.repository;

import net.aoubbad.blog.entity.Role;
import net.aoubbad.blog.entity.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {


    Optional<Role> findByName(RoleName name);

    boolean existsByName(RoleName name);
}