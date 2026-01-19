package com.capstone.personalityTest.dto.RequestDTO.Exhibition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleUpdateRequest {
    private String scheduleJson;
    private List<BoothAllocation> boothAllocations;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoothAllocation {
        private Long boothId;
        private String zone;
        private Integer boothNumber;
    }
}
