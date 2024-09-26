package com.example.spinlog.statistics.service.wordanalysis;

import com.example.spinlog.statistics.service.dto.MorphemeApiRequestDto;
import com.example.spinlog.statistics.service.dto.MorphemeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "MorphemeApiClient", url = "http://aiopen.etri.re.kr:8000/WiseNLU")
public interface MorphemeApiClient {
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    MorphemeResponse getMorphemes(@RequestHeader("Authorization") String authorization, @RequestBody MorphemeApiRequestDto dto);
}
