package kr.krd.vo;

import java.sql.Timestamp;

public class USH_UserSummary {
	public final String userId;
	public final String userName;
	public final String birthDt;
	public final String email;
	public final String roleCd;
	public final String acctStatusCd;
	public final Timestamp createdAt;
	public final Timestamp lastLoginAt;
	
	public USH_UserSummary(String userId, String userName, String birthDt, String email, String roleCd, 
						   String acctStatusCd, Timestamp createdAt, Timestamp lastLoginAt) {
		this.userId = userId;
		this.userName = userName;
		this.birthDt = birthDt;
		this.email = email;
		this.roleCd = roleCd;
		this.acctStatusCd = acctStatusCd;
		this.createdAt = createdAt;
		this.lastLoginAt = lastLoginAt;
	}
}
