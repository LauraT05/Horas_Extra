package com.horasextra.api.repository;

import com.horasextra.api.entity.OvertimeRequest;
import com.horasextra.api.entity.OvertimeRequestStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OvertimeRequestRepository extends JpaRepository<OvertimeRequest, Long> {

    List<OvertimeRequest> findByEmployeeId(Long employeeId);

    List<OvertimeRequest> findByStatusAndCoordinatorNotifiedAtIsNull(OvertimeRequestStatus status);
}
