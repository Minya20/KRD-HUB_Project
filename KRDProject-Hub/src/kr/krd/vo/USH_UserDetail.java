package kr.krd.vo;

import java.sql.Timestamp;

public class USH_UserDetail {
	public final String userId;
	public final String userName;
	public final String birthDt;
	public final String email;
	public final String phoneNo;
	public final String countryCd;
	public final String addr;
	public final String genderCd;
	public final String roleCd;
	public final String acctStatusCd;
	public final Timestamp createdAt;
	public final Timestamp lastLoginAt;
	public final String penaltyEndDt;
	public final String affiliation;
	public final String field;
	public final Timestamp updatedAt;
	
	public USH_UserDetail(String userId, String userName, String birthDt, String email, String phoneNo, String countryCd, String addr, String genderCd,
						  String roleCd, String acctStatusCd, Timestamp createdAt, Timestamp lastLoginAt, String penaltyEndDt, String affiliation,
						  String field, Timestamp updatedAt) {
		this.userId = userId;
		this.userName = userName;
		this.birthDt = birthDt;
		this.email = email;
		this.phoneNo = phoneNo;
		this.countryCd = countryCd;
		this.addr = addr;
		this.genderCd = genderCd;
		this.roleCd = roleCd;
		this.acctStatusCd = acctStatusCd;
		this.createdAt = createdAt;
		this.lastLoginAt = lastLoginAt;
		this.penaltyEndDt = penaltyEndDt;
		this.affiliation = affiliation;
		this.field = field;
		this.updatedAt = updatedAt;
		
	}
}
