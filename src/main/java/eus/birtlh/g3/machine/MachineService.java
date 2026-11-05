package eus.birtlh.g3.machine;

import eus.birtlh.g3.common.dto.PageResponse;
import eus.birtlh.g3.machine.dto.MachineResponse;
import eus.birtlh.g3.machine.mapper.MachineMapper;
import eus.birtlh.g3.machine.repository.MachineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MachineService {

    private final MachineRepository machineRepository;

    @Transactional(readOnly = true)
    public PageResponse<MachineResponse> getAllMachines(int page, int size) {
        Page<Machine> machines = machineRepository.findAll(
                PageRequest.of(page, size, Sort.by("id").ascending())
        );

        return new PageResponse<>(
                machines.getContent()
                        .stream()
                        .map(MachineMapper::toResponse)
                        .toList(),
                machines.getNumber(),
                machines.getSize(),
                machines.getTotalElements(),
                machines.getTotalPages()
        );
    }
}
