package com.capstone.personalityTest.controller.Exhibition;

import com.capstone.personalityTest.model.Exhibition.Exhibition;
import com.capstone.personalityTest.service.Exhibition.ExhibitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exhibitions")
@RequiredArgsConstructor
public class ExhibitionController {

    private final ExhibitionService exhibitionService;

    @PostMapping("/{orgId}")
    public ResponseEntity<Exhibition> createExhibition(@PathVariable Long orgId, @RequestBody Exhibition exhibition, Authentication authentication) {
        Exhibition created = exhibitionService.createExhibition(orgId, exhibition, authentication.getName());
        return ResponseEntity.ok(created);
    }

    @GetMapping("/organization/{orgId}")
    public ResponseEntity<List<Exhibition>> getExhibitionsByOrg(@PathVariable Long orgId) {
        List<Exhibition> exhibitions = exhibitionService.getExhibitionsByOrg(orgId);
        return ResponseEntity.ok(exhibitions);
    }
}
