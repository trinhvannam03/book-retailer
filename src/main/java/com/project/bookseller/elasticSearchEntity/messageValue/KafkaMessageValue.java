package com.project.bookseller.elasticSearchEntity.messageValue;

import lombok.Data;

@Data
public class KafkaMessageValue {
    private Schema schema;
    private Payload payload;
}
