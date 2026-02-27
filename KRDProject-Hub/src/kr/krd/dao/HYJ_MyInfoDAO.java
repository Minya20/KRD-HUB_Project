package kr.krd.dao;

import java.security.interfaces.RSAKey;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import kr.util.DBUtil;

public class HYJ_MyInfoDAO {
	public void SelectInfo (String cust_id) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;

		try {
			conn = DBUtil.getConnection();

			sql = "SELECT * FROM USERINFO WHERE user_id = ?";

			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, cust_id);

			rs = pstmt.executeQuery();

			if(rs.next()) {
				System.out.println("이름\t이메일\t\t생년월일\t\t전화번호\t\t국적\t주소\t성별\t권한\t상태\t소속\t분야");
				System.out.print(rs.getString("USER_NAME") + "\t");
				System.out.print(rs.getString("user_email") + "\t");
				System.out.print(rs.getString("user_birth_dt") + "\t");
				System.out.print(rs.getString("user_phone_no") + "\t");
				System.out.print(rs.getString("user_country_cd") + "\t");
				System.out.print(rs.getString("user_addr") + "\t");
				System.out.print(rs.getString("user_gender_cd") + "\t");
				System.out.print(rs.getString("user_role_cd") + "\t");
				System.out.print(rs.getString("user_acct_status_cd") + "\t");
				System.out.print(rs.getString("user_affiliation") + "\t");
				System.out.print(rs.getString("user_field") + "\n");
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}

	public void InfoUpdate(String user_name, String user_email, String user_addr, String user_field, String user_id){
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		int cnt = 0;

		try {
			conn = DBUtil.getConnection();
			
			
			sql = "UPDATE userinfo SET user_name = ?, user_email = ?, user_addr = ?, user_field = ? WHERE user_id = ?";
			
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(++cnt, user_name);
			pstmt.setString(++cnt, user_email);
			pstmt.setString(++cnt, user_addr);
			pstmt.setString(++cnt, user_field);
			pstmt.setString(++cnt, user_id);
			
			int count = pstmt.executeUpdate();
			System.out.println("수정이 완료되었습니다");
			
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}



}





