package ru.practicum.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.practicum.category.model.Category;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;

import javax.persistence.criteria.CriteriaBuilder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    List<Event> findByCategory(Category category);

    Page<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    Optional<Event> findByIdAndStateIs(Long eventId, EventState published);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    static Specification<Event> hasUsers(List<Long> users) {
        return (root, query, criteriaBuilder) -> {
            if (users == null || users.size() == 0) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            } else {
                CriteriaBuilder.In<Long> userIds = criteriaBuilder.in(root.get("initiator"));
                for (Long userId : users) {
                    userIds.value(userId);
                }
                return userIds;
            }
        };
    }

    static Specification<Event> hasText(String text) {
        return (root, query, criteriaBuilder) -> {
            if (text == null) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            } else {
                return criteriaBuilder.or(criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("annotation")), text.toLowerCase()),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("description")), text.toLowerCase()));
            }
        };
    }

    static Specification<Event> hasCategories(List<Long> categories) {
        return (root, query, criteriaBuilder) -> {
            if (categories == null || categories.size() == 0) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            } else {
                CriteriaBuilder.In<Long> categoryIds = criteriaBuilder.in(root.get("category"));
                for (Long catId : categories) {
                    categoryIds.value(catId);
                }
                return categoryIds;
            }
        };
    }

    static Specification<Event> hasStates(List<EventState> states) {
        return (root, query, criteriaBuilder) -> {
            if (states == null || states.size() == 0) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            } else {
                CriteriaBuilder.In<EventState> cbStates = criteriaBuilder.in(root.get("state"));
                for (EventState state : states) {
                    cbStates.value(state);
                }
                return cbStates;
            }
        };
    }

    static Specification<Event> hasRangeStart(LocalDateTime rangeStart) {
        return (root, query, criteriaBuilder) -> {
            if (rangeStart == null) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            } else {
                return criteriaBuilder.greaterThan(root.get("eventDate"), rangeStart);
            }
        };
    }

    static Specification<Event> hasAvailable(Boolean onlyAvailable) {
        return (root, query, criteriaBuilder) -> {
            if (onlyAvailable != null && onlyAvailable) {
                return criteriaBuilder.or(criteriaBuilder.le(root.get("confirmedRequests"), root.get("participantLimit")),
                        criteriaBuilder.le(root.get("participantLimit"), 0));
            } else {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            }
        };
    }

    static Specification<Event> hasPaid(Boolean paid) {
        return (root, query, criteriaBuilder) -> {
            if (paid == null) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            } else {
                return criteriaBuilder.equal(root.get("paid"), paid);
            }
        };
    }

    static Specification<Event> hasRangeEnd(LocalDateTime rangeEnd) {
        return (root, query, criteriaBuilder) -> {
            if (rangeEnd == null) {
                return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
            } else {
                return criteriaBuilder.lessThan(root.get("eventDate"), rangeEnd);
            }
        };
    }

}
