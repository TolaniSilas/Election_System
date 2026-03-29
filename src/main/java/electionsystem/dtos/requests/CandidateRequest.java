package electionsystem.dtos.requests;
import lombok.Data;


@Data
public class CandidateRequest {
    private String name;
    private String electionId;
}