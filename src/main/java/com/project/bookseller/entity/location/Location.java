package com.project.bookseller.entity.location;

import com.project.bookseller.entity.user.address.City;
import com.project.bookseller.entity.user.address.Coordinates;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data

public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long locationId;
    private String locationName;
    private String detailedAddress;
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "city_id")
    private City city;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('STORE','WAREHOUSE','DISTRIBUTION_CENTER', 'ONLINE_STORE')")
    private LocationType locationType;
    @Embedded
    private Coordinates coordinates;
}
