package org.klausoncloud.viruswar.resources;

public enum Impact {
	MISS("miss"), HIT("hit"), SPAWN("enter"), LEAVE("exit"), LOSE("lose"), WIN("win"), END("end"), PASS("pass");

	public final String impactString;

	Impact(String s) {
		this.impactString = s;
	}

	public String getImpactString() {
		return impactString;
	}
}
