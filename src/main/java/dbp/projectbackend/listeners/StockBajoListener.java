package dbp.projectbackend.listeners;

import dbp.projectbackend.config.AsyncConfig;
import dbp.projectbackend.dtos.response.report.StockBajoDTO;
import dbp.projectbackend.enums.Role;
import dbp.projectbackend.events.VentaRegistradaEvent;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.models.VarianteProductoModel;
import dbp.projectbackend.repositories.UserRepository;
import dbp.projectbackend.repositories.VarianteProductoRepository;
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
public class StockBajoListener {

    private final VarianteProductoRepository varianteRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Async(AsyncConfig.NOTIFICACIONES_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVentaRegistrada(VentaRegistradaEvent event) {
        List<StockBajoDTO> enStockBajo = varianteRepository.findAllByIdInWithProducto(event.getVarianteIds()).stream()
                .filter(VarianteProductoModel::isStockBajo)
                .map(v -> new StockBajoDTO(v.getId(), v.getProducto().getNombre(), v.getTalla(), v.getColor(),
                        v.getStock(), v.getStockMinimo()))
                .toList();

        if (enStockBajo.isEmpty()) {
            return;
        }

        for (UserModel admin : administradores(event.getEmpresaId())) {
            emailService.enviar(
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

    private List<UserModel> administradores(Long empresaId) {
        return userRepository.findByEmpresaId(empresaId).stream()
                .filter(u -> u.getRole() == Role.ADMIN)
                .toList();
    }
}
