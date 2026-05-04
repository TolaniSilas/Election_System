package electionsystem.controllers;

import electionsystem.data.models.User;
import electionsystem.dtos.requests.VoteRequest;
import electionsystem.dtos.responses.ApiResponse;
import electionsystem.security.CurrentUser;
import electionsystem.services.VoteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/votes")
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> castVote(@CurrentUser User currentUser, @Valid @RequestBody VoteRequest request) {
        voteService.castVote(currentUser, request);
        return ResponseEntity.ok(new ApiResponse(true, "Vote cast successfully", null));
    }

    @GetMapping("/results/{electionId}")
    public ResponseEntity<ApiResponse> getResults(@PathVariable String electionId) {
        return ResponseEntity.ok(new ApiResponse(true, "Results fetched", voteService.getResults(electionId)));
    }
}
