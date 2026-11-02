package dbp.projectbackend.mappers;

import dbp.projectbackend.dtos.request.ProductDTO;
import dbp.projectbackend.dtos.request.ProductPatchDTO;
import dbp.projectbackend.dtos.response.ProductResponseDTO;
import dbp.projectbackend.models.CategoryModel;
import dbp.projectbackend.models.EnterpriseModel;
import dbp.projectbackend.models.ProductModel;
import dbp.projectbackend.models.ProductVariantModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    private EnterpriseModel empresa;
    private ProductModel producto;

    @BeforeEach
    void setUp() {
        empresa = new EnterpriseModel("20123456789", "Moda Gamarra SAC");
        empresa.setId(7L);

        producto = new ProductModel("POL-001", "Polo básico", new BigDecimal("25.00"), empresa);
        producto.setId(1L);
        producto.setPrecioCompra(new BigDecimal("12.00"));
        producto.setUnidadMedida("UNIDAD");

        ProductVariantModel m = new ProductVariantModel("M", "Negro");
        m.setStock(10);
        ProductVariantModel s = new ProductVariantModel("S", "Blanco");
        s.setStock(5);
        producto.addVariant(m);
        producto.addVariant(s);
    }

    @Test
    void toResponseAplanaEmpresaCategoriaYStockTotal() {
        CategoryModel categoria = new CategoryModel("Polos", empresa);
        categoria.setId(3L);
        producto.setCategoria(categoria);

        ProductResponseDTO dto = mapper.toResponse(producto);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.codigo()).isEqualTo("POL-001");
        assertThat(dto.empresaId()).isEqualTo(7L);
        assertThat(dto.categoriaId()).isEqualTo(3L);
        assertThat(dto.categoriaNombre()).isEqualTo("Polos");
        assertThat(dto.stockTotal()).isEqualTo(15);
        assertThat(dto.precioVenta()).isEqualByComparingTo("25.00");
    }

    @Test
    void toResponseSinCategoriaDevuelveCamposDeCategoriaEnNull() {
        ProductResponseDTO dto = mapper.toResponse(producto);

        assertThat(dto.categoriaId()).isNull();
        assertThat(dto.categoriaNombre()).isNull();
    }

    @Test
    void updateEntityNoBorraLaUnidadDeMedidaSiVieneVacia() {
        ProductDTO dto = new ProductDTO("POL-002", "Polo oversize", "Algodón", " ",
                new BigDecimal("15.00"), new BigDecimal("35.00"), null);

        mapper.updateEntity(dto, producto);

        assertThat(producto.getCodigo()).isEqualTo("POL-002");
        assertThat(producto.getNombre()).isEqualTo("Polo oversize");
        assertThat(producto.getPrecioVenta()).isEqualByComparingTo("35.00");
        assertThat(producto.getUnidadMedida()).isEqualTo("UNIDAD");
        assertThat(producto.getEmpresa()).isSameAs(empresa);
        assertThat(producto.getId()).isEqualTo(1L);
    }

    @Test
    void patchEntitySoloCambiaLosCamposEnviados() {
        ProductPatchDTO patch = new ProductPatchDTO(null, null, null, null, null, new BigDecimal("22.00"), null);

        mapper.patchEntity(patch, producto);

        assertThat(producto.getPrecioVenta()).isEqualByComparingTo("22.00");
        assertThat(producto.getCodigo()).isEqualTo("POL-001");
        assertThat(producto.getNombre()).isEqualTo("Polo básico");
        assertThat(producto.getPrecioCompra()).isEqualByComparingTo("12.00");
    }
}
