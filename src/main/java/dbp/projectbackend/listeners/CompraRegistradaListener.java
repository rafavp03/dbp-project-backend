package dbp.projectbackend.listeners;

import dbp.projectbackend.config.AsyncConfig;
import dbp.projectbackend.enums.Role;
import dbp.projectbackend.events.CompraRegistradaEvent;
import dbp.projectbackend.models.UserModel;
import dbp.projectbackend.repositories.UserRepository;
import dbp.projectbackend.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CompraRegistradaListener {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final UserRepository userRepository;
    private final EmailService emailService;

    @Async(AsyncConfig.NOTIFICACIONES_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCompraRegistrada(CompraRegistradaEvent event) {
        userRepository.findByEmpresaId(event.getEmpresaId()).stream()
                .filter(u -> u.getRole() == Role.ADMIN)
                .forEach(admin -> enviarConfirmacion(admin, event));
    }

    private void enviarConfirmacion(UserModel admin, CompraRegistradaEvent event) {
        emailService.enviar(
                admin.getEmail(),
                "Compra registrada #" + event.getCompraId(),
                "email/compra-registrada",
                Map.of(
                        "nombre", admin.getNombre(),
                        "compraId", event.getCompraId(),
                        "proveedor", event.getProveedor(),
                        "fecha", event.getFecha().format(FORMATO_FECHA),
                        "total", event.getTotal(),
                        "lineas", event.getLineas()
                )
        );
    }
}
