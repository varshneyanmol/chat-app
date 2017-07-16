package com.app.server;

import java.util.ArrayList;
import java.util.Collections;

/**
 * getUniqueIdentifier() returns a unique random between [0, RANGE-1].
 */
public class UniqueIdentifier {
	private static ArrayList<Integer> client_ids = new ArrayList<Integer>();
	private static ArrayList<Integer> group_ids = new ArrayList<Integer>();
	private static final int RANGE = 1000;
	private static int cindex = 0;
	private static int gindex = 0;

	static {
		for (int i = 0; i < RANGE; i++) {
			client_ids.add(i);
			group_ids.add(i);
		}
		Collections.shuffle(client_ids);
		Collections.shuffle(group_ids);
	}

	private UniqueIdentifier() {
	}

	public static int getUniqueClientIdentifier() {
		if (cindex >= client_ids.size())
			cindex = 0;
		return client_ids.get(cindex++);
	}

	public static int getUniqueGroupIdentifier() {
		if (gindex >= group_ids.size())
			gindex = 0;
		return group_ids.get(gindex++);
	}
}
