package com.entity;

/**
 * @author shiotsuki
 *
 */
public class MenuBeans
{
	int fase_id;
	int number;
	int authority_id;
	int permissions;
	int type;
	String name;
	String contents;

	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getFase_id() {
		return fase_id;
	}
	public void setFase_id(int fase_id) {
		this.fase_id = fase_id;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public int getAuthority_id() {
		return authority_id;
	}
	public void setAuthority_id(int authority_id) {
		this.authority_id = authority_id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getContents() {
		return contents;
	}
	public void setContents(String contents) {
		this.contents = contents;
	}
	public int getPermissions() {
		return permissions;
	}
	public void setPermissions(int permissions) {
		this.permissions = permissions;
	}
}
