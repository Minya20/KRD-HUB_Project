package kr.krd.dao;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import kr.util.DBUtil;

public class HYJ_APPLICATIONCheakDAOOrigin {

    private BufferedReader br =
            new BufferedReader(new InputStreamReader(System.in));

    /* =========================
       내 신청 목록 조회
       ========================= */
    public void CheckMyApp(String cust_id) {

        while (true) {

            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;

            String sql =
                    "SELECT a.APPLICATION_ID, " +
                    "       a2.ANNOUNCEMENT_TITLE, " +
                    "       a.APPLICATION_STATUS_CD " +
                    "FROM APPLICATIONS a " +
                    "LEFT JOIN ANNOUNCEMENT a2 " +
                    "ON a.APPLICATION_ANN_ID = a2.ANNOUNCEMENT_ANN_ID " +
                    "WHERE a.APPLICATION_USER_ID = ?";

            try {
                conn = DBUtil.getConnection();
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, cust_id);

                rs = pstmt.executeQuery();

                System.out.println("\n" + "-".repeat(40));

                if (!rs.next()) {
                    System.out.println("신청 내역이 없습니다.");
                    return;
                }

                System.out.println("신청번호\t공고명\t\t상태");

                do {
                    System.out.print(rs.getInt("APPLICATION_ID") + "\t");
                    System.out.print(rs.getString("ANNOUNCEMENT_TITLE") + "\t");
                    System.out.print(rs.getString("APPLICATION_STATUS_CD") + "\n");
                } while (rs.next());

                System.out.println("-".repeat(40));
                System.out.print("1. 상세조회  2. 이전화면 : ");

                int sel = Integer.parseInt(br.readLine());

                if (sel == 1) {
                	while(true) {
                		try {
                			System.out.print("조회할 신청번호 입력 : ");
                            int appId = Integer.parseInt(br.readLine());
                            //아래 메서드를 활용하는 지점 
                            detailApp(appId);
                            break;
                		}catch(NumberFormatException e) {
                			System.out.println("신청번호를 입력하세요.");
                		}
                	}
                    
                } else if (sel == 2) {
                    return;
                } else {
                    System.out.println("잘못된 입력입니다.");
                }

            }catch(NumberFormatException e) {
    			System.out.println("신청번호를 입력하세요.");
    		}
            catch (Exception e) {
                e.printStackTrace();
            } finally {
                DBUtil.executeClose(rs, pstmt, conn);
            }
        }
    }

    /* =========================
       신청 상세 조회
       ========================= */
    private void detailApp(int appId) {

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String sql =
                "SELECT a.APPLICATION_ID, " +
                "       a.APPLICATION_ANN_ID, " +
                "       a2.ANNOUNCEMENT_TITLE, " +
                "       a.APPLICATION_USER_ID, " +
                "       a.APPLICATION_ATTACH_PATH, " +
                "       a.APPLICATION_STATUS_CD, " +
                "       a.APPLICATION_BUDGET_AMT " +
                "FROM APPLICATIONS a " +
                "LEFT JOIN ANNOUNCEMENT a2 " +
                "ON a.APPLICATION_ANN_ID = a2.ANNOUNCEMENT_ANN_ID " +
                "WHERE a.APPLICATION_ID = ?";

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, appId);

            rs = pstmt.executeQuery();

            System.out.println("\n" + "-".repeat(50));

            if (!rs.next()) {
                System.out.println("신청 정보가 없습니다.");
                return;
            }

            System.out.println(
                    "신청번호\t공고번호\t공고명\t신청자\t첨부파일\t상태\t예산");

            do {
                System.out.print(rs.getInt("APPLICATION_ID") + "\t");
                System.out.print(rs.getInt("APPLICATION_ANN_ID") + "\t");
                System.out.print(rs.getString("ANNOUNCEMENT_TITLE") + "\t");
                System.out.print(rs.getString("APPLICATION_USER_ID") + "\t");
                System.out.print(rs.getString("APPLICATION_ATTACH_PATH") + "\t");
                System.out.print(rs.getString("APPLICATION_STATUS_CD") + "\t");
                System.out.print(rs.getInt("APPLICATION_BUDGET_AMT") + "\n");
            } while (rs.next());

            System.out.println("-".repeat(50));
            System.out.print("아무 키를 누르면 이전화면으로 나가집니다.");
             br.readLine();
             
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }
}