package com.cagnosolutions.ninja.database.test;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class Person {

	private Integer id;
	private String name;
	private String email;
	private Boolean active;

	public Person() {

	}

	public Person(Integer id, String name, String email, Boolean active) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.active = active;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String toString() {
		return "Person{" +
				"id=" + id +
				", name='" + name + '\'' +
				", email='" + email + '\'' +
				", active=" + active +
				'}';
	}
}