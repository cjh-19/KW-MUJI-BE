package com.muji_backend.kw_muji.survey.controller;

import com.muji_backend.kw_muji.survey.dto.request.SurveyRequestDto;
import com.muji_backend.kw_muji.survey.dto.request.SurveySubmitRequestDto;
import com.muji_backend.kw_muji.survey.dto.response.SurveyDetailResponseDto;
import com.muji_backend.kw_muji.survey.dto.response.SurveyResponseDto;
import com.muji_backend.kw_muji.survey.service.SurveyCreateService;
import com.muji_backend.kw_muji.survey.service.SurveyService;
import com.muji_backend.kw_muji.survey.service.SurveySubmitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/survey")
public class SurveyController {

    private final SurveyService surveyService;
    private final SurveyCreateService surveyCreateService;
    private final SurveySubmitService surveySubmitService;

    @GetMapping
    public ResponseEntity<?> getSurveyList(@RequestParam(value = "search", required = false) String search,
                                           @RequestParam(value = "page", defaultValue = "0") int page) {
        try {
            SurveyResponseDto response = surveyService.getSurveys(search, page);
            return ResponseEntity.ok().body(Map.of("code", 200, "data", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "message", e.getMessage()));
        }
    }

    @PostMapping("/create/{userId}")
    public ResponseEntity<?> createSurvey(@PathVariable Long userId, @RequestBody SurveyRequestDto requestDto) {
        try {
            Long surveyId = surveyCreateService.createSurvey(userId, requestDto);
            return ResponseEntity.ok().body(Map.of("code", 200, "surveyId", surveyId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "message", e.getMessage()));
        }
    }

    @GetMapping("/{surveyId}")
    public ResponseEntity<?> getSurvey(@PathVariable Long surveyId) {
        try {
            SurveyDetailResponseDto responseDto = surveyService.getSurveyDetail(surveyId);
            return ResponseEntity.ok().body(Map.of("code", 200, "data", responseDto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "message", e.getMessage()));
        }
    }

    @PostMapping("/submit/{surveyId}")
    public ResponseEntity<?> submitSurvey(
            @PathVariable Long surveyId,
            @RequestBody SurveySubmitRequestDto requestDto) {
        try {
            Long responseId = surveySubmitService.submitSurvey(surveyId, requestDto);
            return ResponseEntity.ok().body(Map.of("code", 200, "responseId", responseId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "message", e.getMessage()));
        }
    }
}