package com.project.bookseller.entity.user.address;

import com.project.bookseller.entity.location.Location;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long cityId;
    private String cityName;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id")
    private State state = new State();
    @OneToMany(mappedBy = "city")
    private List<Location> stores = new ArrayList<>();
}
