package kr.krd.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;

import kr.util.DBUtil;

public class SY_ApplicationDAO {

	//신청서 제출
	public int insertApplication(int annId, String userId, String attachPath, String statusCd, long budgetAmt) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		
		//applied_at, upadated_at은 기본값 sysdate 명시
		try {
			conn = DBUtil.getConnection();
			sql = "INSERT INTO applications (application_id, application_ann_id, application_user_id, application_applied_at, application_updated_at, "
					+ "application_attach_path, application_status_cd, application_budget_amt) VALUES (app_seq.nextval, ?, ?, SYSDATE, SYSDATE, ?, ?, ?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, annId);
			pstmt.setString(2, userId);
			pstmt.setString(3, attachPath);
			pstmt.setString(4, statusCd);
			pstmt.setLong(5, budgetAmt);
			
			return pstmt.executeUpdate(); //1이면 성공
		}catch (Exception e) {
			throw new RuntimeException("신청서 제출 실패", e);
		}finally {
			DBUtil.executeClose(null, pstmt, conn);
		}
	}
}
