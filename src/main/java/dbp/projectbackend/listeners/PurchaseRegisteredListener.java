package dbp.projectbackend.listeners;

import dbp.projectbackend.config.AsyncConfig;
import dbp.projectbackend.enums.Role;
import dbp.projectbackend.events.PurchaseRegisteredEvent;
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
public class PurchaseRegisteredListener {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final UserRepository userRepository;
    private final EmailService emailService;

    @Async(AsyncConfig.NOTIFICATIONS_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPurchaseRegistered(PurchaseRegisteredEvent event) {
        userRepository.findByEmpresaId(event.getEmpresaId()).stream()
                .filter(u -> u.getRole() == Role.ADMIN)
                .forEach(admin -> sendConfirmation(admin, event));
    }

    private void sendConfirmation(UserModel admin, PurchaseRegisteredEvent event) {
        emailService.send(
                admin.getEmail(),
                "Compra registrada #" + event.getCompraId(),
                "email/compra-registrada",
                Map.of(
                        "nombre", admin.getNombre(),
                        "compraId", event.getCompraId(),
                        "proveedor", event.getProveedor(),
                        "fecha", event.getFecha().format(DATE_FORMAT),
                        "total", event.getTotal(),
                        "lineas", event.getLineas()
                )
        );
    }
}
