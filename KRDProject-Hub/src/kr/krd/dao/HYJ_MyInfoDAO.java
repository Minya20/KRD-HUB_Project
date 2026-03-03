package kr.krd.dao;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import kr.util.DBUtil;

public class HYJ_MyInfoDAO {

    private BufferedReader br =
            new BufferedReader(new InputStreamReader(System.in));

    /* =====================
       내 정보 조회
       ===================== */
    public void SelectInfo(String cust_id) {

        while (true) {

            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;

            String sql =
                    "SELECT * FROM USERINFO WHERE user_id = ?";

            try {
                conn = DBUtil.getConnection();
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, cust_id);

                rs = pstmt.executeQuery();

                if (rs.next()) {
                    System.out.println("이름\t이메일\t주소\t분야");

                    System.out.print(rs.getString("USER_NAME") + "\t");
                    System.out.print(rs.getString("user_email") + "\t");
                    System.out.print(rs.getString("user_addr") + "\t");
                    System.out.print(rs.getString("user_field") + "\n");
                }

                System.out.print("1. 업데이트  2. 이전화면");
                int sel = Integer.parseInt(br.readLine());

                if (sel == 1) {

                    System.out.print("이름 : ");
                    String name = br.readLine();

                    System.out.print("이메일 : ");
                    String email = br.readLine();

                    System.out.print("주소 : ");
                    String addr = br.readLine();

                    System.out.print("분야 : ");
                    String field = br.readLine();

                    InfoUpdate(name, email, addr, field, cust_id);

                } else if (sel == 2) {
                    return;
                } else {
                    System.out.println("잘못 입력했습니다.");
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                DBUtil.executeClose(rs, pstmt, conn);
            }
        }
    }

    /* =====================
       정보 수정
       ===================== */
    public void InfoUpdate(String user_name,
                           String user_email,
                           String user_addr,
                           String user_field,
                           String user_id) {

        Connection conn = null;
        PreparedStatement pstmt = null;

        String sql =
                "UPDATE userinfo " +
                "SET user_name=?, user_email=?, user_addr=?, user_field=? " +
                "WHERE user_id=?";

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);

            int cnt = 0;
            pstmt.setString(++cnt, user_name);
            pstmt.setString(++cnt, user_email);
            pstmt.setString(++cnt, user_addr);
            pstmt.setString(++cnt, user_field);
            pstmt.setString(++cnt, user_id);

            int count = pstmt.executeUpdate();

            if (count > 0) {
                System.out.println("수정 완료!");
            } else {
                System.out.println("수정 실패");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(null, pstmt, conn);
        }
    }
}