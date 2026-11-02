package dbp.projectbackend.listeners;

import dbp.projectbackend.config.AsyncConfig;
import dbp.projectbackend.enums.Role;
import dbp.projectbackend.events.ProductPriceChangedEvent;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.repositories.UserRepository;
import dbp.projectbackend.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * Informa a los ADMIN cada cambio de precio o costo, con el margen antes y después,
 * para que ningún empleado cambie precios sin que el dueño lo sepa.
 */
@Component
@RequiredArgsConstructor
public class PriceChangeListener {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private final UserRepository userRepository;
    private final EmailService emailService;

    @Async(AsyncConfig.NOTIFICATIONS_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPriceChanged(ProductPriceChangedEvent event) {
        for (UserModel admin : userRepository.findByEmpresaIdAndRole(event.getEmpresaId(), Role.ADMIN)) {
            // HashMap porque algunos valores (el costo) pueden ser null y Map.of no lo permite.
            Map<String, Object> variables = new HashMap<>();
            variables.put("nombre", admin.getNombre());
            variables.put("producto", event.getCodigo() + " - " + event.getNombre());
            variables.put("precioVentaAnterior", event.getPrecioVentaAnterior());
            variables.put("precioVentaNuevo", event.getPrecioVentaNuevo());
            variables.put("precioCompraAnterior", event.getPrecioCompraAnterior());
            variables.put("precioCompraNuevo", event.getPrecioCompraNuevo());
            variables.put("margenAnterior", margin(event.getPrecioVentaAnterior(), event.getPrecioCompraAnterior()));
            variables.put("margenNuevo", margin(event.getPrecioVentaNuevo(), event.getPrecioCompraNuevo()));
            variables.put("modificadoPor", event.getModificadoPor());

            emailService.send(admin.getEmail(), "Cambio de precio: " + event.getNombre(),
                    "email/cambio-precio", variables);
        }
    }

    /** Margen bruto en porcentaje: (venta - costo) / venta * 100. Null si falta algún dato. */
    static BigDecimal margin(BigDecimal precioVenta, BigDecimal costo) {
        if (precioVenta == null || costo == null || precioVenta.signum() == 0) {
            return null;
        }
        return precioVenta.subtract(costo)
                .multiply(CIEN)
                .divide(precioVenta, 1, RoundingMode.HALF_UP);
    }
}
