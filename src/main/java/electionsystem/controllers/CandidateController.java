package electionsystem.controllers;

import electionsystem.dtos.requests.CandidateRequest;
import electionsystem.dtos.responses.ApiResponse;
import electionsystem.services.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

    private final CandidateService candidateService;

    @Autowired
    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<ApiResponse> add(@PathVariable String userId, @RequestBody CandidateRequest request) {
        return ResponseEntity.ok(new ApiResponse(true, "Candidate added", candidateService.addCandidate(userId, request)));
    }

    @DeleteMapping("/{userId}/{candidateId}")
    public ResponseEntity<ApiResponse> remove(@PathVariable String userId, @PathVariable String candidateId) {
        candidateService.removeCandidate(userId, candidateId);
        return ResponseEntity.ok(new ApiResponse(true, "Candidate removed", null));
    }

    @GetMapping("/election/{electionId}")
    public ResponseEntity<ApiResponse> getByElection(@PathVariable String electionId) {
        return ResponseEntity.ok(new ApiResponse(true, "Candidates fetched", candidateService.getCandidatesByElection(electionId)));
    }
}