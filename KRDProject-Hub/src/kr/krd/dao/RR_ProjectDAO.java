package kr.krd.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import kr.krd.constant.RR_AnnouncementStatus;
import kr.krd.vo.RR_ProjectVO;
import kr.krd.vo.RR_TaskVO;
import kr.util.DBUtil;


public class RR_ProjectDAO {

    // 기관 기준 "관리할 과제(공고) 목록"
    // 선정완료(SELECT_DONE) 공고만 보이도록 제한
    public List<RR_TaskVO> getTaskListByAgency(int agyId) {
        List<RR_TaskVO> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql =
                "SELECT an.ANNOUNCEMENT_ANN_ID AS ANN_ID, an.ANNOUNCEMENT_TITLE AS TITLE, "
              + "       COUNT(p.PROJECT_ID) AS TEAM_CNT, "
              + "       CASE "
              + "         WHEN SUM(CASE WHEN p.PROJECT_STATUS_CD IN ('ONGOING','IN_PROGRESS') THEN 1 ELSE 0 END) > 0 THEN '진행중' "
              + "         WHEN SUM(CASE WHEN p.PROJECT_STATUS_CD = 'STOPPED' THEN 1 ELSE 0 END) > 0 "
              + "              AND SUM(CASE WHEN p.PROJECT_STATUS_CD = 'COMPLETED' THEN 1 ELSE 0 END) = COUNT(*) THEN '중단' "
              + "         WHEN SUM(CASE WHEN p.PROJECT_STATUS_CD = 'COMPLETED' THEN 1 ELSE 0 END) = COUNT(*) THEN '완료' "
              + "         ELSE '대기' "
              + "       END AS TASK_STATUS "
              + "FROM ANNOUNCEMENT an "
              + "JOIN APPLICATIONS ap ON ap.APPLICATION_ANN_ID = an.ANNOUNCEMENT_ANN_ID "
              + "JOIN PROJECTS p ON p.PROJECT_APPLICATION_ID = ap.APPLICATION_ID "
              + "WHERE an.ANNOUNCEMENT_AGY_ID = ? "
              + "  AND an.ANNOUNCEMENT_HIDDEN_YN = 0 "
              + "  AND an.ANNOUNCEMENT_STATUS = ? "     // ✅ 선정완료 공고만
              + "GROUP BY an.ANNOUNCEMENT_ANN_ID, an.ANNOUNCEMENT_TITLE "
              + "ORDER BY an.ANNOUNCEMENT_ANN_ID DESC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, agyId);
            pstmt.setString(2, RR_AnnouncementStatus.SELECT_DONE); // "선정완료"
            rs = pstmt.executeQuery();

            while (rs.next()) {
                RR_TaskVO vo = new RR_TaskVO();
                vo.setAnnId(rs.getInt("ANN_ID"));
                vo.setTitle(rs.getString("TITLE"));
                vo.setTeamCount(rs.getInt("TEAM_CNT"));
                vo.setTaskStatus(rs.getString("TASK_STATUS"));
                list.add(vo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }

        return list;
    }

    // 2) 선택한 "과제(공고)"의 선정된 팀 목록(프로젝트 목록)
    public List<RR_ProjectVO> getTeamsByTask(int agyId, int annId) {
        List<RR_ProjectVO> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql =
                "SELECT p.PROJECT_ID, p.PROJECT_STATUS_CD, "
              + "       ap.APPLICATION_ID, ap.APPLICATION_USER_ID, u.USER_NAME, "
              + "       NVL(ag.AGREEMENT_STATUS_CD, 'PENDING') AS AGREEMENT_STATUS_CD "
              + "FROM ANNOUNCEMENT an "
              + "JOIN APPLICATIONS ap ON ap.APPLICATION_ANN_ID = an.ANNOUNCEMENT_ANN_ID "
              + "JOIN PROJECTS p ON p.PROJECT_APPLICATION_ID = ap.APPLICATION_ID "
              + "JOIN USERINFO u ON u.USER_ID = ap.APPLICATION_USER_ID "
              + "LEFT JOIN AGREEMENT ag ON ag.AGREEMENT_PROJECT_ID = p.PROJECT_ID "
              + "WHERE an.ANNOUNCEMENT_AGY_ID = ? "
              + "  AND an.ANNOUNCEMENT_ANN_ID = ? "
              + "  AND an.ANNOUNCEMENT_HIDDEN_YN = 0 "
              + "ORDER BY p.PROJECT_ID";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, agyId);
            pstmt.setInt(2, annId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                RR_ProjectVO vo = new RR_ProjectVO();
                vo.setProjectId(rs.getInt("PROJECT_ID"));
                vo.setApplicationId(rs.getInt("APPLICATION_ID"));
                vo.setProjectStatusCd(rs.getString("PROJECT_STATUS_CD"));
                vo.setUserId(rs.getString("APPLICATION_USER_ID"));
                vo.setUserName(rs.getString("USER_NAME"));
                vo.setAgreementStatusCd(rs.getString("AGREEMENT_STATUS_CD"));
                list.add(vo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }

        return list;
    }

    // 3) 협약 체결 처리(프로젝트 단위)
    // return: 1 성공, 0 이미 체결, -1 실패
    public int signAgreement(int projectId, String signerId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            // 이미 존재/체결 확인
            String chkSql =
                "SELECT NVL(AGREEMENT_STATUS_CD,'PENDING') AS ST "
              + "FROM AGREEMENT WHERE AGREEMENT_PROJECT_ID = ?";
            pstmt = conn.prepareStatement(chkSql);
            pstmt.setInt(1, projectId);
            rs = pstmt.executeQuery();

            boolean exists = false;
            String st = "PENDING";
            if (rs.next()) {
                exists = true;
                st = rs.getString("ST");
            }
            DBUtil.executeClose(rs, pstmt, null);

            if ("SIGNED".equalsIgnoreCase(st)) {
                return 0;
            }

            if (!exists) {
                String insSql =
                    "INSERT INTO AGREEMENT (AGREEMENT_ID, AGREEMENT_PROJECT_ID, AGREEMENT_STATUS_CD, AGREEMENT_SIGNED_AT, AGREEMENT_SIGNED_BY) "
                  + "VALUES (AGREEMENT_SEQ.NEXTVAL, ?, 'SIGNED', SYSDATE, ?)";
                pstmt = conn.prepareStatement(insSql);
                pstmt.setInt(1, projectId);
                pstmt.setString(2, signerId);
                return pstmt.executeUpdate();
            } else {
                String updSql =
                    "UPDATE AGREEMENT "
                  + "SET AGREEMENT_STATUS_CD='SIGNED', AGREEMENT_SIGNED_AT=SYSDATE, AGREEMENT_SIGNED_BY=? "
                  + "WHERE AGREEMENT_PROJECT_ID=?";
                pstmt = conn.prepareStatement(updSql);
                pstmt.setString(1, signerId);
                pstmt.setInt(2, projectId);
                return pstmt.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }

    // 4) 연구 중단(프로젝트 단위)
    public int stopProject(int projectId) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE PROJECTS SET PROJECT_STATUS_CD = 'STOPPED' WHERE PROJECT_ID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, projectId);
            return pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            DBUtil.executeClose(null, pstmt, conn);
        }
    }
    
    // 5) 팀 완료 처리 메서드
    public int completeProject(int projectId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int count = 0;

        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE PROJECTS "
                       + "SET PROJECT_STATUS_CD = 'COMPLETED', "
                       + "    PROJECT_PROGRESS_PCT = 100 "
                       + "WHERE PROJECT_ID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, projectId);
            count = pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(null, pstmt, conn);
        }

        return count;
    }
    
    // 완료 과제는 5번 메뉴 사용 불가
    public RR_TaskVO getTaskByAnnId(int agyId, int annId) {
        RR_TaskVO vo = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql =
                "SELECT an.ANNOUNCEMENT_ANN_ID AS ANN_ID, "
              + "       an.ANNOUNCEMENT_TITLE AS TITLE, "
              + "       COUNT(p.PROJECT_ID) AS TEAM_COUNT, "
              + "       CASE "
              + "         WHEN SUM(CASE WHEN p.PROJECT_STATUS_CD IN ('ONGOING','IN_PROGRESS') THEN 1 ELSE 0 END) > 0 THEN '진행중' "
              + "         WHEN SUM(CASE WHEN p.PROJECT_STATUS_CD = 'COMPLETED' THEN 1 ELSE 0 END) = COUNT(p.PROJECT_ID) THEN '완료' "
              + "         WHEN SUM(CASE WHEN p.PROJECT_STATUS_CD = 'STOPPED' THEN 1 ELSE 0 END) = COUNT(p.PROJECT_ID) THEN '중단' "
              + "         ELSE '진행중' "
              + "       END AS TASK_STATUS "
              + "FROM ANNOUNCEMENT an "
              + "JOIN APPLICATIONS ap "
              + "  ON ap.APPLICATION_ANN_ID = an.ANNOUNCEMENT_ANN_ID "
              + "JOIN PROJECTS p "
              + "  ON p.PROJECT_APPLICATION_ID = ap.APPLICATION_ID "
              + "WHERE an.ANNOUNCEMENT_AGY_ID = ? "
              + "  AND an.ANNOUNCEMENT_ANN_ID = ? "
              + "  AND an.ANNOUNCEMENT_HIDDEN_YN = 0 "
              + "GROUP BY an.ANNOUNCEMENT_ANN_ID, an.ANNOUNCEMENT_TITLE";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, agyId);
            pstmt.setInt(2, annId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                vo = new RR_TaskVO();
                vo.setAnnId(rs.getInt("ANN_ID"));
                vo.setTitle(rs.getString("TITLE"));
                vo.setTeamCount(rs.getInt("TEAM_COUNT"));
                vo.setTaskStatus(rs.getString("TASK_STATUS"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }

        return vo;
    }
    
}