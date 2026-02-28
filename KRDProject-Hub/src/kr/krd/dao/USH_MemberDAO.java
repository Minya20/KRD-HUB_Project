package kr.krd.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import kr.krd.vo.USH_UserSummary;
import kr.krd.vo.USH_UserDetail;
import kr.util.DBUtil;

public class USH_MemberDAO {
	//시스템 관리자 회원 목록

	//selectUsers 기능
	//DB 연결/DELETED계정을 제외하고 회원 목록 조회/조회 결과를 표 형태로 콘솔 출력
	public List<USH_UserSummary> findUserExcludeDeleted() {
		List<USH_UserSummary> list = new ArrayList<>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;

		try {
			conn = DBUtil.getConnection();
			sql = "SELECT user_id, user_name, user_birth_dt, user_email, user_role_cd, user_acct_status_cd, user_created_at, user_last_login_at "
					+ "FROM userInfo WHERE user_acct_status_cd <> ? OR user_acct_status_cd IS NULL ORDER BY user_role_cd DESC, user_id";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "DELETED");
			rs = pstmt.executeQuery();

			while(rs.next()) {
				list.add(new USH_UserSummary(rs.getString("user_id"),
						rs.getString("user_name"),
						rs.getString("user_birth_dt"),
						rs.getString("user_email"),
						rs.getString("user_role_cd"),
						rs.getString("user_acct_status_cd"),
						rs.getTimestamp("user_created_at"),
						rs.getTimestamp("user_last_login_at")
						));
			}
			return list;

		}catch (Exception e) {
			e.printStackTrace();
			return list;
		}finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}

	//회원 정보 상세 조회
	//입력받은 userId로 해당 회원 1명을 DB에서 조회
	public USH_UserDetail findUserDetail(String userId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;

		try {
			conn = DBUtil.getConnection();
			sql = "SELECT user_id, user_name, user_birth_dt, user_email, user_phone_no, user_country_cd, user_addr, user_gender_cd, "
					+ "user_role_cd, user_acct_status_cd, user_created_at, user_last_login_at, user_penalty_end_dt, user_affiliation, "
					+ "user_field, user_update_at FROM userInfo WHERE user_id = ? ";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			rs = pstmt.executeQuery();

			if (!rs.next()) return null;

			return new USH_UserDetail(
					rs.getString("user_id"),
					rs.getString("user_name"),
					rs.getString("user_birth_dt"),
					rs.getString("user_email"),
					rs.getString("user_phone_no"),
					rs.getString("user_country_cd"),
					rs.getString("user_addr"),
					rs.getString("user_gender_cd"),
					rs.getString("user_role_cd"),
					rs.getString("user_acct_status_cd"),
					rs.getTimestamp("user_created_at"),
					rs.getTimestamp("user_last_login_at"),
					rs.getString("user_penalty_end_dt"),
					rs.getString("user_affiliation"),
					rs.getString("user_field"),
					rs.getTimestamp("user_update_at")
					);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}

	//회원 조건 검색
	public List<USH_UserSummary> searchUsers( String id, String name, String email, String role, String status, String regStart, String regEnd, 
			String lastStart, String lastEnd ) { 
		List<USH_UserSummary> list = new ArrayList<>(); 
		Connection conn = null; 
		PreparedStatement pstmt = null; 
		ResultSet rs = null; 
		try { 
			conn = DBUtil.getConnection(); 
			
			if (role != null) role = role.trim().toUpperCase();
	        if (status != null) status = status.trim().toUpperCase();
	        
			StringBuilder sql = new StringBuilder(); 
			sql.append("SELECT user_id, user_name, user_birth_dt, user_email, user_role_cd, user_acct_status_cd, ") 
			.append(" user_created_at, user_last_login_at ") 
			.append("FROM userInfo ") 
			.append("WHERE 1=1 "); 
			
			List<Object> params = new ArrayList<>(); 
			if(!isEmpty(id)) { sql.append(" AND user_id LIKE ? "); params.add("%" + id + "%"); } 
			if(!isEmpty(name)) { sql.append(" AND user_name LIKE ? "); params.add("%" + name + "%"); } 
			if(!isEmpty(email)) { sql.append(" AND user_email LIKE ? "); params.add("%" + email + "%"); } 
			if(!isEmpty(role)) { sql.append(" AND user_role_cd = ? "); params.add(role); } 
			if(!isEmpty(status)) { sql.append(" AND user_acct_status_cd = ? "); params.add(status); } 
			if(!isEmpty(regStart)) { sql.append(" AND TRUNC(user_created_at) >= TO_DATE(?, 'YYYY-MM-DD') "); params.add(regStart); } 
			if(!isEmpty(regEnd)) { sql.append(" AND TRUNC(user_created_at) <= TO_DATE(?, 'YYYY-MM-DD') "); params.add(regEnd); } 
			if(!isEmpty(lastStart)) { sql.append(" AND TRUNC(user_last_login_at) >= TO_DATE(?, 'YYYY-MM-DD') "); params.add(lastStart); } 
			if(!isEmpty(lastEnd)) { sql.append(" AND TRUNC(user_last_login_at) <= TO_DATE(?, 'YYYY-MM-DD') "); params.add(lastEnd); } 
			
			sql.append(" ORDER BY user_role_cd DESC, user_id "); 
		
			pstmt = conn.prepareStatement(sql.toString()); 
			for(int i=0; i<params.size(); i++) { 
				pstmt.setObject(i + 1, params.get(i)); 
			} 
			
			rs = pstmt.executeQuery(); 
			while(rs.next()) { 
				list.add(new USH_UserSummary( rs.getString("user_id"), rs.getString("user_name"), rs.getString("user_birth_dt"), 
						rs.getString("user_email"), rs.getString("user_role_cd"), rs.getString("user_acct_status_cd"), 
						rs.getTimestamp("user_created_at"), rs.getTimestamp("user_last_login_at") )); 
			} 
			return list; 
		} catch (Exception e) { 
			e.printStackTrace(); 
			return list; 
		} finally { 
			DBUtil.executeClose(rs, pstmt, conn); 
		} 
	}

	private boolean isEmpty(String s) {
		return s == null || s.trim().isEmpty();
	}

	//삭제 가능 여부 판단
	public String canSoftDelete(String userId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;

		try {
			//JDBC 1,2단계 수행
			conn = DBUtil.getConnection();
			//SQL문 작성
			//삭제 가능 여부를 판단하기 위해 상태/권한 조회
			sql = "SELECT user_acct_status_cd, user_role_cd FROM userInfo WHERE user_id=?";
			//JDBC 3단계 수행
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId); //?자리에 바인딩
			//JDBC 4단계 수행
			rs = pstmt.executeQuery();

			//조회 결과가 없으면 존재하지 않는 회원
			if(!rs.next()) return "NOT_FOUND";

			//현재 계정 상태/권한
			String status = rs.getString("user_acct_status_cd");
			String role = rs.getString("user_role_cd");

			//이미 삭제 상태면 다시 삭제할 필요 없음
			if("DELETED".equals(status)) return "ALREADY_DELETED";

			//관리자 계정은 삭제 못하게 막기
			if("ADM".equals(role)) return "ADMIN_BLOCK";

			//위 조건에 걸리지 않으면 삭제 가능
			return "OK";

		}catch (Exception e) {
			e.printStackTrace();
			return "ERROR";
		}finally {
			//자원정리
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}

	//실제 삭제(논리삭제)수행
	public int softDeleteUser(String userId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;

		try {
			//JDBC 1,2단계
			conn = DBUtil.getConnection();
			//SQL문 작성
			//상태를 DELETED로 변경
			sql = "UPDATE userInfo SET user_acct_status_cd = 'DELETED' WHERE user_id = ?";
			//JDBC 3단계
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);

			//JDBC 4단계 executeUpdate() 결과: 1 = 1행 업데이트 성공/ 0 = 해당 ID가 없거나 조건에 맞는 행이 없음
			return pstmt.executeUpdate();


		}catch (Exception e) {
			e.printStackTrace();
			return 0;
		}finally {
			//자원정리
			DBUtil.executeClose(null, pstmt, conn);
		}
	}

	//회원 존재 여부 확인
	public boolean existsUser(String userId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;

		try {
			//JDBC 1,2단계
			conn = DBUtil.getConnection();
			sql = "SELECT user_id FROM userInfo WHERE user_id = ?";
			//JDBC 3단계
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			//JDBC 4단계
			rs = pstmt.executeQuery();

			return rs.next();


		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}finally {
			//자원정리
			DBUtil.executeClose(rs, pstmt, conn);
		}

	}

	//회원 상태 변경
	public int updateUserStatus(String userId, String status, String endDtOrNull) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;

		try {
			//JDBC 1,2단계
			conn = DBUtil.getConnection();
			//SQL문 작성
			sql = "UPDATE userInfo SET user_acct_status_cd = ?, user_penalty_end_dt = ? WHERE user_id = ?";
			//JDBC 3단계
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, status);
			pstmt.setString(2, endDtOrNull);
			pstmt.setString(3, userId);
			//JDBC 4단계
			return pstmt.executeUpdate();


		}catch (Exception e) {
			e.printStackTrace();
			return 0;
		}finally {
			DBUtil.executeClose(null, pstmt, conn);
		}
	}

	//회원 상태 변경 - 현재 상태 확인
	public String getAcctStatus(String userId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;

		try {
			//JDBC 1,2단계
			conn = DBUtil.getConnection();
			//SQL문 작성
			sql = "SELECT user_acct_status_cd FROM userInfo WHERE user_id=?";
			//JDBC 3단계
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			//JDBC 4단계
			rs = pstmt.executeQuery();

			if(!rs.next()) return null;
			return rs.getString("user_acct_status_cd");

		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}

	//회원 상태 변경 - 마지막 패널티 기간 확인
	public String getPenaltyEndDt(String userId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;

		try {
			//JDBC 1,2단계
			conn = DBUtil.getConnection();
			//SQL문 작성
			sql = "SELECT user_penalty_end_dt FROM userInfo WHERE user_id=?";
			//JDBC 3단계
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			//JDBC 4단계
			rs = pstmt.executeQuery();

			if(!rs.next()) return null;
			return rs.getString("user_penalty_end_dt");
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}

	//회원 상태 변경 - 패널티 기간 종료 후 자동 상태 변경&패널티 날짜 초기화
	public int restoreExpiredSuspendedUsers() {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;

		try {
			//JDBC 1,2단계
			conn = DBUtil.getConnection();
			//SQL문 작성
			sql = "UPDATE userInfo SET user_acct_status_cd = 'ACTIVE', user_penalty_end_dt = NULL WHERE user_acct_status_cd =? AND "
					+ "user_penalty_end_dt IS NOT NULL AND REGEXP_LIKE(user_penalty_end_dt, '^\\d{4}-\\d{2}-\\d{2}$') AND "
					+ "TO_DATE(user_penalty_end_dt, 'YYYY-MM-DD') <= TRUNC(SYSDATE)";
			//JDBC 3단계
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "SUSPENDED");
			//JDBC 4단계
			return pstmt.executeUpdate();

		}catch (Exception e) {
			e.printStackTrace();
			return 0;
		}finally {
			DBUtil.executeClose(null, pstmt, conn);
		}
	}

	//권한 변경 - 대상 사용자/상태 조회
	public String[] getRoleAndStatus(String userId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;

		try {
			conn = DBUtil.getConnection();
			sql = "SELECT user_role_cd, user_acct_status_cd FROM userInfo WHERE user_id =?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				String role = rs.getString("user_role_cd");
				String status = rs.getString("user_acct_status_cd");
				return new String[] {role, status};
			}else return null;

		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}

	public int changeUserRoleWithHistory(String userId, String newRole, String changedBy, String reason) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		String beforeRole = null;
		String status = null;

		try {
			conn = DBUtil.getConnection();
			conn.setAutoCommit(false); //트랜잭션 시작
			//현재 역할/상태 조회
			sql = "SELECT user_role_cd, user_acct_status_cd FROM userInfo WHERE user_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			rs = pstmt.executeQuery();

			if(!rs.next()) {
				conn.rollback();
				return 0; //사용자 없음
			}

			beforeRole = rs.getString("user_role_cd");
			status = rs.getString("user_acct_status_cd");

			//ACTIVE 상태인지 체크
			if(!(status == null || "ACTIVE".equalsIgnoreCase(status))) {
				conn.rollback();
				return 0;
			}

			//역할 업데이트
			//동일 역할이면 업데이트/이력 생략하고 e 반환해도 됨
			if(beforeRole != null && beforeRole.equalsIgnoreCase(newRole)) {
				conn.rollback();
				return 0;
			}

			//자원 정리 후 재사용
			DBUtil.executeClose(rs, pstmt, null);
			rs = null;
			pstmt = null;

			String updateSql = "UPDATE userInfo SET user_role_cd = ? WHERE user_id =?";
			pstmt = conn.prepareStatement(updateSql);
			pstmt.setString(1, newRole);
			pstmt.setString(2, userId);

			int updated = pstmt.executeUpdate();
			if(updated != 1) {
				conn.rollback();
				return 0;
			}

			DBUtil.executeClose(null, pstmt, null);
			pstmt = null;

			//이력 INSERT (시퀀스 사용)
			String insertSql = "INSERT INTO role_change_hist (role_change_hist_hist_id, role_change_hist_target_user_id,"
					+ " role_change_hist_before_role_cd, role_change_hist_after_role_cd, role_change_hist_changed_by,"
					+ " role_change_hist_changed_at, role_change_hist_reason) VALUES "
					+ "(seq_role_change_hist.NEXTVAL, ?, ?, ?, ?, SYSDATE, ?) ";
			pstmt = conn.prepareStatement(insertSql);
			pstmt.setString(1, userId);
			pstmt.setString(2, beforeRole);
			pstmt.setString(3, newRole);
			pstmt.setString(4, changedBy);
			pstmt.setString(5, reason); //null 가능

			int inserted = pstmt.executeUpdate();
			if(inserted != 1) {
				conn.rollback();
				return 0;
			}

			conn.commit();
			return 1;

		}catch (Exception e) {
			try {if (conn != null) conn.rollback();}catch (Exception ignore) {}
			e.printStackTrace();
			return 0;
		}finally {
			try {if (conn != null) conn.setAutoCommit(true);}catch (Exception ignore) {}
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}
}
