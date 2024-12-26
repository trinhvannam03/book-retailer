package com.project.bookseller.repository.address;

import com.project.bookseller.entity.user.address.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CityRepository extends JpaRepository<City, Integer> {

    List<City> findCitiesByState_StateId(Long stateId);
}
