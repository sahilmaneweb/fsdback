package com.example.entity;

import jakarta.persistence.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "batches")
public class Batch {

    @Id
    @Column(name = "batch_id", unique = true, nullable = false)
    private String batchId; // Format: 'batch-1', 'batch-2', etc.

    @Column(name = "venue", nullable = false)
    private String venue;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<Group> groups; // One Batch can have multiple Groups

    // Constructors
    public Batch() {}

    public Batch(String batchId, String venue) {
        this.batchId = batchId;
        this.venue = venue;
    }

    // Getters and Setters
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public List<Group> getGroups() { return groups; }
    public void setGroups(List<Group> groups) { this.groups = groups; }
}
