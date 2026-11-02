package dbp.projectbackend.dtos;

import dbp.projectbackend.dtos.response.PageResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseDTOTest {

    @Test
    void fromCopiaLosDatosDePaginacion() {
        PageRequest pedido = PageRequest.of(1, 2, Sort.by("nombre"));
        PageImpl<String> pagina = new PageImpl<>(List.of("c", "d"), pedido, 5);

        PageResponseDTO<String> dto = PageResponseDTO.from(pagina);

        assertThat(dto.content()).containsExactly("c", "d");
        assertThat(dto.page()).isEqualTo(1);
        assertThat(dto.size()).isEqualTo(2);
        assertThat(dto.totalElements()).isEqualTo(5);
        assertThat(dto.totalPages()).isEqualTo(3);
        assertThat(dto.first()).isFalse();
        assertThat(dto.last()).isFalse();
        assertThat(dto.sort()).isEqualTo("nombre: ASC");
    }
}
