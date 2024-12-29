package com.project.bookseller.elasticSearchEntity;

import lombok.Data;

@Data
public class SearchCriteria {
    private boolean alphabetAscending = false;
    private boolean alphabetDescending = false;
    private boolean priceAscending = false;
    private boolean priceDescending = false;
    private double lowestPrice = 0;
    private Double highestPrice;
    private boolean publicationDateAscending = false;
    private boolean publicationDateDescending = false;
    private boolean relevance;
}
