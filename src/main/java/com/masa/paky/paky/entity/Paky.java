package com.masa.paky.paky.entity;

import io.micronaut.core.annotation.Introspected;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@NoArgsConstructor
@EqualsAndHashCode(of = "pakyId")
@Getter
@Setter
@Introspected
@Entity
@Table(name = "PAKY")
public class Paky {
    @Id
    private String idPaky;
    private float quantita;
    private float quantitaPct;
    private String topic;
    private String vendorId;
    private PakyStatus step;
    private String productTypeId;
    private Date dateCreated;
    private String customerId;
    private Date lastAction;
    private  Date packingDate;
    private String unit;
    private String brand;
    private Date expiration;
    private String label;
    private TraciabilityStatus traciabilityStatus;
    private int errorCode;

}
