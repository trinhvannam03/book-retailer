package com.project.bookseller.entity.user.address;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class State {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long stateId;
    private String stateName;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "state")
    private List<City> cities = new ArrayList<>();
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "country_id")
    private Country country;

}
