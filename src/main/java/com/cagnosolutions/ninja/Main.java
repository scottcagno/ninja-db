package com.cagnosolutions.ninja;

import com.cagnosolutions.ninja.database.test.MergeObjects;
import com.cagnosolutions.ninja.database.test.Person;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class Main {

	public static void main(String[] args) throws Exception {

		Person p1 = new Person(1, "Scott Cagno", "scottiecagno@gmail.com", false);
		Person p2 = new Person(1, null, "scottiecagno@gmail.com", true); // active is updated, name is null

		MergeObjects mergeService = new MergeObjects();
		Person newPerson = (Person) mergeService.merge(p1, p2);
		System.out.println(newPerson);

		//Person newPerson = (Person) mergeService.MapToObject(p1m, new Person());
		//System.out.println(newPerson);
	}
}
