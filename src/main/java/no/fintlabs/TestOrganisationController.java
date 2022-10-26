package no.fintlabs;

import no.fintlabs.portal.model.organisation.Organisation;
import no.fintlabs.portal.model.organisation.OrganisationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("test/organisation")
public class TestOrganisationController {

    private final OrganisationService organisationService;

    public TestOrganisationController(OrganisationService organisationService) {
        this.organisationService = organisationService;
    }

    // {name: "test.no", orgNumber: "123456789", displayName: "Test"}
    @GetMapping
    public ResponseEntity<Void> createOrganisation() {
        Organisation organisation = new Organisation();

        organisation.setName("egil.no");
        organisation.setOrgNumber("123456789");
        organisation.setDisplayName("The Egil Company");
        organisationService.createOrganisation(organisation);

        return ResponseEntity.ok().build();
    }
}
