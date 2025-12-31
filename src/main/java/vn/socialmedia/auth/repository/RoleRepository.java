package vn.socialmedia.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.socialmedia.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
