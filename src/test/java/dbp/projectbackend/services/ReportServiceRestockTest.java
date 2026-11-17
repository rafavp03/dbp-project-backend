package dbp.projectbackend.services;

import dbp.projectbackend.dtos.response.report.RestockSuggestionDTO;
import dbp.projectbackend.enums.OrderStatus;
import dbp.projectbackend.models.EnterpriseModel;
import dbp.projectbackend.models.ProductModel;
import dbp.projectbackend.models.ProductVariantModel;
import dbp.projectbackend.models.SalesOrderLineModel;
import dbp.projectbackend.repositories.ProductVariantRepository;
import dbp.projectbackend.repositories.PurchaseOrderRepository;
import dbp.projectbackend.repositories.SalesOrderLineRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceRestockTest {

    @Mock private SalesOrderLineRepository detalleRepository;
    @Mock private ProductVariantRepository varianteRepository;
    @Mock private PurchaseOrderRepository compraRepository;

    @Test
    void sugiereReponerLaTallaQueSeAgotaYNoLaQueTieneStockDeSobra() {
        ReportService service = new ReportService(detalleRepository, varianteRepository, compraRepository);

        EnterpriseModel empresa = new EnterpriseModel("20123456789", "Moda Gamarra SAC");
        empresa.setId(1L);
        ProductModel producto = new ProductModel("POL-001", "Polo básico", new BigDecimal("25.00"), empresa);
        producto.setFechaRegistro(LocalDateTime.now().minusDays(90));

        ProductVariantModel tallaM = new ProductVariantModel("M", "Negro");
        tallaM.setId(10L);
        tallaM.setStock(4);
        producto.addVariant(tallaM);

        ProductVariantModel tallaL = new ProductVariantModel("L", "Negro");
        tallaL.setId(11L);
        tallaL.setStock(100);
        producto.addVariant(tallaL);

        // En 30 dias: la M vendio 30 unidades (1 por dia) y la L solo 3.
        SalesOrderLineModel ventaM = mock(SalesOrderLineModel.class);
        when(ventaM.getVariante()).thenReturn(tallaM);
        when(ventaM.getCantidad()).thenReturn(30);
        SalesOrderLineModel ventaL = mock(SalesOrderLineModel.class);
        when(ventaL.getVariante()).thenReturn(tallaL);
        when(ventaL.getCantidad()).thenReturn(3);

        when(detalleRepository.findVendidosEnPeriodo(eq(1L), eq(OrderStatus.COMPLETADA), any(), any()))
                .thenReturn(List.of(ventaM, ventaL));
        when(varianteRepository.findByProductoEmpresaId(1L)).thenReturn(List.of(tallaM, tallaL));
        when(compraRepository.findComprasDeVariantes(eq(1L), eq(OrderStatus.ANULADA), anyList()))
                .thenReturn(List.of());

        List<RestockSuggestionDTO> sugerencias = service.restockSuggestions(1L, 30, 21);

        assertThat(sugerencias).hasSize(1);
        RestockSuggestionDTO sugerencia = sugerencias.get(0);
        assertThat(sugerencia.varianteId()).isEqualTo(10L);
        assertThat(sugerencia.diasRestantes()).isEqualTo(4);
        assertThat(sugerencia.urgencia()).isEqualTo("CRITICO");
        // 21 dias de cobertura a 1 unidad por dia = 21; menos las 4 que quedan = 17.
        assertThat(sugerencia.cantidadSugerida()).isEqualTo(17);
        assertThat(sugerencia.proveedorId()).isNull();
    }
}
