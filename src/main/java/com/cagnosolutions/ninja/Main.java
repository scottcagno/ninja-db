package com.cagnosolutions.ninja;

import com.cagnosolutions.ninja.test.A;
import com.cagnosolutions.ninja.test.C;

/**
 * Created by Scott Cagno.
 * Copyright Cagno Solutions. All rights reserved.
 */

public class Main {

	//GsonBuilder().setPrettyPrinting().create();

	public static void main(String[] args) throws Exception {

		A a = new A();
		a.createB("users");
		a.insertC("users", new C("user-1"));
		a.insertC("users", new C("user-2"));
		a.insertC("users", new C("user-3"));
		System.out.printf("%s", a.getMapE());

	}

}
