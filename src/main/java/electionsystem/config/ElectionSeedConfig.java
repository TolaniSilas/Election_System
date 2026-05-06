package electionsystem.config;

import electionsystem.data.models.Candidate;
import electionsystem.data.models.Election;
import electionsystem.data.models.ElectionCategory;
import electionsystem.data.models.ElectionScope;
import electionsystem.data.models.ElectionStatus;
import electionsystem.data.models.Role;
import electionsystem.data.models.User;
import electionsystem.data.models.VoterApprovalStatus;
import electionsystem.data.repositories.CandidateRepository;
import electionsystem.data.repositories.ElectionRepository;
import electionsystem.data.repositories.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Configuration
public class ElectionSeedConfig {

    @Bean
    public ApplicationRunner electionSeedRunner(ElectionRepository electionRepository,
                                                CandidateRepository candidateRepository,
                                                UserRepository userRepository) {
        return args -> {
            User seedOwner = userRepository.findAll().stream()
                    .filter(user -> user.getRole() == Role.SUPER_ADMIN)
                    .findFirst()
                    .orElseGet(() -> userRepository.findAll().stream().findFirst().orElse(null));
            String ownerId = seedOwner != null ? seedOwner.getId() : "system";

            seedElection(electionRepository, candidateRepository, ownerId,
                    "President Election",
                    "National presidential election currently ongoing.",
                    ElectionCategory.PRESIDENT,
                    ElectionScope.NATIONAL,
                    null,
                    List.of("Bola Ahmed Tinubu", "Peter Obi", "Atiku Abubakar"));

            seedElection(electionRepository, candidateRepository, ownerId,
                    "Governor Election",
                    "Lagos gubernatorial election currently ongoing.",
                    ElectionCategory.GOVERNOR,
                    ElectionScope.STATE,
                    "LAGOS",
                    List.of("Sanwo-Olu Jide", "Hamsat Fola", "Ambode Rasaq"));

            seedElection(electionRepository, candidateRepository, ownerId,
                    "Chairman Election",
                    "Lagos chairman election currently ongoing.",
                    ElectionCategory.CHAIRMAN,
                    ElectionScope.STATE,
                    "LAGOS",
                    List.of("Adebayo Sule", "Kehinde Balogun", "Rashidat Lawal"));
        };
    }

    private void seedElection(ElectionRepository electionRepository,
                              CandidateRepository candidateRepository,
                              String ownerId,
                              String title,
                              String description,
                              ElectionCategory category,
                              ElectionScope scope,
                              String state,
                              List<String> candidates) {
        Election election = electionRepository.findByTitle(title).orElseGet(() -> {
            Election created = new Election();
            created.setTitle(title);
            created.setDescription(description);
            created.setCategory(category);
            created.setScope(scope);
            created.setState(state);
            created.setStartTime(LocalDateTime.now().minusDays(2));
            created.setEndTime(LocalDateTime.now().plusDays(14));
            created.setStatus(ElectionStatus.ONGOING);
            created.setCreatedByUserId(ownerId);
            created.setCreatedAt(LocalDateTime.now());
            created.setUpdatedAt(LocalDateTime.now());
            return electionRepository.save(created);
        });

        if (candidateRepository.countByElectionId(election.getId()) == 0) {
            for (String candidateName : candidates) {
                Candidate candidate = new Candidate();
                candidate.setName(candidateName);
                candidate.setNormalizedName(candidateName.trim().toLowerCase(Locale.ROOT));
                candidate.setElectionId(election.getId());
                candidate.setParty("INDEPENDENT");
                candidate.setBiography(candidateName + " is a seeded candidate profile. Admins can update election rosters with richer biographies.");
                candidate.setImageUrl("https://picture.jpg" + candidateName.replace(" ", "+"));
                candidateRepository.save(candidate);
            }
        }
    }
}
