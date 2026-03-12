package kr.krd.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import kr.krd.constant.RR_AnnouncementStatus;
import kr.krd.vo.RR_SelectionVO;
import kr.util.DBUtil;

public class RR_SelectionDAO {

	// 공고별 신청서 랭킹: SUBMITTED 기준 평균점수 + 제출개수 + 희망연구비
	public List<RR_SelectionVO> getRankedCandidates(int annId) {
		List<RR_SelectionVO> list = new ArrayList<>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = DBUtil.getConnection();

			String sql =
					"SELECT ap.APPLICATION_ID, ap.APPLICATION_USER_ID, u.USER_NAME, "
							+ "       NVL(ap.APPLICATION_BUDGET_AMT,0) AS BUDGET_AMT, "
							+ "       COUNT(CASE WHEN e.EVALUATION_STATUS_CD = 'SUBMITTED' THEN 1 END) AS SUBMITTED_CNT, "
							+ "       ROUND(NVL(AVG(CASE WHEN e.EVALUATION_STATUS_CD = 'SUBMITTED' THEN e.EVALUATION_SCORE END),0),2) AS AVG_SCORE "
							+ "FROM APPLICATIONS ap "
							+ "JOIN USERINFO u ON u.USER_ID = ap.APPLICATION_USER_ID "
							+ "LEFT JOIN EVALUATIONS e ON e.EVALUATION_APPLICATION_ID = ap.APPLICATION_ID "
							+ "WHERE ap.APPLICATION_ANN_ID = ? "
							+ "GROUP BY ap.APPLICATION_ID, ap.APPLICATION_USER_ID, u.USER_NAME, ap.APPLICATION_BUDGET_AMT "
							+ "ORDER BY CASE WHEN COUNT(CASE WHEN e.EVALUATION_STATUS_CD='SUBMITTED' THEN 1 END) >= 5 THEN 1 ELSE 0 END DESC, "
							+ "         AVG_SCORE DESC, ap.APPLICATION_ID";

			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, annId);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				RR_SelectionVO vo = new RR_SelectionVO();
				vo.setApplicationId(rs.getInt("APPLICATION_ID"));
				vo.setUserId(rs.getString("APPLICATION_USER_ID"));
				vo.setUserName(rs.getString("USER_NAME"));
				vo.setBudgetAmt(rs.getLong("BUDGET_AMT"));
				vo.setSubmittedCnt(rs.getInt("SUBMITTED_CNT"));
				vo.setAvgScore(rs.getDouble("AVG_SCORE"));
				list.add(vo);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}

		return list;
	}

	// ✅ 해당 공고의 모든 신청서가 평가 5개(SUBMITTED) 완료됐는지
	public boolean isAllEvaluationsSubmitted(int annId, int requiredCnt) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = DBUtil.getConnection();

			String sql =
					"SELECT COUNT(*) AS NOT_DONE_CNT "
							+ "FROM ( "
							+ "   SELECT ap.APPLICATION_ID, "
							+ "          COUNT(CASE WHEN e.EVALUATION_STATUS_CD='SUBMITTED' THEN 1 END) AS SUBMITTED_CNT "
							+ "   FROM APPLICATIONS ap "
							+ "   LEFT JOIN EVALUATIONS e ON e.EVALUATION_APPLICATION_ID = ap.APPLICATION_ID "
							+ "   WHERE ap.APPLICATION_ANN_ID = ? "
							+ "   GROUP BY ap.APPLICATION_ID "
							+ ") t "
							+ "WHERE t.SUBMITTED_CNT < ?";

			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, annId);
			pstmt.setInt(2, requiredCnt);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				int notDone = rs.getInt("NOT_DONE_CNT");
				return notDone == 0;
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}

		return false;
	}

	// 중복 승인 방지
	public boolean hasSelectionResult(int annId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean result = false;

		try {
			conn = DBUtil.getConnection();

			String sql =
					"SELECT COUNT(*) " +
							"FROM SELECTION " +
							"WHERE SELECTION_ANN_ID = ? " +
							"  AND SELECTION_RESULT_CD IN ('SELECTED', 'REJECTED')";

			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, annId);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				result = rs.getInt(1) > 0;
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}

		return result;
	}

	// 승인 저장: selectedCount만 SELECTED, 나머지는 REJECTED
	// ✅ 공고 상태는 SELECT_DONE(선정완료)로 업데이트
	// ✅ SELECTED 된 신청서로 PROJECTS 자동 생성
	public int approveSelection(int annId, int agyId, String approverId,
			List<RR_SelectionVO> ranked, int selectedCount) {

		Connection conn = null;
		PreparedStatement pstmtChk = null;
		PreparedStatement pstmtUpdSel = null;
		PreparedStatement pstmtInsSel = null;
		PreparedStatement pstmtUpdAnn = null;
		PreparedStatement pstmtUpdApp = null;
		PreparedStatement pstmtInsProj = null;
		ResultSet rs = null;

		try {
			conn = DBUtil.getConnection();
			conn.setAutoCommit(false);

			// 1) 최종 승인 중복 방지
			// PENDING은 무시하고, SELECTED/REJECTED만 있으면 이미 승인된 것으로 판단
			String chkSql =
					"SELECT COUNT(*) " +
							"FROM SELECTION " +
							"WHERE SELECTION_ANN_ID = ? " +
							"  AND SELECTION_RESULT_CD IN ('SELECTED', 'REJECTED')";

			pstmtChk = conn.prepareStatement(chkSql);
			pstmtChk.setInt(1, annId);
			rs = pstmtChk.executeQuery();

			if (rs.next() && rs.getInt(1) > 0) {
				conn.rollback();
				return -2;
			}

			// 2) 기존 PENDING 행이 있으면 최종 결과로 UPDATE
			String updSelSql =
					"UPDATE SELECTION " +
							"SET SELECTION_RESULT_CD = ?, " +
							"    SELECTION_APPROVED_AT = SYSDATE, " +
							"    SELECTION_APPROVER_ID = ?, " +
							"    SELECTION_FINAL_SCORE = ? " +
							"WHERE SELECTION_ANN_ID = ? " +
							"  AND SELECTION_APPLICATION_ID = ? " +
							"  AND SELECTION_RESULT_CD = 'PENDING'";

			pstmtUpdSel = conn.prepareStatement(updSelSql);

			// 3) PENDING이 없으면 새로 INSERT
			String insSelSql =
					"INSERT INTO SELECTION ("
							+ "  SELECTION_ID, SELECTION_RESULT_CD, SELECTION_ANN_ID, "
							+ "  SELECTION_APPROVED_AT, SELECTION_APPROVER_ID, SELECTION_FINAL_SCORE, SELECTION_APPLICATION_ID"
							+ ") VALUES ("
							+ "  SELECTION_SEQ.NEXTVAL, ?, ?, SYSDATE, ?, ?, ?"
							+ ")";

			pstmtInsSel = conn.prepareStatement(insSelSql);

			// 4) 신청서 상태 업데이트
			// 코드값은 영어로 맞춰야 다른 로직과 안 꼬임
			String updAppSql =
					"UPDATE APPLICATIONS " +
							"SET APPLICATION_STATUS_CD = ?, APPLICATION_UPDATED_AT = SYSDATE " +
							"WHERE APPLICATION_ID = ?";

			pstmtUpdApp = conn.prepareStatement(updAppSql);

			int affected = 0;
			selectedCount = Math.min(selectedCount, ranked.size());

			for (int i = 0; i < ranked.size(); i++) {
				RR_SelectionVO c = ranked.get(i);
				String resultCd = (i < selectedCount) ? "SELECTED" : "REJECTED";

				// 먼저 PENDING -> 최종 결과로 변경 시도
				pstmtUpdSel.setString(1, resultCd);
				pstmtUpdSel.setString(2, approverId);
				pstmtUpdSel.setDouble(3, c.getAvgScore());
				pstmtUpdSel.setInt(4, annId);
				pstmtUpdSel.setInt(5, c.getApplicationId());

				int updated = pstmtUpdSel.executeUpdate();

				// PENDING이 없으면 새로 INSERT
				if (updated == 0) {
					pstmtInsSel.setString(1, resultCd);
					pstmtInsSel.setInt(2, annId);
					pstmtInsSel.setString(3, approverId);
					pstmtInsSel.setDouble(4, c.getAvgScore());
					pstmtInsSel.setInt(5, c.getApplicationId());
					affected += pstmtInsSel.executeUpdate();
				} else {
					affected += updated;
				}

				// 신청서 상태도 최종 결과로 맞춤
				pstmtUpdApp.setString(1, resultCd);
				pstmtUpdApp.setInt(2, c.getApplicationId());
				pstmtUpdApp.executeUpdate();
			}

			// 5) 공고 상태 = 선정완료
			String updAnnSql =
					"UPDATE ANNOUNCEMENT "
							+ "SET ANNOUNCEMENT_STATUS = ? "
							+ "WHERE ANNOUNCEMENT_ANN_ID = ? "
							+ "  AND ANNOUNCEMENT_AGY_ID = ? "
							+ "  AND ANNOUNCEMENT_HIDDEN_YN = 0";

			pstmtUpdAnn = conn.prepareStatement(updAnnSql);
			pstmtUpdAnn.setString(1, RR_AnnouncementStatus.SELECT_DONE);
			pstmtUpdAnn.setInt(2, annId);
			pstmtUpdAnn.setInt(3, agyId);
			pstmtUpdAnn.executeUpdate();

			// 6) 선정된 팀만 PROJECTS 자동 생성
			String insProjSql =
					"INSERT INTO PROJECTS ("
							+ "  PROJECT_ID, PROJECT_APPLICATION_ID, PROJECT_OWNER_ID, PROJECT_START_DT, PROJECT_END_DT, "
							+ "  PROJECT_STATUS_CD, PROJECT_FIELD, PROJECT_PROGRESS_PCT"
							+ ") "
							+ "SELECT "
							+ "  PROJECT_SEQ.NEXTVAL, "
							+ "  s.SELECTION_APPLICATION_ID, "
							+ "  ap.APPLICATION_USER_ID, "
							+ "  TO_CHAR(SYSDATE,'YYYY-MM-DD'), "
							+ "  an.ANNOUNCEMENT_END_DT, "
							+ "  'ONGOING', "
							+ "  an.ANNOUNCEMENT_FIELD, "
							+ "  0 "
							+ "FROM SELECTION s "
							+ "JOIN APPLICATIONS ap ON ap.APPLICATION_ID = s.SELECTION_APPLICATION_ID "
							+ "JOIN ANNOUNCEMENT an ON an.ANNOUNCEMENT_ANN_ID = s.SELECTION_ANN_ID "
							+ "WHERE s.SELECTION_ANN_ID = ? "
							+ "  AND s.SELECTION_RESULT_CD = 'SELECTED' "
							+ "  AND NOT EXISTS ( "
							+ "      SELECT 1 FROM PROJECTS p "
							+ "      WHERE p.PROJECT_APPLICATION_ID = s.SELECTION_APPLICATION_ID "
							+ "  )";

			pstmtInsProj = conn.prepareStatement(insProjSql);
			pstmtInsProj.setInt(1, annId);
			pstmtInsProj.executeUpdate();

			conn.commit();
			return affected;

		} catch (Exception e) {
			try { if (conn != null) conn.rollback(); } catch (Exception ignore) {}
			e.printStackTrace();
			return -1;
		} finally {
			try { if (rs != null) rs.close(); } catch (Exception ignore) {}
			try { if (pstmtChk != null) pstmtChk.close(); } catch (Exception ignore) {}
			try { if (pstmtUpdSel != null) pstmtUpdSel.close(); } catch (Exception ignore) {}
			try { if (pstmtInsSel != null) pstmtInsSel.close(); } catch (Exception ignore) {}
			try { if (pstmtUpdAnn != null) pstmtUpdAnn.close(); } catch (Exception ignore) {}
			try { if (pstmtUpdApp != null) pstmtUpdApp.close(); } catch (Exception ignore) {}
			try { if (pstmtInsProj != null) pstmtInsProj.close(); } catch (Exception ignore) {}
			try { if (conn != null) conn.close(); } catch (Exception ignore) {}
		}
	}

	// 선정 결과 조회
	public List<RR_SelectionVO> getSelectionResults(int annId) {
	    List<RR_SelectionVO> list = new ArrayList<>();
	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;

	    try {
	        conn = DBUtil.getConnection();

	        String sql =
	            "SELECT s.SELECTION_APPLICATION_ID, ap.APPLICATION_USER_ID, u.USER_NAME, "
	          + "       s.SELECTION_FINAL_SCORE, s.SELECTION_RESULT_CD, "
	          + "       TO_CHAR(s.SELECTION_APPROVED_AT, 'YYYY-MM-DD') AS APPROVED_AT "
	          + "FROM SELECTION s "
	          + "JOIN APPLICATIONS ap ON ap.APPLICATION_ID = s.SELECTION_APPLICATION_ID "
	          + "JOIN USERINFO u ON u.USER_ID = ap.APPLICATION_USER_ID "
	          + "WHERE s.SELECTION_ANN_ID = ? "
	          + "  AND s.SELECTION_RESULT_CD IN ('SELECTED', 'REJECTED') "
	          + "ORDER BY s.SELECTION_FINAL_SCORE DESC NULLS LAST, s.SELECTION_APPLICATION_ID";

	        pstmt = conn.prepareStatement(sql);
	        pstmt.setInt(1, annId);
	        rs = pstmt.executeQuery();

	        while (rs.next()) {
	            RR_SelectionVO vo = new RR_SelectionVO();
	            vo.setApplicationId(rs.getInt("SELECTION_APPLICATION_ID"));
	            vo.setUserId(rs.getString("APPLICATION_USER_ID"));
	            vo.setUserName(rs.getString("USER_NAME"));
	            vo.setAvgScore(rs.getDouble("SELECTION_FINAL_SCORE"));
	            vo.setResultCd(rs.getString("SELECTION_RESULT_CD"));
	            vo.setApprovedAt(rs.getString("APPROVED_AT"));
	            list.add(vo);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        DBUtil.executeClose(rs, pstmt, conn);
	    }

	    return list;
	}
}