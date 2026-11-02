package dbp.projectbackend.mappers;

import org.mapstruct.MapperConfig;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * Configuración común de todos los mappers de MapStruct.
 * <ul>
 *     <li>Los mappers se registran como beans de Spring para inyectarlos en los services.</li>
 *     <li>{@code unmappedTargetPolicy = ERROR}: si un DTO de respuesta gana un campo y nadie lo mapea,
 *     la compilación falla en vez de devolver {@code null} en silencio.</li>
 * </ul>
 */
@MapperConfig(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface CentralMapperConfig {
}
