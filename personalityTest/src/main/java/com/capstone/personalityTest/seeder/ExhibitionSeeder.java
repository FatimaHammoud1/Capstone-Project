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
            VenueRepository venueRepository,PasswordEncoder passwordEncoder
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
        this.passwordEncoder =passwordEncoder;
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
                new Role(null, "STUDENT", "Student", "Registers and attends exhibitions", new HashSet<>())
        ));
    }

    // ---------------- USERS ----------------
    private void seedUsers() {
        if (userInfoRepository.count() > 0) return;

        userInfoRepository.saveAll(List.of(
                new UserInfo(null, "System Dev", "dev@system.com", passwordEncoder.encode("password"), TargetGender.MALE,
                        Set.of(roleRepository.findByCode("DEVELOPER").orElseThrow())),

                new UserInfo(null, "Org Owner", "org@org.com", passwordEncoder.encode("password"), TargetGender.FEMALE,
                        Set.of(roleRepository.findByCode("ORG_OWNER").orElseThrow())),

                new UserInfo(null, "Municipality Admin", "muni@city.com", passwordEncoder.encode("password"), TargetGender.MALE,
                        Set.of(roleRepository.findByCode("MUNICIPALITY_ADMIN").orElseThrow())),

                new UserInfo(null, "University Admin", "uni@university.com", passwordEncoder.encode("password"), TargetGender.FEMALE,
                        Set.of(roleRepository.findByCode("UNIVERSITY_ADMIN").orElseThrow())),

                new UserInfo(null, "School Admin", "school@school.com", passwordEncoder.encode("password"), TargetGender.MALE,
                        Set.of(roleRepository.findByCode("SCHOOL_ADMIN").orElseThrow())),

                new UserInfo(null, "Creative Uni Admin", "artuni@university.com", passwordEncoder.encode("password"), TargetGender.FEMALE,
                        Set.of(roleRepository.findByCode("UNIVERSITY_ADMIN").orElseThrow())),

                new UserInfo(null, "Activity Provider", "activity@provider.com", passwordEncoder.encode("password"), TargetGender.MALE,
                        Set.of(roleRepository.findByCode("ACTIVITY_PROVIDER").orElseThrow())),

                new UserInfo(null, "Tech Provider", "tech@provider.com", passwordEncoder.encode("password"), TargetGender.FEMALE,
                        Set.of(roleRepository.findByCode("ACTIVITY_PROVIDER").orElseThrow())),

                new UserInfo(null, "Student User", "student@user.com", passwordEncoder.encode("password"), TargetGender.FEMALE,
                        Set.of(roleRepository.findByCode("STUDENT").orElseThrow()))
        ));

    }

    // ---------------- ORGANIZATION ----------------
    private void seedOrganizations() {
        if (organizationRepository.count() > 0) return;

        UserInfo owner = userInfoRepository.findByEmail("org@org.com").orElseThrow();

        Organization org = new Organization();
        org.setName("Career Guidance Organization");
        org.setDescription("Supports students in career exploration");
        org.setOwner(owner);
        org.setType(OrganizationType.CAREER_GUIDANCE);
        org.setActive(true);
        org.setCreatedAt(LocalDateTime.now());

        organizationRepository.save(org);
    }

    // ---------------- UNIVERSITY ----------------
    private void seedUniversities() {
        if (universityRepository.count() > 0) return;

        University uni = new University();
        uni.setName("Tech University");
        // uni.setShortName("TU"); // Field removed in new model
        uni.setContactEmail("contact@tu.edu");
        uni.setContactPhone("123456789");
        // uni.setAddress("123 Main Street"); // Field removed in new model
        // uni.setWebsite("www.tu.edu"); // Field removed in new model
        UserInfo owner = userInfoRepository.findByEmail("uni@university.com").orElseThrow();
        uni.setOwner(owner);
        uni.setActive(true);

        universityRepository.save(uni);

        University artUni = new University();
        artUni.setName("Creative Arts University");
        artUni.setContactEmail("contact@arts.edu");
        artUni.setContactPhone("111222333");
        UserInfo artOwner = userInfoRepository.findByEmail("artuni@university.com").orElseThrow();
        artUni.setOwner(artOwner);
        artUni.setActive(true);

        universityRepository.save(artUni);
    }

    // ---------------- MUNICIPALITY ----------------
    private void seedMunicipalities() {
        if (municipalityRepository.count() > 0) return;

        UserInfo admin = userInfoRepository.findByEmail("muni@city.com").orElseThrow();

        Municipality municipality = new Municipality();
        municipality.setName("City Municipality");
        municipality.setRegion("Central District");
        municipality.setContactEmail("contact@city.gov");
        municipality.setContactPhone("987654321");
        municipality.setOwner(admin);

        municipalityRepository.save(municipality);
    }

    // ---------------- ACTIVITY PROVIDER ----------------
    private void seedActivityProviders() {
        if (activityProviderRepository.count() > 0) return;

        UserInfo owner = userInfoRepository.findByEmail("activity@provider.com").orElseThrow();

        ActivityProvider provider = new ActivityProvider();
        provider.setName("Fun Science Lab");
        // provider.setDescription("Interactive science activities for students"); // Field removed in new model
        provider.setContactEmail("info@funlab.com");
        provider.setContactPhone("11223344");
        provider.setOwner(owner); // Re-enabled owner field
        provider.setActive(true);
        // provider.setCreatedAt(LocalDateTime.now()); // Field removed in new model

        activityProviderRepository.save(provider);

        UserInfo techOwner = userInfoRepository.findByEmail("tech@provider.com").orElseThrow();
        ActivityProvider techProvider = new ActivityProvider();
        techProvider.setName("Tech Innovators");
        techProvider.setContactEmail("contact@techinnovators.com");
        techProvider.setContactPhone("99887766");
        techProvider.setOwner(techOwner);
        techProvider.setActive(true);
        
        activityProviderRepository.save(techProvider);
    }

    // ---------------- ACTIVITIES ----------------
    private void seedActivities() {
        if (activityRepository.count() > 0) return;

        // Fetch the activity provider we seeded earlier
        ActivityProvider provider = activityProviderRepository.findAll().stream()
                .filter(p -> "Fun Science Lab".equals(p.getName()))
                .findFirst()
                .orElse(null);

        if (provider == null && activityProviderRepository.count() > 0) {
            provider = activityProviderRepository.findAll().get(0);
        }

        ActivityProvider techProvider = activityProviderRepository.findAll().stream()
                .filter(p -> "Tech Innovators".equals(p.getName()))
                .findFirst()
                .orElse(null);

        activityRepository.saveAll(List.of(

                new Activity(
                        null,
                        "AI Career Workshop",
                        "Introduction to artificial intelligence careers",
                        ActivityType.WORKSHOP,
                        60, // suggestedDurationMinutes
                        30, // suggestedMaxParticipants
                        true,
                        provider
                ),
                
                 new Activity(
                        null,
                        "VR Experience",
                        "Immersive Virtual Reality session",
                        ActivityType.DEMO,
                        30, 
                        10,
                        true,
                        techProvider
                ),

                new Activity(
                        null,
                        "Coding Bootcamp Intro",
                        "Learn basics of Python",
                        ActivityType.WORKSHOP,
                        120, 
                        25,
                        true,
                        techProvider
                ),

                new Activity(
                        null,
                        "Robotics Demo",
                        "Live robotics demonstration and interaction",
                        ActivityType.DEMO,
                        45,
                        50,
                        true,
                        provider
                ),

                new Activity(
                        null,
                        "Medical Lab Simulation",
                        "Hands-on medical lab experience",
                        ActivityType.DEMO, // Changed to DEMO as SIMULATION type was not defined in Enum
                        90,
                        20,
                        true,
                        provider
                ),

                new Activity(
                        null,
                        "Engineering Design Challenge",
                        "Problem-solving engineering activity",
                        ActivityType.WORKSHOP, // Changed to WORKSHOP as COMPETITION type was not defined in Enum
                        120,
                        40,
                        true,
                        provider
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
                        true
                )
        ));
    }


}
