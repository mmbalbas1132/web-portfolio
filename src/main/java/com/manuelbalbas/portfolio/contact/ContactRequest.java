package com.manuelbalbas.portfolio.contact;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para el endpoint de contacto.
 * <p>
 * Se usa solo para recibir y validar los datos del formulario
 * antes de mapearlos a la entidad persistente.
 */
public class ContactRequest {

    @NotBlank
    @Size(max = 80)
    private String nombre;

    @NotBlank
    @Email
    @Size(max = 120)
    private String email;

    @NotBlank
    @Size(max = 2000)
    private String mensaje;

    public String getNombre() {
        return nombre;
    }

    /**
     * Se hace trim para evitar espacios sobrantes al principio/fin,
     * pero sin modificar el valor original más allá de eso.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre != null ? nombre.trim() : null;
    }

    public String getEmail() {
        return email;
    }

    /**
     * Se normaliza eliminando espacios sobrantes. La anotación @Email
     * se encarga de validar el formato básico.
     */
    public void setEmail(String email) {
        this.email = email != null ? email.trim() : null;
    }

    public String getMensaje() {
        return mensaje;
    }

    /**
     * Se recorta solo espacios externos; el contenido interno se mantiene
     * tal cual lo ha escrito el usuario.
     */
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje != null ? mensaje.trim() : null;
    }
}

