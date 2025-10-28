package com.lean.lean.repository;

import com.lean.lean.dao.LeanUser;
import com.lean.lean.dao.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeanUserRepository extends JpaRepository<LeanUser, Long> {
    LeanUser findByUser(User user);
}
