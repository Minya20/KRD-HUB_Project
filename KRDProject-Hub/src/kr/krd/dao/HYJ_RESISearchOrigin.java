package kr.krd.dao;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import kr.util.DBUtil;

public class HYJ_RESISearchOrigin {

	public void RESISearch() {
		while(true) {
			Connection conn = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			String sql = null;

			try {
				BufferedReader br =
						new BufferedReader(new InputStreamReader(System.in));
				conn = DBUtil.getConnection();

				sql = "SELECT user_id, user_name, user_birth_dt, user_field FROM USERINFO";

				pstmt = conn.prepareStatement(sql);

				rs = pstmt.executeQuery();

				if(rs.next()) {
					System.out.println("아이디\t이름\t\t생년월일\t\t분야");
					do {
						System.out.print(rs.getString("user_id") + "\t");
						System.out.print(rs.getString("user_name") + "\t" + "\t");
						System.out.print(rs.getString("user_birth_dt") + "\t");
						System.out.print(rs.getString("user_field") + "\n");
					}
					while(rs.next());

					int sel;
					while(true) {
						System.out.print("1.상세보기 2.이전화면");
						sel = Integer.parseInt(br.readLine());
						if(sel > 2 || sel < 1) {
							System.out.println("1 혹은 2를 입력하세요.");
							continue;
						}
						break;
					}
					if(sel == 1) {
						System.out.print("자세히 볼 아이디를 입력해주세요.");
						/*아래 메서드 활용 부분*/
						String user_id = br.readLine();
						RESISearchDetail(user_id);
					}else if(sel == 2) {
						return;
					}
				}else {
					System.out.println("정보가 없습니다.");
				}
			}catch(NumberFormatException e) {
				System.out.println("숫자를 입력하세요");
			}
			catch(Exception e) {
				e.printStackTrace();
			}finally {
				DBUtil.executeClose(rs, pstmt, conn);
			}
		}
	}



	private void RESISearchDetail(String user_Id) {

		while (true) {

			Connection conn = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;

			try {

				BufferedReader br =
						new BufferedReader(new InputStreamReader(System.in));
				conn = DBUtil.getConnection();
				//여기서부터 수정해야
				String sql = "SELECT USER_ID, USER_NAME, USER_EMAIL, USER_BIRTH_DT, USER_COUNTRY_CD, USER_AFFILIATION, USER_FIELD "
						+ "FROM USERINFO "
						+ "WHERE USER_ID = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, user_Id);
				rs = pstmt.executeQuery();

				if (!rs.next()) {
					System.out.println("해당 인원이 존재하지 않습니다.");
					return;
				}

				System.out.println("-".repeat(30));
				System.out.println("아이디 : " + rs.getString("USER_ID"));
				System.out.println("이름 : " + rs.getString("USER_NAME"));
				System.out.println("이메일 : " + rs.getString("USER_EMAIL"));
				System.out.println("생년월일 : " + rs.getString("USER_BIRTH_DT"));
				System.out.println("국적 : " + rs.getString("USER_COUNTRY_CD"));
				System.out.println("소속 : " + rs.getString("USER_AFFILIATION"));
				System.out.println("분야 : " + rs.getString("USER_FIELD"));
				System.out.println("-".repeat(30));
				System.out.print("1.이전 화면");
				String sel = br.readLine();
				if(sel.equals("1")) {
					return;
				}else {
					System.out.println("잘못 입력하셨습니다.");
				}

			}catch (NumberFormatException e) {
				e.printStackTrace();
			}
			catch (Exception e) {
				e.printStackTrace();
			} finally {
				DBUtil.executeClose(rs, pstmt, conn);
			}
		}
	}
}
