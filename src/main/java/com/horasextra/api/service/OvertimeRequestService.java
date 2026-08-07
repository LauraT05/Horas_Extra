package com.horasextra.api.service;

import com.horasextra.api.dto.CreateOvertimeRequestDto;
import com.horasextra.api.entity.Coordinator;
import com.horasextra.api.entity.Employee;
import com.horasextra.api.entity.OvertimeRequest;
import com.horasextra.api.entity.OvertimeRequestStatus;
import com.horasextra.api.exception.ResourceNotFoundException;
import com.horasextra.api.repository.CoordinatorRepository;
import com.horasextra.api.repository.EmployeeRepository;
import com.horasextra.api.repository.OvertimeRequestRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OvertimeRequestService {

    private final OvertimeRequestRepository overtimeRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final CoordinatorRepository coordinatorRepository;

    public OvertimeRequestService(OvertimeRequestRepository overtimeRequestRepository,
                                   EmployeeRepository employeeRepository,
                                   CoordinatorRepository coordinatorRepository) {
        this.overtimeRequestRepository = overtimeRequestRepository;
        this.employeeRepository = employeeRepository;
        this.coordinatorRepository = coordinatorRepository;
    }

    @Transactional
    public OvertimeRequest create(CreateOvertimeRequestDto dto) {
        Employee employee = employeeRepository.findById(dto.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("No existe el empleado " + dto.employeeId()));
        Coordinator coordinator = coordinatorRepository.findById(dto.coordinatorId())
                .orElseThrow(() -> new ResourceNotFoundException("No existe el coordinador " + dto.coordinatorId()));

        OvertimeRequest request = new OvertimeRequest();
        request.setEmployee(employee);
        request.setCoordinator(coordinator);
        request.setPeriodYear(dto.periodYear());
        request.setPeriodMonth(dto.periodMonth());
        request.setPeriodFortnight(dto.periodFortnight());
        request.setHoursRequested(dto.hoursRequested());
        request.setJustification(dto.justification());
        request.setStatus(OvertimeRequestStatus.PENDING_COORDINATOR);

        return overtimeRequestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<OvertimeRequest> findByEmployee(Long employeeId) {
        return overtimeRequestRepository.findByEmployeeId(employeeId);
    }

    @Transactional(readOnly = true)
    public OvertimeRequest findById(Long id) {
        return overtimeRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la solicitud " + id));
    }
}
