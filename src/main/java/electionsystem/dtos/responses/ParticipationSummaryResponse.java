package electionsystem.dtos.responses;

import java.util.List;
import java.util.Map;

public class ParticipationSummaryResponse {
    private Map<String, Long> totalsByCategory;
    private List<ElectionParticipationResponse> elections;

    public Map<String, Long> getTotalsByCategory() { return totalsByCategory; }
    public void setTotalsByCategory(Map<String, Long> totalsByCategory) { this.totalsByCategory = totalsByCategory; }
    public List<ElectionParticipationResponse> getElections() { return elections; }
    public void setElections(List<ElectionParticipationResponse> elections) { this.elections = elections; }
}
