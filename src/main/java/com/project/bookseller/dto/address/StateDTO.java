package com.project.bookseller.dto.address;

import com.project.bookseller.entity.user.address.State;
import lombok.Data;

@Data
public class StateDTO {
    private long id;
    private String name;

    public static StateDTO convertFromEntity(State state) {
        StateDTO stateDTO = new StateDTO();
        stateDTO.setId(state.getStateId());
        stateDTO.setName(state.getStateName());
        return stateDTO;
    }
}
