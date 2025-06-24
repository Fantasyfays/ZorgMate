package com.example.zorgmate.dal.entity.Client;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressData {
    private String street;
    private String city;
    private String postcode;
}
