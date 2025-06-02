package com.bmt.webapp.models;

import jakarta.persistence.Entity;

import java.util.Date;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="invoices")
@Data
public class Invoice {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	
	private String fileName;
	private String storageFileName;
	
	private Date createdAt;

	@ManyToOne
	private Client client;

}
