package com.app.server;

import java.util.ArrayList;

public class Group {
	private int group_id;
	private ArrayList<Integer> members;

	public Group(int id, ArrayList<Integer> members) {
		this.group_id = id;
		this.members = members;
	}

	public int getGroupID() {
		return this.group_id;
	}

	public ArrayList<Integer> getGroupMembers() {
		return members;
	}
}
