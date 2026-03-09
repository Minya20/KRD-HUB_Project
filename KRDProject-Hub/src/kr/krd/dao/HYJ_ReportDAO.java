package kr.krd.dao;

import java.io.BufferedReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import kr.util.DBUtil;

public class HYJ_ReportDAO {

    private BufferedReader br;

    public HYJ_ReportDAO(BufferedReader br) {
        this.br = br;
    }
 
    // ===============================
    // 보고서 등록
    // ===============================
    public void InsertReport(String cust_id) {

        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql = null;
        int cnt = 0;

        try {
            conn = DBUtil.getConnection();

            // 1. 내가 맡고 있는 프로젝트가 있는지 확인
            if (!hasMyProject(conn, cust_id)) {
                System.out.println("현재 맡고 있는 프로젝트가 없습니다.");
                return;
            }

            // 2. 내 프로젝트 목록 출력
            System.out.println("\n[내 프로젝트 목록]");
            showMyProjects(conn, cust_id);

            // 3. 프로젝트 선택
            System.out.print("보고서를 제출할 프로젝트 아이디 : ");
            String project_id = br.readLine();

            // 4. 프로젝트 확인 (내 프로젝트인지)
            if (!checkProject(conn, cust_id, project_id)) {
                System.out.println("존재하지 않거나 본인 프로젝트가 아닙니다.");
                return;
            }

            // 5. 보고서 입력
            sql = "INSERT INTO REPORTS("
                    + "report_rpt_id, report_project_id, report_rpt_type_cd, report_submitted_at, "
                    + "report_status_cd, report_content, report_keywords, report_progress_rate, report_approved_by) "
                    + "VALUES(REPORTS_SEQ.NEXTVAL, ?, ?, SYSDATE, 'APPLIED', ?, ?, ?, ?)";

            pstmt = conn.prepareStatement(sql);

            System.out.print("보고서 타입[중간 / 최종] : ");
            String type = br.readLine();

            System.out.print("내용 : ");
            String content = br.readLine();

            System.out.print("키워드 : ");
            String keywords = br.readLine();

            System.out.print("진행률[숫자만 입력] : ");
            int progress = Integer.parseInt(br.readLine());

            pstmt.setString(++cnt, project_id);
            pstmt.setString(++cnt, type);
            pstmt.setString(++cnt, content);
            pstmt.setString(++cnt, keywords);
            pstmt.setInt(++cnt, progress);
            pstmt.setString(++cnt, "agy01");

            int result = pstmt.executeUpdate();

            if (result > 0) {
                System.out.println("보고서 등록 완료");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // ===============================
    // 내 프로젝트가 하나라도 있는지 확인
    // ===============================
    private boolean hasMyProject(Connection conn, String cust_id) {

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;

        try {
            sql = "SELECT COUNT(*) "
                + "FROM PROJECTS "
                + "WHERE project_owner_id = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, cust_id);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, null);
        }

        return false;
    }

    // ===============================
    // 내 프로젝트 목록 출력
    // ===============================
    private void showMyProjects(Connection conn, String cust_id) {

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;

        try {
            sql = "SELECT project_id "
                + "FROM PROJECTS "
                + "WHERE project_owner_id = ? "
                + "ORDER BY project_id";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, cust_id);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                System.out.println("- 프로젝트 아이디 : " + rs.getString("project_id"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, null);
        }
    }

    // ===============================
    // 프로젝트 존재 + 소유 확인
    // ===============================
    private boolean checkProject(Connection conn, String cust_id, String project_id) {

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;

        try {
            sql = "SELECT COUNT(*) "
                + "FROM PROJECTS "
                + "WHERE project_id = ? "
                + "AND project_owner_id = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, project_id);
            pstmt.setString(2, cust_id);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, null);
        }

        return false;
    }
}