package com.manuelbalbas.portfolio.contact;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controlador REST que expone el endpoint de contacto del portafolio.
 * <p>
 * Responsabilidades principales:
 * <ul>
 *   <li>Validar la entrada del formulario.</li>
 *   <li>Aplicar un pequeño rate limiting por IP para evitar abuso.</li>
 *   <li>Persistir el mensaje en la base de datos.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/contact")
@CrossOrigin(
        origins = {"*", "null"},
        allowedHeaders = {"Content-Type", "Accept"},
        methods = {org.springframework.web.bind.annotation.RequestMethod.POST, org.springframework.web.bind.annotation.RequestMethod.OPTIONS}
)
public class ContactController {

    private static final Logger log = LoggerFactory.getLogger(ContactController.class);

    // Rate limiting muy sencillo en memoria: máx. 5 peticiones por IP cada 5 minutos
    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_MILLIS = 5 * 60 * 1000L;

    private final Map<String, Deque<Long>> requestsPerIp = new ConcurrentHashMap<>();
    private final ContactMessageRepository repository;

    public ContactController(ContactMessageRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<?> handleContact(
            @Valid @RequestBody ContactRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest
    ) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Datos no válidos en el formulario."
            ));
        }

        String clientIp = resolveClientIp(httpRequest);
        if (!isAllowed(clientIp)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                    "status", "error",
                    "message", "Demasiadas solicitudes. Inténtalo de nuevo más tarde."
            ));
        }

        ContactMessage entity = new ContactMessage(
                request.getNombre(),
                request.getEmail(),
                request.getMensaje(),
                clientIp
        );
        repository.save(entity);

        log.info(
                "Nuevo mensaje de contacto desde IP {}: nombre='{}', email='{}', longitudMensaje={}",
                clientIp,
                safeLogValue(request.getNombre(), 80),
                safeLogValue(request.getEmail(), 120),
                request.getMensaje() != null ? request.getMensaje().length() : 0
        );

        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "message", "Mensaje recibido correctamente."
        ));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");
        if (header != null && !header.isBlank()) {
            return header.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private boolean isAllowed(String ip) {
        long now = Instant.now().toEpochMilli();
        Deque<Long> deque = requestsPerIp.computeIfAbsent(ip, k -> new ArrayDeque<>());

        synchronized (deque) {
            while (!deque.isEmpty() && now - deque.peekFirst() > WINDOW_MILLIS) {
                deque.pollFirst();
            }
            if (deque.size() >= MAX_REQUESTS) {
                return false;
            }
            deque.addLast(now);
            return true;
        }
    }

    private String safeLogValue(String value, int maxLen) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxLen) {
            return value;
        }
        return value.substring(0, maxLen) + "...";
    }
}

