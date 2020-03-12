package com.webank.weid.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class IssuerType extends BaseDto {
    
    private String type;
    private List<String> weidList = new ArrayList<String>();
   
}
