package kr.krd.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import kr.krd.vo.USH_RoleAppVO;
import kr.util.DBUtil;

public class USH_RoleApplicationDAO {
	
	//PENDING(대기) 권한 신청 목록 조회
	public List<USH_RoleAppVO> selectPending() {
		List<USH_RoleAppVO> list = new ArrayList<>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		
		try {
			conn = DBUtil.getConnection();
			sql = "SELECT role_app_id, role_app_user_id, role_app_role_cd, role_app_applied_at, role_app_apply_reason "
					+ "FROM role_application WHERE role_app_status = 'PENDING' ORDER BY role_app_applied_at DESC";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				list.add(new USH_RoleAppVO(rs.getInt("role_app_id"),
										   rs.getString("role_app_user_id"),
										   rs.getString("role_app_role_cd"),
										   rs.getDate("role_app_applied_at"),
										   rs.getString("role_app_apply_reason")));
			}
			return list;
			
		}catch (Exception e) {
			e.printStackTrace();
			return list;
		}finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}
	
	public int approve(int roleAppId, String adminId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		String sql2 = null;
		String sql3 = null;
		
		try {
			conn = DBUtil.getConnection();
			conn.setAutoCommit(false);
			
			//1. ROLE_APPLICATION 승인(PENDING 건만)
			sql = "UPDATE role_application SET role_app_status = 'APPROVED', role_approved_at = SYSDATE, role_approved_by = ?,"
					+ " role_reject_reason=NULL WHERE role_app_id = ? AND role_app_status = 'PENDING'";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, adminId);
			pstmt.setInt(2, roleAppId);
			int updated = pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;
			
			if(updated == 0) { //이미 처리됐거나 없는 ID
				conn.rollback();
				return 0;
			}
			
			//2. 신청서에서 (userId, roleCd) 읽기
			sql2 = "SELECT role_app_user_id, role_app_role_cd FROM role_application WHERE role_app_id = ?";
			pstmt = conn.prepareStatement(sql2);
			pstmt.setInt(1, roleAppId);
			rs = pstmt.executeQuery();
			
			if(!rs.next()) {
				conn.rollback();
				return 0;
			}
			
			String userId = rs.getString("role_app_user_id");
			String roleCd = rs.getString("role_app_role_cd");
			rs.close();
			pstmt.close();
			
			//3. USERINFO 권한 변경
			sql3 = "UPDATE userinfo SET user_role_cd = ? WHERE user_id = ?";
			pstmt = conn.prepareStatement(sql3);
			pstmt.setString(1, roleCd);
			pstmt.setString(2, userId);
			int u = pstmt.executeUpdate();
			pstmt.close();
			pstmt = null;
			
			if(u == 0) {
				conn.rollback();
				return 0;
			}
			
			conn.commit();
			return 1;
			
		}catch (Exception e) {
			try {if(conn != null) conn.rollback();}catch(Exception ignore) {}
			e.printStackTrace();
			return 0;
		}finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}
	
	public int reject(int roleAppId, String adminId, String reason) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		
		try {
			conn = DBUtil.getConnection();
			sql = "UPDATE role_application SET role_app_status = 'REJECTED', role_reject_reason = ?, role_approved_at = NULL, "
					+ "role_approved_by = NULL WHERE role_app_id = ? AND role_app_status = 'PENDING'";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, reason);
			pstmt.setInt(2, roleAppId);
			return pstmt.executeUpdate(); // 1 : 성공 / 0 : 이미 처리 or 없음
		}catch (Exception e) {
			e.printStackTrace();
			return 0;
		}finally {
			DBUtil.executeClose(null, pstmt, conn);
		}
	}
}
