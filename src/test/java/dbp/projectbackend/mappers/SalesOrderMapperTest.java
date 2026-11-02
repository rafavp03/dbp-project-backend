package dbp.projectbackend.mappers;

import dbp.projectbackend.dtos.response.SalesOrderResponseDTO;
import dbp.projectbackend.enums.PaymentMethod;
import dbp.projectbackend.enums.SalesChannel;
import dbp.projectbackend.models.ClientModel;
import dbp.projectbackend.models.EnterpriseModel;
import dbp.projectbackend.models.ProductModel;
import dbp.projectbackend.models.ProductVariantModel;
import dbp.projectbackend.models.SalesOrderLineModel;
import dbp.projectbackend.models.SalesOrderModel;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SalesOrderMapperTest {

    private final SalesOrderMapper mapper = Mappers.getMapper(SalesOrderMapper.class);

    @Test
    void toResponseMapeaCabeceraYDetallesAnidados() {
        EnterpriseModel empresa = new EnterpriseModel("20123456789", "Moda Gamarra SAC");
        empresa.setId(1L);
        ProductModel producto = new ProductModel("POL-001", "Polo básico", new BigDecimal("25.00"), empresa);
        ProductVariantModel variante = new ProductVariantModel("M", "Negro");
        variante.setId(10L);
        producto.addVariant(variante);
        ClientModel cliente = new ClientModel("71234567", "María Torres", empresa);
        cliente.setId(4L);

        SalesOrderModel orden = new SalesOrderModel(empresa, cliente, PaymentMethod.YAPE, SalesChannel.WHATSAPP);
        orden.setId(99L);
        orden.addLine(new SalesOrderLineModel(variante, 2, new BigDecimal("23.00")));
        orden.recalculateTotal();

        SalesOrderResponseDTO dto = mapper.toResponse(orden);

        assertThat(dto.id()).isEqualTo(99L);
        assertThat(dto.empresaId()).isEqualTo(1L);
        assertThat(dto.clienteId()).isEqualTo(4L);
        assertThat(dto.clienteNombre()).isEqualTo("María Torres");
        assertThat(dto.medioPago()).isEqualTo(PaymentMethod.YAPE);
        assertThat(dto.total()).isEqualByComparingTo("46.00");
        assertThat(dto.detalles()).singleElement().satisfies(linea -> {
            assertThat(linea.varianteId()).isEqualTo(10L);
            assertThat(linea.varianteDescripcion()).isEqualTo("Polo básico - M - Negro");
            assertThat(linea.cantidad()).isEqualTo(2);
            assertThat(linea.subtotal()).isEqualByComparingTo("46.00");
        });
    }

    @Test
    void ventaSinClienteDejaLosDatosDelClienteEnNull() {
        EnterpriseModel empresa = new EnterpriseModel("20123456789", "Moda Gamarra SAC");
        SalesOrderModel orden = new SalesOrderModel(empresa, null, PaymentMethod.EFECTIVO, SalesChannel.TIENDA);

        SalesOrderResponseDTO dto = mapper.toResponse(orden);

        assertThat(dto.clienteId()).isNull();
        assertThat(dto.clienteNombre()).isNull();
        assertThat(dto.detalles()).isEmpty();
    }
}
