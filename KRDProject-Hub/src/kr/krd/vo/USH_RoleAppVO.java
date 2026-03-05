package kr.krd.vo;

import java.util.Date;

public class USH_RoleAppVO {
	public final int roleAppId;
	public final String roleAppUserId;
	public final String roleAppRoleCd;
	public final Date roleAppAppliedAt;
	public final String roleAppApplyReason;
	
	public USH_RoleAppVO(int roleAppId, String roleAppUserId, String roleAppRoleCd, Date roleAppAppliedAt,
						 String roleAppApplyReason) {
		this.roleAppId = roleAppId;
		this.roleAppUserId = roleAppUserId;
		this.roleAppRoleCd = roleAppRoleCd;
		this.roleAppAppliedAt = roleAppAppliedAt;
		this.roleAppApplyReason = roleAppApplyReason;
	}
}
