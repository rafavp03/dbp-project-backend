package dbp.projectbackend.services;

import dbp.projectbackend.dtos.request.ProductPatchDTO;
import dbp.projectbackend.enums.Role;
import dbp.projectbackend.events.ProductPriceChangedEvent;
import dbp.projectbackend.mappers.ProductMapper;
import dbp.projectbackend.models.EnterpriseModel;
import dbp.projectbackend.models.ProductModel;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.repositories.CategoryRepository;
import dbp.projectbackend.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    private ProductService service;
    private UserModel empleado;
    private ProductModel producto;

    @BeforeEach
    void setUp() {
        ProductMapper mapper = Mappers.getMapper(ProductMapper.class);
        service = new ProductService(productRepository, categoryRepository, mapper, eventPublisher);

        EnterpriseModel empresa = new EnterpriseModel("20123456789", "Moda Gamarra SAC");
        empresa.setId(1L);
        empleado = new UserModel("Luis Huamán", "luis@modagamarra.pe", "hash", Role.EMPLEADO, empresa);

        producto = new ProductModel("POL-001", "Polo básico", new BigDecimal("25.00"), empresa);
        producto.setId(5L);
        producto.setPrecioCompra(new BigDecimal("12.00"));

        when(productRepository.findById(5L)).thenReturn(Optional.of(producto));
        when(productRepository.save(any(ProductModel.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void cambiarElPrecioDeVentaPublicaElEventoConLosPreciosAnterioresYNuevos() {
        ProductPatchDTO patch = new ProductPatchDTO(null, null, null, null, null, new BigDecimal("22.00"), null);

        service.patchProduct(empleado, 5L, patch);

        ArgumentCaptor<ProductPriceChangedEvent> captor = ArgumentCaptor.forClass(ProductPriceChangedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        ProductPriceChangedEvent evento = captor.getValue();
        assertThat(evento.getProductoId()).isEqualTo(5L);
        assertThat(evento.getEmpresaId()).isEqualTo(1L);
        assertThat(evento.getPrecioVentaAnterior()).isEqualByComparingTo("25.00");
        assertThat(evento.getPrecioVentaNuevo()).isEqualByComparingTo("22.00");
        assertThat(evento.getPrecioCompraNuevo()).isEqualByComparingTo("12.00");
        assertThat(evento.getModificadoPor()).isEqualTo("Luis Huamán");
    }

    @Test
    void mismoPrecioConOtraEscalaNoPublicaEvento() {
        ProductPatchDTO patch = new ProductPatchDTO(null, "Polo básico unisex", null, null, null, new BigDecimal("25"), null);

        service.patchProduct(empleado, 5L, patch);

        assertThat(producto.getNombre()).isEqualTo("Polo básico unisex");
        verify(eventPublisher, never()).publishEvent(any(ApplicationEvent.class));
    }
}
