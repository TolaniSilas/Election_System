package electionsystem.controllers;

import electionsystem.data.models.User;
import electionsystem.dtos.requests.CandidateRequest;
import electionsystem.dtos.responses.ApiResponse;
import electionsystem.security.CurrentUser;
import electionsystem.services.CandidateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> add(@CurrentUser User currentUser, @Valid @RequestBody CandidateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Candidate added", candidateService.addCandidate(currentUser, request)));
    }

    @DeleteMapping("/{candidateId}")
    public ResponseEntity<ApiResponse> remove(@CurrentUser User currentUser, @PathVariable String candidateId) {
        candidateService.removeCandidate(currentUser, candidateId);
        return ResponseEntity.ok(new ApiResponse(true, "Candidate removed", null));
    }

    @GetMapping("/election/{electionId}")
    public ResponseEntity<ApiResponse> getByElection(@PathVariable String electionId) {
        return ResponseEntity.ok(new ApiResponse(true, "Candidates fetched", candidateService.getCandidatesByElection(electionId)));
    }
}
