package electionsystem.controllers;

import electionsystem.dtos.requests.VoteRequest;
import electionsystem.dtos.responses.ApiResponse;
import electionsystem.services.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/votes")
public class VoteController {

    private final VoteService voteService;

    @Autowired
    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> castVote(@RequestBody VoteRequest request) {
        voteService.castVote(request);
        return ResponseEntity.ok(new ApiResponse(true, "Vote cast successfully", null));
    }

    @GetMapping("/results/{electionId}")
    public ResponseEntity<ApiResponse> getResults(@PathVariable String electionId) {
        return ResponseEntity.ok(new ApiResponse(true, "Results fetched", voteService.getResults(electionId)));
    }
}