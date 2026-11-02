package dbp.projectbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

/**
 * Habilita {@code Pageable} como parámetro de los controllers ({@code ?page=0&size=20&sort=nombre,asc})
 * y fija un tamaño máximo de página para que nadie pueda pedir toda la tabla de golpe.
 * Los controllers no devuelven {@code Page} directamente sino {@code PageResponseDTO}, así el JSON
 * de los listados es estable y no depende de la serialización interna de Spring Data.
 */
@Configuration
@EnableSpringDataWebSupport
public class PaginationConfig {

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    @Bean
    public PageableHandlerMethodArgumentResolverCustomizer pageableCustomizer() {
        return resolver -> {
            resolver.setMaxPageSize(MAX_PAGE_SIZE);
            resolver.setFallbackPageable(PageRequest.of(0, DEFAULT_PAGE_SIZE));
        };
    }
}
