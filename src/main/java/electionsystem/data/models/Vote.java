package electionsystem.data.models;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@Document(collection = "votes")
public class Vote {
    @Id
    private String id;
    private String userId;
    private String candidateId;
    private String electionId;
}