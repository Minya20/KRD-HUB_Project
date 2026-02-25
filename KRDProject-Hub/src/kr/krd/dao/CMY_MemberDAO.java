package kr.krd.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import kr.util.DBUtil;



public class CMY_MemberDAO {
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	
	//로그인 메서드
	public String userLogin(String user_id, String user_pw) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		ResultSet rs = null;
		String real_id = null;
		
		try {
			conn = DBUtil.getConnection();
			sql = "SELECT * FROM USERINFO WHERE USER_ID = ? AND USER_PWD = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, user_id);
			pstmt.setString(2, user_pw);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				String user_name = rs.getString("USER_NAME");
				System.out.println(user_name + "님 환영합니다.");
				real_id = rs.getString("user_id");	//일치하면 real_id에 유저 아이디를 넣는다
				
			}else {
				System.out.println("아이디 혹은 비밀번호가 일치 하지 않습니다.");
				real_id = "0";	//틀리면 "0"을 넣는다.
				
			}
		}
		catch(Exception e){e.printStackTrace();}
		finally {DBUtil.executeClose(rs, pstmt, conn);}
		return real_id;
	}
	
	//사용자의 권한을 반환하는 메서드
	public String getUserRole(String user_id) {
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		ResultSet rs = null;
		String real_role = null;
		
		try {
			conn = DBUtil.getConnection();
			sql = "SELECT user_role_cd FROM USERINFO WHERE USER_ID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, user_id);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				real_role = rs.getString("user_role_cd");	//일치하면 real_id에 유저 아이디를 넣는다
				System.out.println("권한 디버그용 : "+real_role);
			}
		}
		catch(Exception e){e.printStackTrace();}
		finally {DBUtil.executeClose(rs, pstmt, conn);}
		return real_role;
	}
	
	
	//로그아웃 메서드
	
	
	//평가목록 화면 메서드
	public void callReviewerMenu() {
		System.out.println("┌────────────────────────────────────────────────────────┐");
		System.out.println("│							 │");
		System.out.println("│	국가 연구과제 관리 프로그램	「KRD Hubs」		 │");
		System.out.println("│							 │");
		System.out.println("│	1. 평가배정목록조회					 │");
		System.out.println("│	2. 평가기록조회					 │");
		System.out.println("│	3. 내정보						 │");
		System.out.println("│	4. 로그아웃					 │");
		System.out.println("│	5. 종료						 │");
		System.out.println("│							 │");
		System.out.println("│등급 : 평가위원					ver.1.0	 │");
		System.out.println("└────────────────────────────────────────────────────────┘");
		System.out.println("［원하시는 메뉴를 선택하세요 ]");
		System.out.print(">>");
		try {
			int rev_choose = Integer.parseInt(br.readLine());
			if(rev_choose == 1) {
				//평가배정목록조회
			}else if(rev_choose == 2) {
				//평가기록조회
			}else if(rev_choose == 3) {
				//내정보
			}else if(rev_choose == 4) {
				//로그아웃
			}else if(rev_choose == 5) {
				System.out.println("프로그램 종료");
				return;
			}
		}
		catch(Exception e) {e.printStackTrace();}
		finally {if(br != null)try{br.close();}catch(IOException e) {}}
		
	}
}
