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
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse> add(@CurrentUser User currentUser,
                                           @Valid @ModelAttribute CandidateRequest request,
                                           @RequestParam("image") MultipartFile image) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Candidate added", candidateService.addCandidate(currentUser, request, image)));
    }

    @PutMapping(value = "/{candidateId}", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse> update(@CurrentUser User currentUser,
                                              @PathVariable String candidateId,
                                              @Valid @ModelAttribute CandidateRequest request,
                                              @RequestParam(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(new ApiResponse(true, "Candidate updated",
                candidateService.updateCandidate(currentUser, candidateId, request, image)));
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
