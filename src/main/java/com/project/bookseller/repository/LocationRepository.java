package com.project.bookseller.repository;

import com.project.bookseller.entity.location.Location;
import com.project.bookseller.entity.location.LocationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findLocationsByLocationType(LocationType type);
}
