package electionsystem.services;

import electionsystem.data.models.Candidate;
import electionsystem.data.models.Election;
import electionsystem.data.models.ElectionStatus;
import electionsystem.data.models.Role;
import electionsystem.data.models.User;
import electionsystem.data.repositories.CandidateRepository;
import electionsystem.data.repositories.ElectionRepository;
import electionsystem.data.repositories.VoteRepository;
import electionsystem.dtos.requests.CandidateRequest;
import electionsystem.exceptions.AuthorizationException;
import electionsystem.exceptions.ConflictException;
import electionsystem.exceptions.InvalidStateException;
import electionsystem.exceptions.ResourceNotFoundException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class CandidateServiceImpl implements CandidateService {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final CandidateRepository candidateRepository;
    private final ElectionRepository electionRepository;
    private final VoteRepository voteRepository;

    public CandidateServiceImpl(CandidateRepository candidateRepository,
                                ElectionRepository electionRepository,
                                VoteRepository voteRepository) {
        this.candidateRepository = candidateRepository;
        this.electionRepository = electionRepository;
        this.voteRepository = voteRepository;
    }

    @Override
    public Candidate addCandidate(User currentUser, CandidateRequest request, MultipartFile image) {
        assertAdmin(currentUser);
        Election election = electionRepository.findById(request.getElectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Election not found"));
        if (election.getStatus() != ElectionStatus.UPCOMING) {
            throw new InvalidStateException("Candidates can only be managed before an election starts");
        }
        if (image == null || image.isEmpty()) {
            throw new InvalidStateException("Candidate image is required");
        }

        Candidate candidate = new Candidate();
        candidate.setName(request.getName().trim());
        candidate.setNormalizedName(normalizeName(request.getName()));
        candidate.setElectionId(request.getElectionId());
        candidate.setParty(request.getParty().trim().toUpperCase(Locale.ROOT));
        candidate.setBiography(request.getBiography().trim());
        candidate.setImageUrl(storeCandidateImage(image, request.getName()));
        try {
            return candidateRepository.save(candidate);
        } catch (DuplicateKeyException e) {
            throw new ConflictException("Candidate already exists in this election");
        }
    }

    @Override
    public Candidate updateCandidate(User currentUser, String candidateId, CandidateRequest request, MultipartFile image) {
        assertAdmin(currentUser);
        Candidate existing = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));
        Election election = electionRepository.findById(existing.getElectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Election not found"));
        if (election.getStatus() != ElectionStatus.UPCOMING) {
            throw new InvalidStateException("Candidates can only be edited before an election starts");
        }
        String normalizedName = normalizeName(request.getName());
        if (!existing.getNormalizedName().equals(normalizedName) &&
                candidateRepository.existsByNormalizedNameAndElectionId(normalizedName, existing.getElectionId())) {
            throw new ConflictException("Candidate already exists in this election");
        }

        existing.setName(request.getName().trim());
        existing.setNormalizedName(normalizedName);
        existing.setParty(request.getParty().trim().toUpperCase(Locale.ROOT));
        existing.setBiography(request.getBiography().trim());
        if (image != null && !image.isEmpty()) {
            existing.setImageUrl(storeCandidateImage(image, request.getName()));
        }
        return candidateRepository.save(existing);
    }

    @Override
    public void removeCandidate(User currentUser, String candidateId) {
        assertAdmin(currentUser);
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));
        Election election = electionRepository.findById(candidate.getElectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Election not found"));
        if (election.getStatus() != ElectionStatus.UPCOMING) {
            throw new InvalidStateException("Candidates cannot be removed after an election starts");
        }
        if (voteRepository.existsByCandidateId(candidateId)) {
            throw new InvalidStateException("Candidates with recorded votes cannot be removed");
        }
        candidateRepository.delete(candidate);
    }

    @Override
    public List<Candidate> getCandidatesByElection(String electionId) {
        electionRepository.findById(electionId)
                .orElseThrow(() -> new ResourceNotFoundException("Election not found"));
        return candidateRepository.findByElectionId(electionId);
    }

    private void assertAdmin(User currentUser) {
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.SUPER_ADMIN) {
            throw new AuthorizationException("Only admins can perform this action");
        }
    }

    private String normalizeName(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }

    private String storeCandidateImage(MultipartFile image, String candidateName) {
        String extension = resolveExtension(image.getOriginalFilename());
        String fileName = buildNameSlug(candidateName) + "." + extension;

        try {
            Path uploadDir = Path.of(System.getProperty("user.dir"), "uploads", "candidates");
            Files.createDirectories(uploadDir);
            Path filePath = uploadDir.resolve(fileName);
            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/candidates/" + fileName;
        } catch (IOException exception) {
            throw new InvalidStateException("Unable to store candidate image");
        }
    }

    private String resolveExtension(String originalName) {
        if (originalName == null || !originalName.contains(".")) {
            throw new InvalidStateException("Image must have a valid extension");
        }
        String extension = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidStateException("Only jpg, jpeg, png, or webp images are allowed");
        }
        return extension;
    }

    private String buildNameSlug(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        String firstName = sanitizeSlugToken(parts[0]);
        String lastName = sanitizeSlugToken(parts[parts.length - 1]);
        if (firstName.isBlank() || lastName.isBlank()) {
            throw new InvalidStateException("Candidate name is invalid for image generation");
        }
        return firstName + "-" + lastName;
    }

    private String sanitizeSlugToken(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
