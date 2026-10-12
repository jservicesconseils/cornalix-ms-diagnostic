package ca.cornalix.diagnostic.question;

import ca.cornalix.diagnostic.question.dto.QuestionImportResult;
import ca.cornalix.diagnostic.question.dto.QuestionRequest;
import ca.cornalix.diagnostic.question.dto.QuestionResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/diagnostics/questions")
public class QuestionController {

    private final QuestionService service;

    public QuestionController(QuestionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<QuestionResponse> create(@Valid @RequestBody QuestionRequest request) {
        QuestionResponse created = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/diagnostics/questions/" + created.id()))
                .body(created);
    }

    /**
     * SCRUM-54 : page/size restent optionnels -- sans eux, on renvoie le
     * meme tableau brut qu'avant (pas de rupture pour les clients
     * existants, dont le front qui charge tout le catalogue d'un coup).
     */
    @GetMapping
    public Object list(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
        if (page == null && size == null) {
            return service.listAll();
        }
        return service.listPaged(page == null ? 0 : page, size == null ? 20 : size);
    }

    @GetMapping("/{id}")
    public QuestionResponse getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public QuestionResponse update(@PathVariable UUID id, @Valid @RequestBody QuestionRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Ecran d'administration des questions : import en masse par CSV
    // (colonnes cisControl, cisSafeguard, nistFunction, implementationGroup,
    // text_fr, text_en). Pas de restriction de role pour l'instant --
    // meme garantie que les autres routes de ce controleur (tout jeton
    // valide suffit), a restreindre plus tard au role OWNER.
    @PostMapping("/import")
    public QuestionImportResult importCsv(@RequestParam("file") MultipartFile file) {
        try {
            return service.importFromCsv(file.getInputStream());
        } catch (IOException e) {
            throw new UncheckedIOException("Impossible de lire le fichier envoye", e);
        }
    }
}
