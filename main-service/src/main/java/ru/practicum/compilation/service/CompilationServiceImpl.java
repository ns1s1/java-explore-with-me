package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventRepository eventRepository;


    @Override
    public CompilationDto create(NewCompilationDto newCompilationDto) {
        Compilation compilation = compilationMapper.convertToCompilation(newCompilationDto);

        if (newCompilationDto.getEvents() != null) {
            List<Event> events = eventRepository.findAllById(newCompilationDto.getEvents());
            compilation.setEvents(events);
        }

        return compilationMapper.convertToCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    public CompilationDto update(Long compilationId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = getCompilationById(compilationId);

        if (updateCompilationRequest.getEvents() != null) {
            List<Event> events = eventRepository.findAllById(updateCompilationRequest.getEvents());
            compilation.setEvents(events);
        }
        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }

        return compilationMapper.convertToCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    public CompilationDto findBydId(Long compilationId) {
        return compilationMapper.convertToCompilationDto(getCompilationById(compilationId));
    }

    @Override
    public List<CompilationDto> findAll(Boolean pinned, int from, int size) {
        Pageable page = PageRequest.of(from / size, size, Sort.by("id").descending());

        if (pinned != null) {
            List<Compilation> compilations = compilationRepository.findAllByPinned(pinned, page);
            return compilations.stream()
                    .map(compilationMapper::convertToCompilationDto)
                    .collect(Collectors.toList());
        } else {
            Page<Compilation> compilations = compilationRepository.findAll(page);
            return compilations.stream()
                    .map(compilationMapper::convertToCompilationDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void delete(Long compilationId) {
        getCompilationById(compilationId);
        compilationRepository.deleteById(compilationId);
    }

    public Compilation getCompilationById(Long compilationId) {
        return compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Подборка событий не найдена"));
    }
}
