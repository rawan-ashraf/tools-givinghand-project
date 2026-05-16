package com.example.givinghand.entity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "warehouses")
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // el organization elly bt-own el warehouse
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private user organization;

    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WarehouseItem> items = new ArrayList<>();

    // el Constructors
    public Warehouse() {}

    public Warehouse(String name, user organization) {
        this.name = name;
        this.organization = organization;
    }

    // el Helpers
    public WarehouseItem findItem(String itemName) {
        return items.stream()
                .filter(i -> i.getItemName().equalsIgnoreCase(itemName))
                .findFirst()
                .orElse(null);
    }

    // el getters w setters

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public user getOrganization() { return organization; }
    public void setOrganization(user organization) { this.organization = organization; }

    public List<WarehouseItem> getItems() { return items; }
}