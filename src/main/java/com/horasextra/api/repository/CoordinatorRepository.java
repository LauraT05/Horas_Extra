package com.horasextra.api.repository;

import com.horasextra.api.entity.Coordinator;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoordinatorRepository extends JpaRepository<Coordinator, Long> {

    List<Coordinator> findByActiveTrue();
}
