package dbp.projectbackend.listeners;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PriceChangeListenerTest {

    @Test
    void margenEsLaGananciaSobreElPrecioDeVenta() {
        assertThat(PriceChangeListener.margin(new BigDecimal("25.00"), new BigDecimal("12.00")))
                .isEqualByComparingTo("52.0");
    }

    @Test
    void margenEsNullSiFaltaElCosto() {
        assertThat(PriceChangeListener.margin(new BigDecimal("25.00"), null)).isNull();
    }
}
