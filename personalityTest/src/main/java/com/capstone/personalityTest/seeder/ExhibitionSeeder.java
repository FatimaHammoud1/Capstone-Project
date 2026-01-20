package com.capstone.personalityTest.seeder;

import com.capstone.personalityTest.model.Enum.Exhibition.ActivityType;
import com.capstone.personalityTest.model.Enum.Exhibition.OrganizationType;
import com.capstone.personalityTest.model.Enum.TargetGender;
import com.capstone.personalityTest.model.Exhibition.*;
import com.capstone.personalityTest.model.Role;
import com.capstone.personalityTest.model.UserInfo;
import com.capstone.personalityTest.repository.Exhibition.*;
import com.capstone.personalityTest.repository.RoleRepository;
import com.capstone.personalityTest.repository.UserInfoRepository;
import com.capstone.personalityTest.repository.financial_aid.DonorRepository;
import com.capstone.personalityTest.model.financial_aid.Donor;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ExhibitionSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserInfoRepository userInfoRepository;
    private final OrganizationRepository organizationRepository;
    private final UniversityRepository universityRepository;
    private final MunicipalityRepository municipalityRepository;
    private final ActivityProviderRepository activityProviderRepository;
    private final ActivityRepository activityRepository;
    private final SchoolRepository schoolRepository;
    private final VenueRepository venueRepository;
    private final DonorRepository donorRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ExhibitionSeeder(
            RoleRepository roleRepository,
            UserInfoRepository userInfoRepository,
            OrganizationRepository organizationRepository,
            UniversityRepository universityRepository,
            MunicipalityRepository municipalityRepository,
            ActivityProviderRepository activityProviderRepository,
            ActivityRepository activityRepository,
            SchoolRepository schoolRepository,
            VenueRepository venueRepository,
            DonorRepository donorRepository,
            PasswordEncoder passwordEncoder
    ) {

        this.roleRepository = roleRepository;
        this.userInfoRepository = userInfoRepository;
        this.organizationRepository = organizationRepository;
        this.universityRepository = universityRepository;
        this.municipalityRepository = municipalityRepository;
        this.activityProviderRepository = activityProviderRepository;
        this.activityRepository = activityRepository;
        this.schoolRepository = schoolRepository;
        this.venueRepository = venueRepository;
        this.donorRepository = donorRepository;
        this.passwordEncoder = passwordEncoder;
    }

 


    @Override
    public void run(String... args) {
        seedRoles();
        seedUsers();
        seedOrganizations();
        seedUniversities();
        seedMunicipalities();
        seedActivityProviders();
        seedActivities();
        seedSchools();
        seedVenues();
        seedDonors();
    }

    // ---------------- ROLES ----------------
    private void seedRoles() {
        if (roleRepository.count() > 0) return;

        roleRepository.saveAll(List.of(
                new Role(null, "DEVELOPER", "System Developer", "Full system access", new HashSet<>()),
                new Role(null, "ORG_OWNER", "Organization Owner", "Creates and manages exhibitions", new HashSet<>()),
                new Role(null, "MUNICIPALITY_ADMIN", "Municipality Admin", "Approves venue requests", new HashSet<>()),
                new Role(null, "UNIVERSITY_ADMIN", "University Admin", "Manages university participation", new HashSet<>()),
                new Role(null, "SCHOOL_ADMIN", "School Admin", "Manages school participation", new HashSet<>()),
                new Role(null, "ACTIVITY_PROVIDER", "Activity Provider", "Provides activities and demos", new HashSet<>()),
                new Role(null, "STUDENT", "Student", "Registers and attends exhibitions", new HashSet<>()),
                new Role(null, "DONOR", "Donor", "Provides financial aid", new HashSet<>())
        ));
    }

    // ---------------- USERS ----------------
    private void seedUsers() {
        if (userInfoRepository.count() > 0) return;

        userInfoRepository.saveAll(List.of(
                // 1. DEVELOPER
                new UserInfo(null, "System Dev", "dev@system.com", passwordEncoder.encode("password"), TargetGender.MALE,
                        Set.of(roleRepository.findByCode("DEVELOPER").orElseThrow())),

                // 2. ORG_OWNER
                new UserInfo(null, "Org Owner", "org@org.com", passwordEncoder.encode("password"), TargetGender.FEMALE,
                        Set.of(roleRepository.findByCode("ORG_OWNER").orElseThrow())),

                // 3. MUNICIPALITY_ADMIN
                new UserInfo(null, "Municipality Admin", "muni@city.com", passwordEncoder.encode("password"), TargetGender.MALE,
                        Set.of(roleRepository.findByCode("MUNICIPALITY_ADMIN").orElseThrow())),

                // 4. UNIVERSITY_ADMIN
                new UserInfo(null, "University Admin", "uni@university.com", passwordEncoder.encode("password"), TargetGender.FEMALE,
                        Set.of(roleRepository.findByCode("UNIVERSITY_ADMIN").orElseThrow())),

                // 5. SCHOOL_ADMIN
                new UserInfo(null, "School Admin", "school@school.com", passwordEncoder.encode("password"), TargetGender.MALE,
                        Set.of(roleRepository.findByCode("SCHOOL_ADMIN").orElseThrow())),

                // 6. ACTIVITY_PROVIDER
                new UserInfo(null, "Activity Provider", "activity@provider.com", passwordEncoder.encode("password"), TargetGender.FEMALE,
                        Set.of(roleRepository.findByCode("ACTIVITY_PROVIDER").orElseThrow())),

                // 7. STUDENT
                new UserInfo(null, "Student User", "student@user.com", passwordEncoder.encode("password"), TargetGender.MALE,
                        Set.of(roleRepository.findByCode("STUDENT").orElseThrow())),

                // 8. DONOR
                new UserInfo(null, "Donor User", "donor@user.com", passwordEncoder.encode("password"), TargetGender.FEMALE,
                        Set.of(roleRepository.findByCode("DONOR").orElseThrow()))
        ));

    }

    // ---------------- ORGANIZATION ----------------
    private void seedOrganizations() {
        if (organizationRepository.count() > 0) return;

        UserInfo owner = userInfoRepository.findByEmail("org@org.com").orElseThrow();

        organizationRepository.saveAll(List.of(
                new Organization(
                        null,
                        "Career Guidance Organization",
                        "Supports students in career exploration",
                        owner,
                        OrganizationType.CAREER_GUIDANCE,
                        true,
                        LocalDateTime.now()
                ),
                new Organization(
                        null,
                        "Tech Education Hub",
                        "Promotes technology education and innovation",
                        owner,
                        OrganizationType.CAREER_GUIDANCE,
                        true,
                        LocalDateTime.now()
                ),
                new Organization(
                        null,
                        "Future Skills Foundation",
                        "Prepares students for future careers",
                        owner,
                        OrganizationType.CAREER_GUIDANCE,
                        true,
                        LocalDateTime.now()
                )
        ));
    }

    // ---------------- UNIVERSITY ----------------
    private void seedUniversities() {
        if (universityRepository.count() > 0) return;

        UserInfo owner = userInfoRepository.findByEmail("uni@university.com").orElseThrow();

        universityRepository.saveAll(List.of(
                new University(
                        null,
                        "Tech University",
                        "contact@tu.edu",
                        "123456789",
                        true,
                        owner
                ),
                new University(
                        null,
                        "Creative Arts University",
                        "contact@arts.edu",
                        "111222333",
                        true,
                        owner
                ),
                new University(
                        null,
                        "Medical Sciences University",
                        "contact@medsci.edu",
                        "444555666",
                        true,
                        owner
                )
        ));
    }

    // ---------------- MUNICIPALITY ----------------
    private void seedMunicipalities() {
        if (municipalityRepository.count() > 0) return;

        UserInfo admin = userInfoRepository.findByEmail("muni@city.com").orElseThrow();

        municipalityRepository.saveAll(List.of(
                new Municipality(
                        null,
                        "City Municipality",
                        "Central District",
                        "contact@city.gov",
                        "987654321",
                        admin
                ),
                new Municipality(
                        null,
                        "North District Municipality",
                        "North District",
                        "north@city.gov",
                        "123123123",
                        admin
                ),
                new Municipality(
                        null,
                        "South Coast Municipality",
                        "South District",
                        "south@city.gov",
                        "456456456",
                        admin
                )
        ));
    }

    // ---------------- ACTIVITY PROVIDER ----------------
    private void seedActivityProviders() {
        if (activityProviderRepository.count() > 0) return;

        UserInfo owner = userInfoRepository.findByEmail("activity@provider.com").orElseThrow();

        activityProviderRepository.saveAll(List.of(
                new ActivityProvider(
                        null,
                        "Fun Science Lab",
                        "contact@funlab.com",
                        "11223344",
                        true,
                        owner
                ),
                new ActivityProvider(
                        null,
                        "Tech Innovators",
                        "contact@tech.com",
                        "99887766",
                        true,
                        owner
                ),
                new ActivityProvider(
                        null,
                        "Art & Design Studio",
                        "contact@artdesign.com",
                        "55443322",
                        true,
                        owner
                )
        ));
    }

    // ---------------- ACTIVITIES ----------------
    private void seedActivities() {
        if (activityRepository.count() > 0) return;

        // Fetch the activity providers
        ActivityProvider funLab = activityProviderRepository.findAll().stream()
                .filter(p -> "Fun Science Lab".equals(p.getName()))
                .findFirst()
                .orElseThrow();

        ActivityProvider techInnovators = activityProviderRepository.findAll().stream()
                .filter(p -> "Tech Innovators".equals(p.getName()))
                .findFirst()
                .orElseThrow();
                
        ActivityProvider artStudio = activityProviderRepository.findAll().stream()
                .filter(p -> "Art & Design Studio".equals(p.getName()))
                .findFirst()
                .orElseThrow();

        activityRepository.saveAll(List.of(

                // Fun Science Lab Activities
                new Activity(
                        null,
                        "AI Career Workshop",
                        "Introduction to artificial intelligence careers",
                        ActivityType.WORKSHOP,
                        60, 
                        30, 
                        true,
                        funLab
                ),
                new Activity(
                        null,
                        "Medical Lab Simulation",
                        "Hands-on medical lab experience",
                        ActivityType.DEMO, 
                        90,
                        20,
                        true,
                        funLab
                ),

                // Tech Innovators Activities
                new Activity(
                        null,
                        "VR Experience",
                        "Immersive Virtual Reality session",
                        ActivityType.DEMO,
                        30, 
                        10,
                        true,
                        techInnovators
                ),
                new Activity(
                        null,
                        "Coding Bootcamp Intro",
                        "Learn basics of Python",
                        ActivityType.WORKSHOP,
                        120, 
                        25,
                        true,
                        techInnovators
                ),

                // Art & Design Studio Activities
                new Activity(
                        null,
                        "Digital Art Basics",
                        "Introduction to digital painting",
                        ActivityType.WORKSHOP,
                        90,
                        15,
                        true,
                        artStudio
                ),
                new Activity(
                        null,
                        "3D Modeling Demo",
                        "Live 3D character modeling",
                        ActivityType.DEMO,
                        45,
                        30,
                        true,
                        artStudio
                )

        ));
    }

    // ---------------- SCHOOLS ----------------
    private void seedSchools() {
        if (schoolRepository.count() > 0) return;

        UserInfo owner = userInfoRepository.findByEmail("school@school.com").orElseThrow();

        schoolRepository.saveAll(List.of(

                new School(
                        null,
                        "Al-Najah High School",
                        "contact@najahschool.edu",
                        "70123456",
                        true,
                        owner
                ),

                new School(
                        null,
                        "Future Leaders School",
                        "info@futureleaders.edu",
                        "70987654",
                        true,
                        owner
                ),

                new School(
                        null,
                        "Modern Science School",
                        "admin@modscience.edu",
                        "71112233",
                        true,
                        owner
                )

        ));
    }
    // ---------------- VENUES ----------------
    private void seedVenues() {
        if (venueRepository.count() > 0) return;

        // Make sure the municipality exists
        Municipality municipality = municipalityRepository.findByName("City Municipality")
                .orElseThrow(() -> new RuntimeException("Municipality not found"));

        venueRepository.saveAll(List.of(
                new Venue(
                        null,
                        municipality.getId(),
                        "Grand Hall",
                        "123 Main Street",
                        500,
                        1000.0,
                        new java.math.BigDecimal("2000.00"),
                        true,
                        true
                ),
                new Venue(
                        null,
                        municipality.getId(),
                        "Exhibition Center A",
                        "45 Expo Road",
                        300,
                        750.0,
                        new java.math.BigDecimal("1500.00"),
                        true,
                        true
                ),
                new Venue(
                        null,
                        municipality.getId(),
                        "Convention Hall B",
                        "78 City Plaza",
                        200,
                        500.0,
                        new java.math.BigDecimal("1200.00"),
                        true,
                        true


                )
        ));
    }


    // ---------------- DONORS ----------------
    private void seedDonors() {
        if (donorRepository.count() > 0) return;

        UserInfo donorUser = userInfoRepository.findByEmail("donor@user.com").orElseThrow();
        Organization org = organizationRepository.findAll().stream()
                .filter(o -> "Career Guidance Organization".equals(o.getName()))
                .findFirst()
                .orElseThrow();

        donorRepository.saveAll(List.of(
                new Donor(null, org, donorUser, "Tech Future Fund", new BigDecimal("50000"), new BigDecimal("50000"), true),
                new Donor(null, org, donorUser, "Education For All", new BigDecimal("75000"), new BigDecimal("75000"), true),
                new Donor(null, org, donorUser, "Community Grant", new BigDecimal("25000"), new BigDecimal("25000"), true)
        ));
    }
}
