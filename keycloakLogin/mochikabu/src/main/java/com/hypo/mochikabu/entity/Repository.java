package com.hypo.mochikabu.entity;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface Repository extends JpaRepository<User, Integer>{
    List<User> findByUsername(String username);
}
