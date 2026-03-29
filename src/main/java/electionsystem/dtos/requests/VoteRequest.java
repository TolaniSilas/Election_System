package electionsystem.dtos.requests;
import lombok.Data;


@Data
public class VoteRequest {
    private String userId;
    private String candidateId;
    private String electionId;
}