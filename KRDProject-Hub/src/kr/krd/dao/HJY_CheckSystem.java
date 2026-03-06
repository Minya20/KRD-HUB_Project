package kr.krd.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import kr.util.DBUtil;

public class HJY_CheckSystem {

    public void applyAnnouncement(int annId,
                                  String userId,
                                  String attachPath,
                                  int budgetAmt) {

        Connection conn = null;
        PreparedStatement pstmt = null;
        PreparedStatement pstmt2 = null;
        ResultSet rs = null;

        try {

            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            /* 1 공고 상태 + 모집인원 + 분야 조회 */
            String checkSql =
                    "SELECT ANNOUNCEMENT_STATUS, ANNOUNCEMENT_RECRUIT_CAP, ANNOUNCEMENT_FIELD " +
                    "FROM ANNOUNCEMENT " +
                    "WHERE ANNOUNCEMENT_ANN_ID = ? " +
                    "AND ANNOUNCEMENT_HIDDEN_YN = 0";

            pstmt = conn.prepareStatement(checkSql);
            pstmt.setInt(1, annId);
            rs = pstmt.executeQuery();

            if (!rs.next()) {
                System.out.println("공고가 존재하지 않습니다.");
                conn.rollback();
                return;
            }

            String status = rs.getString("ANNOUNCEMENT_STATUS");//모집중인지 상태 체크
            int cap = rs.getInt("ANNOUNCEMENT_RECRUIT_CAP");//모집인원
            String field = rs.getString("ANNOUNCEMENT_FIELD");//분야 체크

            if (!"공고중".equals(status)) {
                System.out.println("현재 모집중인 공고가 아닙니다.");
                conn.rollback();
                return;
            }

            if (cap == 0) {
                System.out.println("모집 인원이 마감되었습니다.");
                conn.rollback();
                return;
            }

            rs.close();
            pstmt.close();


           
            /* 신청자 FIELD 조회 */
            String userFieldSql =
                    "SELECT USER_FIELD FROM USERINFO WHERE USER_ID = ?";

            pstmt = conn.prepareStatement(userFieldSql);
            pstmt.setString(1, userId);
            rs = pstmt.executeQuery();

            if (!rs.next()) {
                System.out.println("사용자 정보가 존재하지 않습니다.");
                conn.rollback();
                return;
            }

            String userField = rs.getString("USER_FIELD");

            if (!field.equals(userField)) {
                System.out.println("신청자 분야와 공고 분야가 일치하지 않습니다.");
                conn.rollback();
                return;
            }

            rs.close();
            pstmt.close();
            


            /* 2 중복 신청 체크 */
            String dupSql =
                    "SELECT COUNT(*) FROM APPLICATIONS " +
                    "WHERE APPLICATION_ANN_ID = ? " +
                    "AND APPLICATION_USER_ID = ?";

            pstmt = conn.prepareStatement(dupSql);
            pstmt.setInt(1, annId);
            pstmt.setString(2, userId);
            rs = pstmt.executeQuery();
            rs.next();

            if (rs.getInt(1) > 0) {
                System.out.println("이미 신청한 공고입니다.");
                conn.rollback();
                return;
            }

            rs.close();
            pstmt.close();


            /* 3 진행중 과제 5개 제한 */
            String countSql =
                    "SELECT COUNT(*) FROM APPLICATIONS " +
                    "WHERE APPLICATION_USER_ID = ? " +
                    "AND APPLICATION_STATUS_CD = 'SELECTED'";

            pstmt = conn.prepareStatement(countSql);
            pstmt.setString(1, userId);
            rs = pstmt.executeQuery();
            rs.next();

            if (rs.getInt(1) >= 5) {
                System.out.println("진행중 과제가 5개 이상입니다.");
                conn.rollback();
                return;
            }

            rs.close();
            pstmt.close();


            /* 4 APPLICATION INSERT */
            String insertAppSql =
                    "INSERT INTO APPLICATIONS (" +
                    "APPLICATION_ID, " +
                    "APPLICATION_ANN_ID, " +
                    "APPLICATION_USER_ID, " +
                    "APPLICATION_APPLIED_AT, " +
                    "APPLICATION_UPDATED_AT, " +
                    "APPLICATION_ATTACH_PATH, " +
                    "APPLICATION_STATUS_CD, " +
                    "APPLICATION_BUDGET_AMT" +
                    ") VALUES (app_seq.nextval, ?, ?, SYSDATE, SYSDATE, ?, 'APPLIED', ?)";

            pstmt = conn.prepareStatement(insertAppSql);

            pstmt.setInt(1, annId);
            pstmt.setString(2, userId);
            pstmt.setString(3, attachPath);
            pstmt.setInt(4, budgetAmt);

            pstmt.executeUpdate();
            pstmt.close();


            /* APPLICATION_ID 가져오기 */
            int applicationId = 0;

            pstmt = conn.prepareStatement(
                    "SELECT app_seq.currval FROM dual");//방금 시퀀스로 만든 값을 반환하라는 sql문

            rs = pstmt.executeQuery();

            if (rs.next()) {
                applicationId = rs.getInt(1);
            }

            rs.close();
            pstmt.close();


            /* 5 SELECTION INSERT */
            String insertSelSql =
                    "INSERT INTO SELECTION ( " +
                    "SELECTION_ID, " +
                    "SELECTION_RESULT_CD, " +
                    "SELECTION_ANN_ID, " +
                    "SELECTION_APPLICATION_ID, " +
                    "SELECTION_APPROVER_ID " +
                    ") " +
                    "SELECT " +
                    "sel_seq.nextval, " +
                    "'PENDING', " +
                    "A.APPLICATION_ANN_ID, " +
                    "A.APPLICATION_ID, " +
                    "A.APPLICATION_USER_ID " +
                    "FROM APPLICATIONS A " +
                    "WHERE A.APPLICATION_ID = ?";

            pstmt = conn.prepareStatement(insertSelSql);
            pstmt.setInt(1, applicationId);

            pstmt.executeUpdate();
            pstmt.close();

            /* 6 평가위원 랜덤 5명 조회 (중복 방지 + 신청자 제외) */
            String evalSql =
                    "SELECT DISTINCT U.USER_ID " +
                    "FROM USERINFO U " +
                    "JOIN USERINFO AP ON AP.USER_ID = ? " +
                    "WHERE U.USER_ROLE_CD = 'REV' " +
                    "AND U.USER_AFFILIATION <> AP.USER_AFFILIATION " +
                    "AND U.USER_ID <> AP.USER_ID " +
                    "ORDER BY DBMS_RANDOM.VALUE " +
                    "FETCH FIRST 5 ROWS ONLY";

            pstmt = conn.prepareStatement(evalSql);
            pstmt.setString(1, userId);
            rs = pstmt.executeQuery();


            /* 7 EVALUATION INSERT */
            String insertEvalSql =
                    "INSERT INTO EVALUATIONS ( " +
                    "EVALUATION_ID, " +
                    "EVALUATION_APPLICATION_ID, " +
                    "EVALUATION_REVIEWER_ID, " +
                    "EVALUATION_FIELD, " +
                    "EVALUATION_STATUS_CD " +
                    ") VALUES (eval_seq.nextval, ?, ?, ?, 'ASSIGNED')";

            pstmt2 = conn.prepareStatement(insertEvalSql);

            int evalCount = 0;

            while (rs.next()) {

                String evaluatorId = rs.getString("USER_ID");

                pstmt2.setInt(1, applicationId);
                pstmt2.setString(2, evaluatorId);
                pstmt2.setString(3, field);

                pstmt2.executeUpdate();

                evalCount++;
            }

            pstmt2.close();

            if (evalCount < 5) {
                System.out.println("평가위원이 부족합니다.");
            }

            conn.commit();

            System.out.println("신청 완료! 평가위원 5명이 배정되었습니다.");

        } catch (Exception e) {

            try {
                if (conn != null) conn.rollback();
            } catch (Exception ex) {
            	e.printStackTrace();}

        } finally {

            DBUtil.executeClose(rs, pstmt, conn);
        }
    }
}