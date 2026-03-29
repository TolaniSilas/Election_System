package electionsystem.data.models;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@Document(collection = "candidates")
public class Candidate {
    @Id
    private String id;
    private String name;
    private String electionId;
}