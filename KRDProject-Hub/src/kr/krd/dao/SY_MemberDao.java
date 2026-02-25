package kr.krd.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import kr.util.DBUtil;
import java.sql.Date;

public class SY_MemberDao {
	
	private BufferedReader br;
//회원가입(회원유형 선택을 곁들인)
	public void insertMember(String user_id, String user_pwd, String user_name, String user_email, String user_birth_dt, String user_phone_no,
			String user_country_cd, String user_addr, String user_gender_cd, Date user_create_at, Date user_last_login_at, String user_penalty_end_dt,
		 String user_affiliation, String user_field, Date user_update_at) {
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;

		try {
			//회원유형 선택
			System.out.println("어떤 회원으로 가입하시겠습니까? 1.개인연구자 2.단체연구자 3.기관담당자 4.평가위원");
	        br = new BufferedReader(new InputStreamReader(System.in));
	        int no = Integer.parseInt(br.readLine());
	        
			//JDBC 수행 1,2단계
			conn = DBUtil.getConnection();
			//SQL 문 작성
				sql = "INSERT INTO USERINFO (user_id, user_pwd, user_name, user_email, user_birth_dt, user_phone_no, user_country_cd, user_addr, " +
						"user_gender_cd, user_create_at, user_last_login_at, user_penalty_end_dt, user_role_cd, user_affiliation, user_field, " +
						"user_update_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
						
			
			//JDBC 수행 3단계
			pstmt = conn.prepareStatement(sql);
			//?에 데이터 바인딩
			pstmt.setString(1, user_id);
			pstmt.setString(2, user_pwd);
			pstmt.setString(3, user_name);
			pstmt.setString(4, user_email);
			pstmt.setString(5, user_birth_dt);
			pstmt.setString(6, user_phone_no);
			pstmt.setString(7, user_country_cd);
			pstmt.setString(8, user_addr);
			pstmt.setString(9, user_gender_cd);
			pstmt.setDate(10, user_create_at);
			pstmt.setDate(11, user_last_login_at);
			pstmt.setString(12, user_penalty_end_dt);
			//선택한 회원 유형에 따라 다르게 입력
			if(no==1) {pstmt.setString(13, "RESI");}
			else if(no==2) {pstmt.setString(13, "RESO");}
			else if(no==3) {pstmt.setString(13, "AGY");}
			else if(no==4) {pstmt.setString(13, "REV");}
			pstmt.setString(14, user_affiliation);
			pstmt.setString(15, user_field);
			pstmt.setDate(16, user_update_at);

			//JDBC 수행 4단계
			int count = pstmt.executeUpdate();
			System.out.println(count+"개의 회원 정보를 저장했습니다.");	

		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			DBUtil.executeClose(null, pstmt, conn);
		}
	}


	
}
