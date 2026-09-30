package com.oj.platform.controller;

import com.oj.platform.dto.LeaderboardEntryDto;
import com.oj.platform.service.LeaderboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping
    public ResponseEntity<List<LeaderboardEntryDto>> getLeaderboard(
            @RequestParam(required = false, defaultValue = "GLOBAL") String scope,
            @RequestParam(required = false) String language) {
        List<LeaderboardEntryDto> leaderboard = leaderboardService.getLeaderboard(scope, language);
        return ResponseEntity.ok(leaderboard);
    }
}
