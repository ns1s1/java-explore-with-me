package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ViewStatsDto;
import ru.practicum.model.ViewStats;

@Mapper(componentModel = "spring")
public interface StatsMapper {
    ViewStatsDto v(ViewStats viewStats);
}
