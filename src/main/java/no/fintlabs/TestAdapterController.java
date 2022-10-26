package no.fintlabs;

import no.fintlabs.portal.model.adapter.Adapter;
import no.fintlabs.portal.model.adapter.AdapterService;
import no.fintlabs.portal.model.organisation.Organisation;
import no.fintlabs.portal.model.organisation.OrganisationService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("test")
public class TestAdapterController {

    private final OrganisationService organisationService;
    private final AdapterService adapterService;

    public TestAdapterController(OrganisationService organisationService, AdapterService adapterService) {
        this.organisationService = organisationService;
        this.adapterService = adapterService;
    }

    @PostMapping("adapter/{orgName}")
    public ResponseEntity<Adapter> addAdapter(@PathVariable("orgName") final String orgName,
                                              @RequestBody final Adapter adapter) {

        Optional<Organisation> organisation = organisationService.getOrganisation(orgName);

        Optional<Adapter> optionalAdapter = adapterService.getAdapter(adapter.getName(), orgName);
        if (!optionalAdapter.isPresent()) {
            if (adapterService.addAdapter(adapter, organisation.get())) {

                return ResponseEntity.status(HttpStatus.CREATED).cacheControl(CacheControl.noStore()).body(adapter);
            }
        }

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("adapter/{orgName}/{adapterName}")
    public ResponseEntity<Void> deleteAdapter(@PathVariable("orgName") final String orgName,
                                              @PathVariable final String adapterName) {
        Optional<Organisation> organisation = organisationService.getOrganisation(orgName);
        Optional<Adapter> adapter = adapterService.getAdapter(adapterName, organisation.get().getName());

        adapterService.deleteAdapter(adapter.get());
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }


}
