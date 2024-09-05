package com.amr.book.handler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)//to include only the nonempty attribute
public class ExceptionResponse {
private Integer businessErrorCode;
private String  businessErrorDescription;
private String error;
private Set<String> validationErrors;
private Map<String, String> errors;

}
