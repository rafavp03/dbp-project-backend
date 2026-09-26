package dbp.projectbackend.models;

import dbp.projectbackend.exceptions.InsufficientStockException;
import dbp.projectbackend.exceptions.InvalidOperationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductVariantModelTest {

    private ProductVariantModel varianteConStock(int stock, int stockMinimo) {
        ProductVariantModel variante = new ProductVariantModel("M", "Negro");
        variante.setStock(stock);
        variante.setStockMinimo(stockMinimo);
        return variante;
    }

    @Test
    void decreaseStockDescuentaLaCantidadSolicitada() {
        ProductVariantModel variante = varianteConStock(10, 2);

        variante.decreaseStock(3);

        assertThat(variante.getStock()).isEqualTo(7);
    }

    @Test
    void decreaseStockRechazaMasUnidadesDeLasDisponibles() {
        ProductVariantModel variante = varianteConStock(2, 0);

        assertThatThrownBy(() -> variante.decreaseStock(3))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("disponible: 2")
                .hasMessageContaining("solicitado: 3");

        assertThat(variante.getStock()).isEqualTo(2);
    }

    @Test
    void decreaseStockRechazaCantidadesNoPositivas() {
        ProductVariantModel variante = varianteConStock(5, 0);

        assertThatThrownBy(() -> variante.decreaseStock(0))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void decreaseStockPermiteAgotarElStockExacto() {
        ProductVariantModel variante = varianteConStock(3, 1);

        variante.decreaseStock(3);

        assertThat(variante.getStock()).isZero();
    }

    @Test
    void isLowStockIncluyeElStockMinimoComoLimite() {
        assertThat(varianteConStock(2, 5).isLowStock()).isTrue();
        assertThat(varianteConStock(5, 5).isLowStock()).isTrue();
        assertThat(varianteConStock(6, 5).isLowStock()).isFalse();
    }

    @Test
    void increaseStockSumaYRechazaCantidadesNoPositivas() {
        ProductVariantModel variante = varianteConStock(4, 1);

        variante.increaseStock(6);

        assertThat(variante.getStock()).isEqualTo(10);
        assertThatThrownBy(() -> variante.increaseStock(-1))
                .isInstanceOf(InvalidOperationException.class);
    }
}
