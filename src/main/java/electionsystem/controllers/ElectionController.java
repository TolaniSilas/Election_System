package electionsystem.controllers;

import electionsystem.data.models.User;
import electionsystem.dtos.requests.ElectionRequest;
import electionsystem.dtos.responses.ApiResponse;
import electionsystem.security.CurrentUser;
import electionsystem.services.ElectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/elections")
public class ElectionController {

    private final ElectionService electionService;

    public ElectionController(ElectionService electionService) {
        this.electionService = electionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> create(@CurrentUser User currentUser, @Valid @RequestBody ElectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Election created", electionService.createElection(currentUser, request)));
    }

    @PutMapping("/{electionId}")
    public ResponseEntity<ApiResponse> update(@CurrentUser User currentUser,
                                              @PathVariable String electionId,
                                              @Valid @RequestBody ElectionRequest request) {
        return ResponseEntity.ok(new ApiResponse(true, "Election updated",
                electionService.updateElection(currentUser, electionId, request)));
    }

    @DeleteMapping("/{electionId}")
    public ResponseEntity<ApiResponse> delete(@CurrentUser User currentUser, @PathVariable String electionId) {
        electionService.deleteElection(currentUser, electionId);
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
