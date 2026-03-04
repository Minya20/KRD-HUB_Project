package kr.krd.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import kr.util.DBUtil;

public class HYJ_KRDRESOUserDAO {
	

    private BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    
 
    // ===========================
    // 공고 목록
    // ===========================
    public void selectAnn() {

        while (true) {

            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;

            try {
                conn = DBUtil.getConnection();

                String sql = "SELECT * FROM ANNOUNCEMENT ORDER BY ANNOUNCEMENT_ANN_ID";
                pstmt = conn.prepareStatement(sql);
                rs = pstmt.executeQuery();

                System.out.println("\n" + "-".repeat(40));

                if (!rs.next()) {
                    System.out.println("등록된 공고가 없습니다.");
                    return;
                }
                
                System.out.println("번호\t\t공고명\t\t\t예산\t\t시작일\t\t종료일");

                do {
                    System.out.print(rs.getInt("ANNOUNCEMENT_ANN_ID") + "\t" + "\t");
                    System.out.print(rs.getString("ANNOUNCEMENT_TITLE") + "\t" + "\t");
                    System.out.printf("%,d\t", rs.getInt("ANNOUNCEMENT_TOTAL_BUDGET"));
                    System.out.print(rs.getString("ANNOUNCEMENT_START_DT") + "\t");
                    System.out.println(rs.getString("ANNOUNCEMENT_END_DT"));
                } while (rs.next());

                System.out.println("-".repeat(40));
                System.out.print("1. 상세조회  2. 이전화면 : ");
                int sel = Integer.parseInt(br.readLine());

                if (sel == 1) {
                    System.out.print("상세조회할 번호 입력 : ");
                    int annId = Integer.parseInt(br.readLine());
                    detailAnn(annId);   // 상세 메서드 분리
                } else if (sel == 2) {
                    return;   // 이전 화면으로 복귀
                } else {
                    System.out.println("잘못된 입력입니다.");
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                DBUtil.executeClose(rs, pstmt, conn);
            }
        }
    }

    // ===========================
    // 공고 상세보기
    // ===========================
    private void detailAnn(int annId) {

        while (true) {

            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;

            try {
                conn = DBUtil.getConnection();

                String sql = "SELECT * FROM ANNOUNCEMENT WHERE ANNOUNCEMENT_ANN_ID = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, annId);
                rs = pstmt.executeQuery();

                if (!rs.next()) {
                    System.out.println("해당 공고가 없습니다.");
                    return;
                }

                System.out.println("\n" + "-".repeat(60));
                System.out.println("공고번호 : " + rs.getInt("ANNOUNCEMENT_ANN_ID"));
                System.out.println("기관번호 : " + rs.getString("ANNOUNCEMENT_AGY_ID"));
                System.out.println("공고명 : " + rs.getString("ANNOUNCEMENT_TITLE"));
                System.out.println("재공고여부 : " + rs.getInt("ANNOUNCEMENT_REANN_YN"));
                System.out.println("담당자연락처 : " + rs.getString("ANNOUNCEMENT_PM_CONTACT"));
                System.out.println("모집인원 : " + rs.getInt("ANNOUNCEMENT_RECRUIT_CAP"));
                System.out.println("접수시작일 : " + rs.getString("ANNOUNCEMENT_START_DT"));
                System.out.println("접수종료일 : " + rs.getString("ANNOUNCEMENT_END_DT"));
                System.out.println("공고상태 : " + rs.getString("ANNOUNCEMENT_STATUS"));
                System.out.println("모집분야 : " + rs.getString("ANNOUNCEMENT_FIELD"));
                System.out.println("공고담당자 : " + rs.getString("ANNOUNCEMENT_CREATED_BY"));
                System.out.printf("총예산 : %,d\n", rs.getInt("ANNOUNCEMENT_TOTAL_BUDGET"));
                System.out.println("-".repeat(60));

                System.out.print("1. 신청하기  2. 목록으로 : ");
                int sel = Integer.parseInt(br.readLine());

                if (sel == 1) {
                	//신청서 작성
                	System.out.println("신청 하실 번호를 입력하세요");
                	int application_id = Integer.parseInt(br.readLine());
                	System.out.println("신청자 아이디를 입력하세요");
                	String application_user_id = br.readLine();
                	System.out.println("첨부 파일을 입력하세요.");
                	String application_attach_path = br.readLine(); 
                	System.out.println("신청 예산을 입력하세요");
                	int application_budget_amt = Integer.parseInt(br.readLine()); 
                	
                	//신청서 자동 제출
                	
                	conn = DBUtil.getConnection();

 	                sql = "INSERT INTO APPLICATIONS (APPLICATION_ID, APPLICATION_ANN_ID, APPLICATION_USER_ID, APPLICATION_APPLIED_AT,"
 	                		+ " APPLICATION_UPDATED_AT, APPLICATION_ATTACH_PATH, APPLICATION_STATUS_CD, APPLICATION_BUDGET_AMT)"
 	                		+ "VALUES (?, ?, ?, SYSDATE, SYSDATE, ?, ?, ?)";
 	               pstmt = conn.prepareStatement(sql);

 	              pstmt.setInt(1, application_id);
 	              pstmt.setInt(2, annId);
 	              pstmt.setString(3, application_user_id); //여기에서 로그인 아이디를 직접 받을수 있는 방법이 있을까요????
 	              pstmt.setString(4, application_attach_path);
 	              pstmt.setString(5, "접수");
 	              pstmt.setInt(6, application_budget_amt);

 	              int count = pstmt.executeUpdate();
 	              
 	              if(count > 0) {
 	                  System.out.println("신청이 완료되었습니다.");
 	              }
                } else if (sel == 2) {
                    return;  // 목록으로 복귀
                } else {
                    System.out.println("잘못된 입력입니다.");
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                DBUtil.executeClose(rs, pstmt, conn);
            }
        }
    }
}