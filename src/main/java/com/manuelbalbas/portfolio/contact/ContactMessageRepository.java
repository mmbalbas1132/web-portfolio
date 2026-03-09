package com.manuelbalbas.portfolio.contact;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA para acceder a los mensajes de contacto.
 * <p>
 * Por ahora se usa solo para inserciones, pero permite también
 * consultas futuras (listado, filtros, etc.).
 */
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
}

