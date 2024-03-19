package ru.practicum.event.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.practicum.category.model.Category;
import ru.practicum.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "events")
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"category", "location"})
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String annotation;

    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    private Long confirmedRequests;

    @Column(name = "created_on")
    @CreationTimestamp
    private LocalDateTime createdOn;

    private String description;

    private LocalDateTime eventDate;

    @ManyToOne(fetch = FetchType.EAGER)
    private User initiator;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "location_id")
    private Location location;

    private boolean paid;

    private Long participantLimit;

    private LocalDateTime publishedOn;

    private Boolean requestModeration;

    @Enumerated(EnumType.STRING)
    private EventState state;

    private String title;

    private Long views;
}









