package com.project.bookseller.repository.address;

import com.project.bookseller.entity.user.address.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StateRepository extends JpaRepository<State, Integer> {
    List<State> findStatesByCountry_CountryId(long countryId);

}
