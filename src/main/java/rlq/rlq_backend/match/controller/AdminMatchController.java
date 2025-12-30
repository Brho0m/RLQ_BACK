package rlq.rlq_backend.match.controller;

import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import rlq.rlq_backend.match.service.AdminMatchService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;
import rlq.rlq_backend.match.dtos.MatchDTO;

@RestController
@RequestMapping("/admin/matches")
@RequiredArgsConstructor
public class AdminMatchController {

    private final AdminMatchService adminMatchService;

    @PostMapping("create")
    public ResponseEntity<MatchDTO> createMatch() {
        return ResponseEntity.ok(adminMatchService.createMatch());
    }

    @PostMapping("close/{matchId}")
    public ResponseEntity<MatchDTO> closeMatch(@PathVariable Long matchId) {
        return ResponseEntity.ok(adminMatchService.closeMatch(matchId));
    }

    @PostMapping("start/{matchId}")
    public ResponseEntity<MatchDTO> startMatch(@PathVariable Long matchId) {
        return ResponseEntity.ok(adminMatchService.startMatch(matchId));
    }
}
