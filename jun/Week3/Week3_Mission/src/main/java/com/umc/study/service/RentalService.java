package com.umc.study.service;

import com.umc.study.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service // 비즈니스 로직을 수행하는 메인 셰프 계층
@RequiredArgsConstructor
public class RentalService {
    private final RentalRepository rentalRepository;

    public void createRental(Map<String, Object> body){
        rentalRepository.createRental(body);
    }
}
