package com.bmt.webapp.models;

import java.util.Date;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "clients")
@Data
public class Client {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	
	private String firstName;
	private String lastName;
	
	@Column(unique = true, nullable = false)
	private String email;
	
	private String phone;
	private String address;
	private String status;
	private Date createdAt;
	
	@OneToMany(mappedBy="client", cascade = CascadeType.ALL)
	private List<Invoice> invoices;
}
