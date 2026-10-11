package ca.cornalix.diagnostic.common;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Forme generique de reponse paginee (SCRUM-54). N'est renvoyee que
 * lorsque l'appelant demande explicitement page/size -- sans ces
 * parametres, les endpoints renvoient toujours le tableau brut d'avant
 * pour ne rien casser chez les clients existants.
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
