package org.example.ducksocialnetworkm.domeniu.relatie;

import java.time.LocalDateTime;

public class CererePrietenie {
    private Long id;
    private Long idExpeditor;
    private Long idDestinatar;
    private String status;
    private LocalDateTime data;


    public CererePrietenie(Long id, Long idExpeditor, Long idDestinatar, String status, LocalDateTime data) {
        this.id = id;
        this.idExpeditor = idExpeditor;
        this.idDestinatar = idDestinatar;
        this.status = status;
        this.data = data;
    }

    public Long getId() { return id; }
    public Long getIdExpeditor() { return idExpeditor; }
    public Long getIdDestinatar() { return idDestinatar; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getData() { return data; }

    @Override
    public String toString() {
        return "Cerere #" + id + " | " + idExpeditor + " -> " + idDestinatar + ": " + status;
    }
}