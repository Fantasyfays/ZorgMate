package com.example.zorgmate.service.interfaces;

import java.util.Map;

public interface AddressService {
    Map<String, String> lookupAddress(String postcode, String huisnummer);
}
