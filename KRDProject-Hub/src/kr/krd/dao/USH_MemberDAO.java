package kr.krd.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import kr.util.DBUtil;

public class USH_MemberDAO {
	//시스템 관리자 회원 목록
	public void selectUsers() {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		try {
			//JDBC 수행 1,2단계
			conn = DBUtil.getConnection();
			//SQL문 작성
			sql = "SELECT user_id,user_name,user_birth_dt,user_email,user_role_cd,user_acct_status_cd,"
					+ "user_created_at,user_last_login_at FROM userInfo "
					+ "WHERE user_acct_status_cd <> ? OR user_acct_status_cd IS NULL "
					+ "ORDER BY user_role_cd DESC, user_id";
			//JDBC 수행 3단계
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "DELETED");
			//JDBC 수행 4단계
			rs = pstmt.executeQuery();
			System.out.println("=========회원 목록 조회=========");
			System.out.println("[기본 조회 : DELETED 제외]");
			if(rs.next()) {
				System.out.printf("%-12s %-8s %-12s %-24s %-10s %-12s %-19s %-19s%n","ID", "이름", "생년월일", "이메일", "권한", "상태", "가입일자", "마지막접속");
				System.out.println("-".repeat(130));
				do {
					System.out.printf("%-12s %-8s %-12s %-24s %-10s %-12s %-19s %-19s%n",
					        nvl(rs.getString("user_id")),
					        nvl(rs.getString("user_name")),
					        nvl(rs.getString("user_birth_dt")),
					        nvl(rs.getString("user_email")),
					        nvl(rs.getString("user_role_cd")),
					        nvl(rs.getString("user_acct_status_cd")),
					        fmtTs(rs.getTimestamp("user_created_at")),
					        fmtTs(rs.getTimestamp("user_last_login_at")));
				}while(rs.next());
			}else {
				System.out.println("등록된 회원 정보가 없습니다.");
			}
			System.out.println("-".repeat(130));
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			//자원정리
			DBUtil.executeClose(rs, pstmt, conn);
		}
		
	}
	private String nvl(String s) {
	    return (s == null || s.isBlank()) ? "-" : s;
	}

	private String fmtTs(java.sql.Timestamp ts) {
	    if (ts == null) return "-";
	    return ts.toLocalDateTime().toString().replace('T', ' '); // 2026-02-25 14:03:21
	}
	
	//회원 정보 상세 조회
	public void selectUserDetail(String userId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		
		try {
			//JDBC 수행 1,2단계
			conn = DBUtil.getConnection();
			//SQL문 작성
			sql = "SELECT user_id,user_name,user_birth_dt,user_email,user_phone_no,user_country_cd,"
					+ "user_addr,user_gender_cd,user_created_at,user_last_login_at,user_penalty_end_dt,"
					+ "user_role_cd,user_acct_status_cd,user_affiliation,user_field,user_update_at "
					+ "FROM userInfo WHERE user_id=?";
			//JDBC 수행 3단계
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			//JDBC 수행 4단계
			rs = pstmt.executeQuery();
			if(!rs.next()) {
				System.out.println("해당 ID의 회원이 없습니다: " + userId);
				return;
			}
			
			System.out.println("=========회원 상세 조회=========");
			System.out.println("ID : " + nvl(rs.getString("user_id")));
	        System.out.println("이름 : " + nvl(rs.getString("user_name")));
	        System.out.println("생년월일 : " + nvl(rs.getString("user_birth_dt")));
	        System.out.println("이메일 : " + nvl(rs.getString("user_email")));
	        System.out.println("전화번호 : " + nvl(rs.getString("user_phone_no")));
	        System.out.println("국적 : " + nvl(rs.getString("user_country_cd")));
	        System.out.println("주소 : " + nvl(rs.getString("user_addr")));
	        System.out.println("성별 : " + nvl(rs.getString("user_gender_cd")));
	        System.out.println("권한 : " + nvl(rs.getString("user_role_cd")));
	        System.out.println("계정 상태 : " + nvl(rs.getString("user_acct_status_cd")));
	        System.out.println("가입일자 : " + fmtTs(rs.getTimestamp("user_created_at")));
	        System.out.println("마지막 접속 : " + fmtTs(rs.getTimestamp("user_last_login_at")));
	        System.out.println("패널티 종료일 : " + nvl(rs.getString("user_penalty_end_dt")));
	        System.out.println("소속 : " + nvl(rs.getString("user_affiliation")));
	        System.out.println("담당 분야 : " + nvl(rs.getString("user_field")));
	        System.out.println("업데이트 일시 : " + fmtTs(rs.getTimestamp("user_update_at")));
	        System.out.println("-".repeat(50));

		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}
	
	//회원 조건 검색
	public void searchUsers(String id, String name, String email, String role, String status,
							String regStart, String regEnd, String lastStart, String lastEnd) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			//JDBC 수행 1,2단계
			conn = DBUtil.getConnection();
			//SQL문 작성
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT user_id, user_name, user_birth_dt, user_email, user_role_cd, user_acct_status_cd, "
					+ "user_created_at, user_last_login_at FROM userInfo"
					+ "WHERE (user_acct_status_cd <> ? OR user_acct_status_cd IS NULL)");
			
			//파라미터를 순서대로 쌓기
			java.util.List<Object> params = new java.util.ArrayList<>();
			params.add("DELETED");
			
			if(!isEmpty(id)) {
				sql.append(" AND user_id LIKE ? ");
				params.add("%" + id + "%");
			}
			if(!isEmpty(name)) {
				sql.append(" AND user_name Like ? ");
				params.add("%" + name + "%");
			}
			if(!isEmpty(email)) {
				sql.append(" AND user_email Like ? ");
				params.add("%" + email + "%");
			}
			if(!isEmpty(role)) {
				sql.append(" AND user_role_cd = ? ");
				params.add(role);
			}
			if(!isEmpty(status)) {
				sql.append(" AND user_acct_status_cd = ? ");
				params.add(status);
			}
			
			//날짜 범위(일 단위 비교) : TRUNC로 시간 제거
			if(!isEmpty(regStart)) {
				sql.append(" AND TRUNC(user_created_at) >= TO_DATE(?, 'YYYY-MM-DD') ");
				params.add(regStart);
			}
			if(!isEmpty(regEnd)) {
				sql.append(" AND TRUNC(user_created_at) <= TO_DATE(?, 'YYYY-MM-DD') ");
				params.add(regEnd);
			}
			if(!isEmpty(lastStart)) {
				sql.append(" AND TRUNC(user_last_login_at) >= TO_DATE(?, 'YYYY-MM-DD') ");
				params.add(lastStart);
			}
			if(!isEmpty(lastEnd)) {
				sql.append(" AND TRUNC(user_last_login_at) <= TO_DATE(?, 'YYYY-MM-DD') ");
				params.add(lastEnd);
			}
			
			sql.append(" ORDER BY user_role_cd DESC, user_id");
			
			pstmt = conn.prepareStatement(sql.toString());
			
			for(int i=0;i<params.size();i++) {
				pstmt.setObject(i + 1, params.get(i));
			}
			
			rs = pstmt.executeQuery();
			
			System.out.println("");
			
			
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}
	
	private boolean isEmpty(String s) {
		return s == null || s.trim().isEmpty();
	}
	
}
