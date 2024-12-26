package com.project.bookseller.entity.location;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ItemAvailability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long itemAvailabilityId;


}
