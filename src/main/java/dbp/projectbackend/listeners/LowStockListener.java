package dbp.projectbackend.listeners;

import dbp.projectbackend.config.AsyncConfig;
import dbp.projectbackend.dtos.response.report.LowStockDTO;
import dbp.projectbackend.enums.Role;
import dbp.projectbackend.events.SaleRegisteredEvent;
import dbp.projectbackend.events.StockMovementRegisteredEvent;
import dbp.projectbackend.models.ProductVariantModel;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.repositories.ProductVariantRepository;
import dbp.projectbackend.repositories.UserRepository;
import dbp.projectbackend.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;

/**
 * Avisa a los ADMIN cuando una variante queda en su stock mínimo o por debajo,
 * ya sea por una venta o por una salida/ajuste manual de inventario.
 */
@Component
@RequiredArgsConstructor
public class LowStockListener {

    private final ProductVariantRepository varianteRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Async(AsyncConfig.NOTIFICATIONS_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSaleRegistered(SaleRegisteredEvent event) {
        notifyIfLowStock(event.getEmpresaId(), event.getVarianteIds(), "Después de la venta #" + event.getVentaId());
    }

    @Async(AsyncConfig.NOTIFICATIONS_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStockMovementRegistered(StockMovementRegisteredEvent event) {
        if (!event.reducesStock()) {
            return;
        }
        String origen = "Después del movimiento de inventario #" + event.getMovimientoId()
                + " (" + event.getTipo().name() + ", registrado por " + event.getRegistradoPor() + ")";
        notifyIfLowStock(event.getEmpresaId(), List.of(event.getVarianteId()), origen);
    }

    private void notifyIfLowStock(Long empresaId, List<Long> varianteIds, String origen) {
        List<LowStockDTO> enStockBajo = varianteRepository.findAllByIdInWithProducto(varianteIds).stream()
                .filter(ProductVariantModel::isLowStock)
                .map(v -> new LowStockDTO(v.getId(), v.getProducto().getNombre(), v.getTalla(), v.getColor(),
                        v.getStock(), v.getStockMinimo()))
                .toList();

        if (enStockBajo.isEmpty()) {
            return;
        }

        for (UserModel admin : userRepository.findByEmpresaIdAndRole(empresaId, Role.ADMIN)) {
            emailService.send(
                    admin.getEmail(),
                    "Alerta de stock bajo",
                    "email/stock-bajo",
                    Map.of(
                            "nombre", admin.getNombre(),
                            "origen", origen,
                            "variantes", enStockBajo
                    )
            );
        }
    }
}
