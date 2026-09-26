package dbp.projectbackend.listeners;

import dbp.projectbackend.config.AsyncConfig;
import dbp.projectbackend.dtos.response.report.LowStockDTO;
import dbp.projectbackend.enums.Role;
import dbp.projectbackend.events.SaleRegisteredEvent;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.models.ProductVariantModel;
import dbp.projectbackend.repositories.UserRepository;
import dbp.projectbackend.repositories.ProductVariantRepository;
import dbp.projectbackend.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LowStockListener {

    private final ProductVariantRepository varianteRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Async(AsyncConfig.NOTIFICATIONS_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSaleRegistered(SaleRegisteredEvent event) {
        List<LowStockDTO> enStockBajo = varianteRepository.findAllByIdInWithProducto(event.getVarianteIds()).stream()
                .filter(ProductVariantModel::isLowStock)
                .map(v -> new LowStockDTO(v.getId(), v.getProducto().getNombre(), v.getTalla(), v.getColor(),
                        v.getStock(), v.getStockMinimo()))
                .toList();

        if (enStockBajo.isEmpty()) {
            return;
        }

        for (UserModel admin : admins(event.getEmpresaId())) {
            emailService.send(
                    admin.getEmail(),
                    "Alerta de stock bajo",
                    "email/stock-bajo",
                    Map.of(
                            "nombre", admin.getNombre(),
                            "ventaId", event.getVentaId(),
                            "variantes", enStockBajo
                    )
            );
        }
    }

    private List<UserModel> admins(Long empresaId) {
        return userRepository.findByEmpresaId(empresaId).stream()
                .filter(u -> u.getRole() == Role.ADMIN)
                .toList();
    }
}
