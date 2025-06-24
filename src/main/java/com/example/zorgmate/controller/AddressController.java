package com.example.zorgmate.controller;

import com.example.zorgmate.service.interfaces.AddressService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@RestController
@RequestMapping("/api/address")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> lookupAddress(
            @RequestParam String postcode,
            @RequestParam String huisnummer
    ) {
        try {
            Map<String, String> result = addressService.lookupAddress(postcode, huisnummer);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}

