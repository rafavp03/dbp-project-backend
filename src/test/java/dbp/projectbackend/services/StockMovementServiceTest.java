package dbp.projectbackend.services;

import dbp.projectbackend.dtos.request.StockAdjustmentDTO;
import dbp.projectbackend.dtos.response.StockMovementResponseDTO;
import dbp.projectbackend.enums.MovementType;
import dbp.projectbackend.enums.Role;
import dbp.projectbackend.events.StockMovementRegisteredEvent;
import dbp.projectbackend.mappers.ProductVariantMapper;
import dbp.projectbackend.mappers.StockMovementMapper;
import dbp.projectbackend.models.EnterpriseModel;
import dbp.projectbackend.models.ProductModel;
import dbp.projectbackend.models.ProductVariantModel;
import dbp.projectbackend.models.StockMovementModel;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.repositories.ProductVariantRepository;
import dbp.projectbackend.repositories.StockMovementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockMovementServiceTest {

    @Mock private StockMovementRepository movimientoRepository;
    @Mock private ProductVariantRepository varianteRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @Test
    void ajusteDeInventarioPublicaEventoConLaDiferencia() {
        StockMovementService service = new StockMovementService(movimientoRepository, varianteRepository,
                Mappers.getMapper(StockMovementMapper.class), Mappers.getMapper(ProductVariantMapper.class),
                eventPublisher);

        EnterpriseModel empresa = new EnterpriseModel("20123456789", "Moda Gamarra SAC");
        empresa.setId(1L);
        UserModel empleado = new UserModel("Luis Huamán", "luis@modagamarra.pe", "hash", Role.EMPLEADO, empresa);
        ProductModel producto = new ProductModel("POL-001", "Polo básico", new BigDecimal("25.00"), empresa);
        ProductVariantModel variante = new ProductVariantModel("M", "Negro");
        variante.setId(10L);
        variante.setStock(12);
        producto.addVariant(variante);

        when(varianteRepository.findById(10L)).thenReturn(Optional.of(variante));
        when(movimientoRepository.save(any(StockMovementModel.class))).thenAnswer(inv -> inv.getArgument(0));

        StockMovementResponseDTO respuesta = service.registerAdjustment(
                empleado, new StockAdjustmentDTO(10L, 9, "Conteo de fin de mes"));

        assertThat(respuesta.tipo()).isEqualTo(MovementType.AJUSTE);
        assertThat(respuesta.stockResultante()).isEqualTo(9);

        ArgumentCaptor<StockMovementRegisteredEvent> captor = ArgumentCaptor.forClass(StockMovementRegisteredEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        StockMovementRegisteredEvent evento = captor.getValue();
        assertThat(evento.getEmpresaId()).isEqualTo(1L);
        assertThat(evento.getVarianteDescripcion()).isEqualTo("Polo básico - M - Negro");
        assertThat(evento.getStockAnterior()).isEqualTo(12);
        assertThat(evento.getStockResultante()).isEqualTo(9);
        assertThat(evento.getDiferencia()).isEqualTo(-3);
        assertThat(evento.reducesStock()).isTrue();
        assertThat(evento.getRegistradoPor()).isEqualTo("Luis Huamán");
    }
}
