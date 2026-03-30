package electionsystem.controllers;

import electionsystem.dtos.requests.ElectionRequest;
import electionsystem.dtos.responses.ApiResponse;
import electionsystem.services.ElectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/elections")
public class ElectionController {

    private final ElectionService electionService;

    @Autowired
    public ElectionController(ElectionService electionService) {
        this.electionService = electionService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<ApiResponse> create(@PathVariable String userId, @RequestBody ElectionRequest request) {
        return ResponseEntity.ok(new ApiResponse(true, "Election created", electionService.createElection(userId, request)));
    }

    @PutMapping("/{userId}/{electionId}")
    public ResponseEntity<ApiResponse> update(@PathVariable String userId, @PathVariable String electionId, @RequestBody ElectionRequest request) {
        return ResponseEntity.ok(new ApiResponse(true, "Election updated", electionService.updateElection(userId, electionId, request)));
    }

    @DeleteMapping("/{userId}/{electionId}")
    public ResponseEntity<ApiResponse> delete(@PathVariable String userId, @PathVariable String electionId) {
        electionService.deleteElection(userId, electionId);
        return ResponseEntity.ok(new ApiResponse(true, "Election deleted", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAll() {
        return ResponseEntity.ok(new ApiResponse(true, "Elections fetched", electionService.getAllElections()));
    }

    @GetMapping("/{electionId}")
    public ResponseEntity<ApiResponse> getById(@PathVariable String electionId) {
        return ResponseEntity.ok(new ApiResponse(true, "Election fetched", electionService.getElectionById(electionId)));
    }
}