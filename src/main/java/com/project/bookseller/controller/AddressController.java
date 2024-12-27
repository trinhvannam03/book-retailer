package com.project.bookseller.controller;

import com.project.bookseller.dto.address.CityDTO;
import com.project.bookseller.dto.address.CountryDTO;
import com.project.bookseller.dto.address.StateDTO;
import com.project.bookseller.entity.user.address.City;
import com.project.bookseller.entity.user.address.Country;
import com.project.bookseller.entity.user.address.State;
import com.project.bookseller.repository.address.CityRepository;
import com.project.bookseller.repository.address.CountryRepository;
import com.project.bookseller.repository.address.StateRepository;
import com.project.bookseller.repository.UserAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {
    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;
    private final CityRepository cityRepository;

    //get cities, states, countries to display
    @GetMapping("/countries")
    public ResponseEntity<List<CountryDTO>> countries() {
        List<Country> countriesDTO = countryRepository.findAll();
        List<CountryDTO> countryDTOList = new ArrayList<>();
        for (Country country : countriesDTO) {
            CountryDTO countryDTO = CountryDTO.convertFromCountry(country);
            countryDTOList.add(countryDTO);
        }
        return ResponseEntity.ok(countryDTOList);
    }

    @GetMapping("/states/{country_id}")
    public ResponseEntity<List<StateDTO>> states(@PathVariable long country_id) {
        List<State> states = stateRepository.findStatesByCountry_CountryId(country_id);
        List<StateDTO> stateDTOs = new ArrayList<>();
        for (State state : states) {
            StateDTO stateDTO = StateDTO.convertFromEntity(state);
            stateDTOs.add(stateDTO);
        }
        return new ResponseEntity<>(stateDTOs, HttpStatusCode.valueOf(200));
    }

    @GetMapping("/cities/{state_id}")
    public ResponseEntity<List<CityDTO>> cities(@PathVariable long state_id) {
        List<City> cities = cityRepository.findCitiesByState_StateId(state_id);
        List<CityDTO> cityDTOs = new ArrayList<>();
        for (City city : cities) {
            CityDTO cityDTO = CityDTO.convertFromCity(city);
            cityDTOs.add(cityDTO);
        }
        return new ResponseEntity<>(cityDTOs, HttpStatusCode.valueOf(200));
    }

}
