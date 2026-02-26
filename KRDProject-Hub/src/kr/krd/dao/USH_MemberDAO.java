package kr.krd.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import kr.util.DBUtil;

public class USH_MemberDAO {
	//시스템 관리자 회원 목록
	
	//selectUsers 기능
	//DB 연결/DELETED계정을 제외하고 회원 목록 조회/조회 결과를 표 형태로 콘솔 출력
	public void selectUsers() {
		//자원 변수 준비
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		try {
			//JDBC 수행 1,2단계 DB연결 (여기서 실패하면 DB 접속 설정 문제)
			conn = DBUtil.getConnection();
			//SQL문 작성 : DELETED 제외 + 역할/ID 정렬
			//status 컬럼 NULL 허용이므로 OR IS NULL 포함
			sql = "SELECT user_id,user_name,user_birth_dt,user_email,user_role_cd,user_acct_status_cd,"
					+ "user_created_at,user_last_login_at FROM userInfo "
					+ "WHERE user_acct_status_cd <> ? OR user_acct_status_cd IS NULL "
					+ "ORDER BY user_role_cd DESC, user_id";
			//JDBC 수행 3단계 PreparedStatment 바인딩
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "DELETED");
			//JDBC 수행 4단계 실행 후 결과 받기
			rs = pstmt.executeQuery();
			System.out.println("=======================================회원 목록 조회=======================================");
			System.out.println();
			System.out.println("[기본 조회 : DELETED 제외]");
			System.out.println();
			
			//출력 로직 : if(rs.next()) 패턴
			//rs.next()는 다음 행으로 이동이면서 행이 있는지도 알려줌.
			//그래서 첫 행이 있는지 확인하고, 있으면 do-while로 첫 행부터 출력하는 패턴.
			if(rs.next()) {
				//헤더 출력
				System.out.printf("%-12s %-8s %-12s %-24s %-10s %-12s %-19s %-19s%n","ID", "이름", "생년월일", "이메일", "권한", "상태", "가입일자", "마지막접속");
				System.out.println("-".repeat(130));
				//do-while로 첫 행 포함 전체 출력
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
				//결과 없음 출력
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
	
	//nvl() -> DB에서 NULL로 넘어오는 값은 출력하면 null로 보이기 때문에 보기 안 좋음.
	//		   그래서 NULL/빈 문자열이면 -로 통일해서 출력.
	private String nvl(String s) {
	    return (s == null || s.isBlank()) ? "-" : s;
	}

	//fmtTs() -> getTimestamp()로 가져오면 날짜+시간까지 있음
	//			 Timestamp.toString()도 되지만, LocalDateTime으로 바꿔서 보기 좋게 만드는 방식. NULL이면 -.
	private String fmtTs(java.sql.Timestamp ts) {
	    if (ts == null) return "-";
	    return ts.toLocalDateTime().toString().replace('T', ' '); // 2026-02-25 14:03:21
	}
	
	//회원 정보 상세 조회
	//입력받은 userId로 해당 회원 1명을 DB에서 조회
	//조회 결과가 있으면 상세 정보를 줄줄이 출력
	//없으면 오류메시지 출력
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
			
			System.out.println();
			
			if(!rs.next()) {
				System.out.println("해당 ID의 회원이 없습니다: " + userId);
				return;
			}
			
			System.out.println("===================회원 상세 조회===================");
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
					+ " WHERE (user_acct_status_cd <> ? OR user_acct_status_cd IS NULL)");
			
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
			
			System.out.println("===============회원 조건 검색 결과===============");
			if(rs.next()) {
				System.out.printf("%-12s %-8s %-12s %-24s %-10s %-12s %-19s %-19s%n", "ID", "이름", "생년월일", "이메일", "권한", "상태", "가입일자", "마지막접속");
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
				}while (rs.next());
			}else {
				System.out.println("조건에 맞는 회원이 없습니다.");
			}
			System.out.println("-".repeat(130));
			
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
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
	
}
