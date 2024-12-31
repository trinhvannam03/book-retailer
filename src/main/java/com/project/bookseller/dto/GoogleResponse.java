package com.project.bookseller.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GoogleResponse {
    //used by Google to authorize access to protected resources
    private String access_token;
    private String token_type;
    private String scope;
    //open_id token, used by the application for authentication, not authorization
    private String id_token;
    private String id;
    private String email;
    private boolean verified_email;
    private String name;
    private String given_name;
    private String family_name;
    private String picture;
}
