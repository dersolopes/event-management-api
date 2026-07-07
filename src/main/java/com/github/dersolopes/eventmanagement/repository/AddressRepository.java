package com.github.dersolopes.eventmanagement.repository;

import com.github.dersolopes.eventmanagement.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {
}
