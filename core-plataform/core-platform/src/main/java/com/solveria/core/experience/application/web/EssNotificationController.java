package com.solveria.core.experience.application.web;
import com.solveria.core.experience.application.dto.NotificationResponseDTO;
import com.solveria.core.experience.application.port.in.NotificationPI;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ess/me/notifications")
@RequiredArgsConstructor
@Tag(name = "ESS - Notifications", description = "Bandeja de entrada y notificaciones del empleado")
public class EssNotificationController {

    private final NotificationPI notificationPI;

    // Helper method para obtener el personId (igual que el otro controller)
    private UUID getCurrentPersonId() {
        return UUID.randomUUID(); // Placeholder a tu SecurityContext
    }

    // --- OPERACIONES DE LECTURA ---

//    @GetMapping
//    @Operation(summary = "Listar mis notificaciones", description = "Obtiene la bandeja de notificaciones del usuario logueado")
//    public ResponseEntity<List<NotificationResponseDTO>> getMyNotifications(
//            @RequestParam(defaultValue = "false") boolean unreadOnly) {
//        UUID personId = getCurrentPersonId();
//        // Asume nuevo método de query en el puerto de entrada
//        List<NotificationResponseDTO> notifications = notificationPI.getNotifications(personId, unreadOnly);
//        return ResponseEntity.ok(notifications);
//    }

    // --- OPERACIONES DE MUTACIÓN ---

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "Marcar notificación como leída", description = "Registra la fecha de lectura de una notificación regular")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID notificationId) {

        notificationPI.markAsRead( notificationId);
        return ResponseEntity.noContent().build();
    }

//    @PostMapping("/{notificationId}/acknowledge")
//    @Operation(summary = "Acuse de recibo legal (Firma)", description = "Firma digitalmente un memorando o notificación formal (W12)")
//    public ResponseEntity<Void> acknowledgeNotification(@PathVariable UUID notificationId, @RequestBody String digitalSignatureToken) {
//        UUID personId = getCurrentPersonId();
//        // Valida la firma del token contra el usuario logueado antes de impactar el Core
//        notificationId.acknowledgeLegalNotification(personId, notificationId, digitalSignatureToken);
//        return ResponseEntity.ok().build();
//    }
}