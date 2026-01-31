package com.capstone.personalityTest.dto.ResponseDTO.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationStatsResponse {
    private Long exhibitionId;
    private String exhibitionTitle;
    private UniversityStats universities;
    private SchoolStats schools;
    private StudentStats students;
    private ActivityProviderStats activityProviders;
    private Integer totalExpectedVisitors;
    private Integer actualAttendees;
    private Double attendanceRate;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UniversityStats {
        private Long invited;
        private Long registered;
        private Long confirmed;
        private Long finalized;
        private Long attended;
        private Integer totalBooths;
        private Double attendanceRate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SchoolStats {
        private Long invited;
        private Long registered;
        private Long finalized;
        private Long attended;
        private Double attendanceRate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentStats {
        private Long registered;
        private Long attended;
        private Long noShow;
        private Double attendanceRate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityProviderStats {
        private Long invited;
        private Long proposalSubmitted;
        private Long finalized;
        private Long attended;
        private Integer totalBooths;
        private Double attendanceRate;
    }
}
