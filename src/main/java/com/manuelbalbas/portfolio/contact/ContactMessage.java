package com.manuelbalbas.portfolio.contact;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Entidad JPA que representa un mensaje de contacto almacenado en la base de datos.
 * <p>
 * Se guarda el contenido básico del formulario junto con la IP de origen y
 * la marca temporal de creación.
 */
@Entity
@Table(name = "contact_messages")
public class ContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(nullable = false, length = 2000)
    private String mensaje;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(length = 64)
    private String clientIp;

    /**
     * Constructor protegido requerido por JPA (no usar directamente).
     */
    protected ContactMessage() {
    }

    /**
     * Crea una nueva instancia lista para persistir.
     *
     * @param nombre   nombre de la persona que contacta
     * @param email    email de contacto
     * @param mensaje  contenido del mensaje
     * @param clientIp IP resuelta en el servidor (puede ser proxy/pasarela)
     */
    public ContactMessage(String nombre, String email, String mensaje, String clientIp) {
        this.nombre = nombre;
        this.email = email;
        this.mensaje = mensaje;
        this.clientIp = clientIp;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getMensaje() {
        return mensaje;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getClientIp() {
        return clientIp;
    }
}

