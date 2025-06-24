package com.example.zorgmate.service.impl;

import com.example.zorgmate.external.PostcodeApiClient;
import com.example.zorgmate.service.interfaces.AddressService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AddressServiceImpl implements AddressService {

    private final PostcodeApiClient client;

    public AddressServiceImpl(PostcodeApiClient client) {
        this.client = client;
    }

    @Override
    public Map<String, String> lookupAddress(String postcode, String huisnummer) {
        Map<String, Object> data = client.lookup(postcode, huisnummer);
        Map<String, String> result = new HashMap<>();
        result.put("street", (String) data.get("street"));
        result.put("city", (String) data.get("city"));
        return result;
    }
}
