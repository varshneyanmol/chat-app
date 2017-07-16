package com.app.client;

import java.util.ArrayList;

public class Test {

	public static void main(String[] args) {
		String demo = "/d/362/i/jatin:134/i/aditya:614 removed jatin:134";
		String[] arr = demo.split("/d/|/i/");
		System.out.println("Array size: " + arr.length);
		for (int i = 0; i < arr.length; i++) {
			System.out.println(arr[i]);
		}

	}
}
