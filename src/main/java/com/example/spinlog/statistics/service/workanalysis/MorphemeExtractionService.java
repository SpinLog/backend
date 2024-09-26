package com.example.spinlog.statistics.service.workanalysis;

import com.example.spinlog.statistics.service.dto.MorphemeApiRequestDto;
import com.example.spinlog.statistics.service.dto.MorphemeResponse;
import com.example.spinlog.statistics.service.dto.Token;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MorphemeExtractionService {
    private final MorphemeApiClient morphemeApiClient;
    @Value("${morphemeApiKey}") String apiKey;
    private static final int WORD_LENGTH_LIMIT = 9999;
    private static final int API_ERROR_RESPONSE = -1;

    public List<Token> extractTokensFromMemos(List<String> memos) {
        List<Token> tokens = new ArrayList<>();
        StringBuilder input = new StringBuilder();

        for (String memo : memos) {
            if (input.length() + memo.length() > WORD_LENGTH_LIMIT) {
                tokens.addAll(extractTokens(input));
                input.setLength(0);
            }
            input.append(memo).append(" ");
        }

        // extract remaining memos
        if (!input.isEmpty()) {
            tokens.addAll(extractTokens(input));
        }

        return tokens;
    }

    private List<Token> extractTokens(StringBuilder input) {
        MorphemeResponse response = morphemeApiClient.getMorphemes(
                apiKey,
                MorphemeApiRequestDto.builder()
                        .request_id("request")
                        .argument(
                                new MorphemeApiRequestDto.Argument(
                                        input.toString()))
                        .build());

        if(isErrorResponse(response)) {
            throw new RuntimeException("API 요청에 실패했습니다.");
        }

        return Token.createTokenList(response);
    }

    private static boolean isErrorResponse(MorphemeResponse response) {
        return response.getResult() == API_ERROR_RESPONSE;
    }

}
