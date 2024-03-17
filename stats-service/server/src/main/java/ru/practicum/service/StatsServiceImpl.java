package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    private final EndpointHitMapper endpointHitMapper;

    @Override
    @Transactional
    public void create(EndpointHitDto endpointHitDto) {
        statsRepository.save(endpointHitMapper.convertToEndpointHit(endpointHitDto));
    }

    @Override
    public List<ViewStatsDto> getStatistics(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (end.isBefore(start)) {
            throw new ValidationException("Время завершения раньше начала");
        } else if (start.isEqual(end)) {
            throw new ValidationException("Время завершения равно времени начала");
        }

        if (unique) {
            if (uris == null || uris.isEmpty()) {
                return statsRepository.getUniqueIpRequestsWithoutUri(start, end);
            }
            return statsRepository.getUniqueIpRequestsWithUri(start, end, uris);
        } else {
            if (uris == null || uris.isEmpty()) {
                return statsRepository.getAllRequestsWithoutUri(start, end);
            }
            return statsRepository.getAllRequestsWithUri(start, end, uris);
        }
    }
}
