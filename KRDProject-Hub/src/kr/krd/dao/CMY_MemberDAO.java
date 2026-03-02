package kr.krd.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import kr.util.DBUtil;



public class CMY_MemberDAO {
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	String cust_id;
	String role;
	String field;
	
	//로그인 메서드
	public String userLogin(String user_id, String user_pw) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		ResultSet rs = null;
		String real_id = null;
		
		try {
			conn = DBUtil.getConnection();
			sql = "SELECT * FROM USERINFO WHERE USER_ID = ? AND USER_PWD = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, user_id);
			pstmt.setString(2, user_pw);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				String user_name = rs.getString("USER_NAME");
				System.out.println(user_name + "님 환영합니다.");
				real_id = rs.getString("user_id");	//일치하면 real_id에 유저 아이디를 넣는다
				
			}else {
				System.out.println("아이디 혹은 비밀번호가 일치 하지 않습니다.");
				real_id = "0";	//틀리면 "0"을 넣는다.
				
			}
		}
		catch(Exception e){e.printStackTrace();}
		finally {DBUtil.executeClose(rs, pstmt, conn);}
		return real_id;
	}
	
	//사용자의 권한을 반환하는 메서드
	public String getUserRole(String user_id) {
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		ResultSet rs = null;
		String real_role = null;
		
		try {
			conn = DBUtil.getConnection();
			sql = "SELECT user_role_cd FROM USERINFO WHERE USER_ID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, user_id);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				real_role = rs.getString("user_role_cd");	//일치하면 real_id에 유저 아이디를 넣는다
			}
		}
		catch(Exception e){e.printStackTrace();}
		finally {DBUtil.executeClose(rs, pstmt, conn);}
		return real_role;
	}
	
	//사용자의 분야를 반환하는 메서드
	public String getUserField(String user_id) {
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		ResultSet rs = null;
		String real_field = null;
		
		try {
			conn = DBUtil.getConnection();
			sql = "SELECT user_field FROM USERINFO WHERE USER_ID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, user_id);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				real_field = rs.getString("user_field");	//일치하면 real_id에 유저 아이디를 넣는다
			}
		}
		catch(Exception e){e.printStackTrace();}
		finally {DBUtil.executeClose(rs, pstmt, conn);}
		return real_field;
	}
	
	
	//평가 배정 목록 조회
	//평가 테이블에 행이 있어야 조회 함
	public void readEval() {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		ResultSet rs = null;
		//DB 행값을 담을 변수를 선언
		int eval_id=0;				//평가번호
		int eval_app_id=0	;	//신청번호
		String eval_rev_id="";	//평가위원ID
		String eval_field="";			//평가분야
		Date eval_assigned_dt =null;	//평가배정일
		int eval_score=0;	
		String eval_comment="";			//평가 코멘트//평가 점수
		String eval_status="";		//평가 상태
		
		Date ann_start_dt =null;		//공고(평가)시작일
		Date ann_end_dt =null;			//공고(평가)마감일			
		String ann_title="";			//공고명
		String ann_desc="";			//공고 설명
		String ann_budget="";	//총 예산
		
		int app_budget_amt=0;		//신청자의 요구 예산
		String app_user_id="";		//신청자 아이디
		String app_attach="";	//신청자 첨부파일
		
		String ag_name="";		//기관명
		
		try {
			conn = DBUtil.getConnection();
			sql = "SELECT  e.EVALUATION_ID,\r\n"
					+ "		e.EVALUATION_APPLICATION_ID,\r\n"
					+ "		e.EVALUATION_REVIEWER_ID,\r\n"
					+ "		e.EVALUATION_FIELD,\r\n"
					+ "		e.EVALUATION_ASSIGNED_at,\r\n"
					+ "		e.EVALUATION_SCORE,\r\n"
					+ "		e.EVALUATION_STATUS_CD,\r\n"
					+ "		e.EVALUATION_IS,\r\n"
					+ "		a.APPLICATION_ID,\r\n"
					+ "		a.APPLICATION_BUDGET_AMT,\r\n"
					+ "		a.APPLICATION_USER_ID,\r\n"
					+ "        a.APPLICATION_ATTACH_PATH,\r\n"
					+ "        ag.AGENCY_AGY_NAME,\r\n"
					+ "		an.ANNOUNCEMENT_START_DT,\r\n"
					+ "        an.ANNOUNCEMENT_END_DT,\r\n"
					+ "		an.ANNOUNCEMENT_TITLE,\r\n"
					+ "		an.ANNOUNCEMENT_DESC,\r\n"
					+ "        an.ANNOUNCEMENT_TOTAL_BUDGET\r\n"
					+ "FROM EVALUATIONS e\r\n"
					+ "JOIN APPLICATIONS a \r\n"
					+ "ON e.EVALUATION_APPLICATION_ID = a.APPLICATION_ID\r\n"
					+ "JOIN ANNOUNCEMENT an\r\n"
					+ "ON a.APPLICATION_ANN_ID = an.ANNOUNCEMENT_ann_ID\r\n"
					+ "JOIN AGENCY ag\r\n"
					+ "ON an.ANNOUNCEMENT_AGY_ID = ag.AGENCY_AGY_ID\r\n"
					+ "WHERE e.EVALUATION_REVIEWER_ID = ?";
			pstmt = conn.prepareStatement(sql);
			System.out.println("현재 로그인 ID = [" + cust_id + "]");
			System.out.println("현재 로그인 FIELD = [" + field + "]");
			pstmt.setString(1, cust_id);
			//pstmt.setString(2, field);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				//내용들 대충
				do {
					eval_id = rs.getInt("EVALUATION_ID");						//평가번호
					eval_app_id = rs.getInt("EVALUATION_APPLICATION_ID");		//신청번호
					eval_rev_id = rs.getString("EVALUATION_REVIEWER_ID");	//평가위원ID
					eval_field = rs.getString("EVALUATION_FIELD");			//평가분야
					eval_assigned_dt = rs.getDate("EVALUATION_ASSIGNED_AT");	//평가배정일
					eval_score = rs.getInt("EVALUATION_SCORE");	
					eval_comment = rs.getString("EVALUATION_IS");			//평가 코멘트//평가 점수
					eval_status = rs.getString("EVALUATION_STATUS_CD");		//평가 상태
					
					ann_start_dt = rs.getDate("ANNOUNCEMENT_START_DT");		//공고(평가)시작일
					ann_end_dt = rs.getDate("ANNOUNCEMENT_END_DT");			//공고(평가)마감일			
					ann_title = rs.getString("ANNOUNCEMENT_TITLE");			//공고명
					ann_desc = rs.getString("ANNOUNCEMENT_DESC");			//공고 설명
					ann_budget = rs.getString("ANNOUNCEMENT_TOTAL_BUDGET");	//총 예산
					
					app_budget_amt = rs.getInt("APPLICATION_BUDGET_AMT");		//신청자의 요구 예산
					app_user_id = rs.getString("APPLICATION_USER_ID");		//신청자 아이디
					app_attach = rs.getString("APPLICATION_ATTACH_PATH");	//신청자 첨부파일
					
					ag_name = rs.getString("AGENCY_AGY_NAME");				//기관명
					
					if(eval_comment == null) {
						eval_comment = "";
					}
					System.out.println("");
					System.out.print("평가번호\t");
					System.out.print("과제명\t\t\t");
					System.out.print("신청자\t");
					System.out.print("기관명\t\t");
					System.out.print("분야\t");
					System.out.print("평가 상태\t\t");
					System.out.print("평가 마감일\t");
					System.out.print("연구 수행 기간\n");
					System.out.println("=".repeat(100));
					System.out.print(eval_id+"\t");
					System.out.print(ann_title+"\t");
					System.out.print(app_user_id+"\t");
					System.out.print(ag_name+"\t\t");
					System.out.print(eval_field+"\t");
					System.out.print(eval_status+"\t");
					System.out.print(ann_end_dt+"\t");
					System.out.print(ann_start_dt+"-"+ann_end_dt+"\n");
				}
				while(rs.next());
			}else {
				System.out.println("┌───────────────────────────────┐");
				System.out.println("│평가할 대상 목록이 없습니다.		│");
				System.out.println("└───────────────────────────────┘");
			}
			System.out.println();
			System.out.println();
			
		}
		catch(Exception e){e.printStackTrace();}
		finally {DBUtil.executeClose(rs, pstmt, conn);}
	}
	
	//상세 조회 메서드 (평가 테이블에서 보고 싶은 값을 인자로 받아온다.)
	public void viewAppDetail(String eval_no) 
	{
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		ResultSet rs = null;
		//DB 행값을 담을 변수를 선언
		int eval_id=0;				//평가번호
		int eval_app_id=0	;	//신청번호
		String eval_rev_id="";	//평가위원ID
		String eval_field="";			//평가분야
		Date eval_assigned_dt =null;	//평가배정일
		int eval_score=0;	
		String eval_comment="";			//평가 코멘트//평가 점수
		String eval_status="";		//평가 상태
		
		Date ann_start_dt =null;		//공고(평가)시작일
		Date ann_end_dt =null;			//공고(평가)마감일			
		String ann_title="";			//공고명
		String ann_desc="";			//공고 설명
		String ann_budget="";	//총 예산
		
		int app_budget_amt=0;		//신청자의 요구 예산
		String app_user_id="";		//신청자 아이디
		String app_attach="";	//신청자 첨부파일
		
		String ag_name="";		//기관명
		
		try {
			conn = DBUtil.getConnection();
			sql = "SELECT  e.EVALUATION_ID,\r\n"
					+ "		e.EVALUATION_APPLICATION_ID,\r\n"
					+ "		e.EVALUATION_REVIEWER_ID,\r\n"
					+ "		e.EVALUATION_FIELD,\r\n"
					+ "		e.EVALUATION_ASSIGNED_at,\r\n"
					+ "		e.EVALUATION_SCORE,\r\n"
					+ "		e.EVALUATION_STATUS_CD,\r\n"
					+ "		e.EVALUATION_IS,\r\n"
					+ "		a.APPLICATION_ID,\r\n"
					+ "		a.APPLICATION_BUDGET_AMT,\r\n"
					+ "		a.APPLICATION_USER_ID,\r\n"
					+ "        a.APPLICATION_ATTACH_PATH,\r\n"
					+ "        ag.AGENCY_AGY_NAME,\r\n"
					+ "		an.ANNOUNCEMENT_START_DT,\r\n"
					+ "        an.ANNOUNCEMENT_END_DT,\r\n"
					+ "		an.ANNOUNCEMENT_TITLE,\r\n"
					+ "		an.ANNOUNCEMENT_DESC,\r\n"
					+ "        an.ANNOUNCEMENT_TOTAL_BUDGET\r\n"
					+ "FROM EVALUATIONS e\r\n"
					+ "JOIN APPLICATIONS a \r\n"
					+ "ON e.EVALUATION_APPLICATION_ID = a.APPLICATION_ID\r\n"
					+ "JOIN ANNOUNCEMENT an\r\n"
					+ "ON a.APPLICATION_ANN_ID = an.ANNOUNCEMENT_ann_ID\r\n"
					+ "JOIN AGENCY ag\r\n"
					+ "ON an.ANNOUNCEMENT_AGY_ID = ag.AGENCY_AGY_ID\r\n"
					+ "WHERE e.EVALUATION_ID = ? AND e.EVALUATION_REVIEWER_ID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, eval_no);
			pstmt.setString(2, cust_id);
			//pstmt.setString(2, field);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				//내용들 대충
				
					eval_id = rs.getInt("EVALUATION_ID");						//평가번호
					eval_app_id = rs.getInt("EVALUATION_APPLICATION_ID");		//신청번호
					eval_rev_id = rs.getString("EVALUATION_REVIEWER_ID");	//평가위원ID
					eval_field = rs.getString("EVALUATION_FIELD");			//평가분야
					eval_assigned_dt = rs.getDate("EVALUATION_ASSIGNED_AT");	//평가배정일
					eval_score = rs.getInt("EVALUATION_SCORE");	
					eval_comment = rs.getString("EVALUATION_IS");			//평가 코멘트//평가 점수
					eval_status = rs.getString("EVALUATION_STATUS_CD");		//평가 상태
					
					ann_start_dt = rs.getDate("ANNOUNCEMENT_START_DT");		//공고(평가)시작일
					ann_end_dt = rs.getDate("ANNOUNCEMENT_END_DT");			//공고(평가)마감일			
					ann_title = rs.getString("ANNOUNCEMENT_TITLE");			//공고명
					ann_desc = rs.getString("ANNOUNCEMENT_DESC");			//공고 설명
					ann_budget = rs.getString("ANNOUNCEMENT_TOTAL_BUDGET");	//총 예산
					
					app_budget_amt = rs.getInt("APPLICATION_BUDGET_AMT");		//신청자의 요구 예산
					app_user_id = rs.getString("APPLICATION_USER_ID");		//신청자 아이디
					app_attach = rs.getString("APPLICATION_ATTACH_PATH");	//신청자 첨부파일
					
					ag_name = rs.getString("AGENCY_AGY_NAME");				//기관명
					
					if(eval_comment == null) {
						eval_comment = "";
					}
					System.out.println("");
					System.out.print("평가번호\t");
					System.out.print("과제명\t\t\t");
					System.out.print("신청자\t");
					System.out.print("기관명\t\t");
					System.out.print("분야\t");
					System.out.print("평가 상태\t\t");
					System.out.print("평가 마감일\t");
					System.out.print("연구 수행 기간\n");
					System.out.println("=".repeat(100));
					System.out.print(eval_id+"\t");
					System.out.print(ann_title+"\t");
					System.out.print(app_user_id+"\t");
					System.out.print(ag_name+"\t\t");
					System.out.print(eval_field+"\t");
					System.out.print(eval_status+"\t");
					System.out.print(ann_end_dt+"\t");
					System.out.print(ann_start_dt+"-"+ann_end_dt+"\n");
					System.out.println(eval_rev_id);
			}else {
				System.out.println("┌────────────────────────────────────┐");
				System.out.println("│본인의 평가목록에 존재하는 번호를 입력하세요! │");
				System.out.println("└────────────────────────────────────┘");
				System.out.println();
				readEval();
				return;
			}
			System.out.println();
			System.out.println();
			System.out.println("[1]평가하기");
			System.out.println("[2]나가기");
			int do_eval_no = Integer.parseInt(br.readLine());
			if(do_eval_no == 1) {
				//임시 평가 체크 메서드
				boolean checkYN = checkTempEval(eval_id);
				//checkTempEval메서드에서 임시평가를 사용하면 checkYN에 True값 리턴
				//임시평가 값이 존재하지 않거나 임시평가를 사용하지 않으면 false로 리턴함
				if(!checkYN) {
					//평가하기
					//신청 아이디, 평가위원 아이디, 평가분야를 인자로 사용함
					submiteval(eval_id);
				}else {
					//재평가 값을 그대로 사용하는 메서드 작성
					//submiteval();
				}
				//평가 메서드
			}else if(do_eval_no == 2) {
				//
				System.out.println();
				System.out.println("메인메뉴로 돌아갑니다!");
				System.out.println();
				
			}
		}
		catch(Exception e){e.printStackTrace();}
		finally {DBUtil.executeClose(rs, pstmt, conn);}	
	}
	//신청하기 메서드
	//해당 평가를 작성하기 위해 필요한 평가 번호를 가져온다.
	/*
	 * 
	 * 
	 * 
	 * 03 03 오후 시간에 여기 부터 시작해야함!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	public void submiteval(int eval_id) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		
		try {
			//점수, 평가 부분
			int score = checkScore("점수 :"); //점수가 0~100 으로만 입력하게 해주는 메서드
			System.out.println("코멘트 작성");
			String comment = br.readLine();
			
			//DB연결 부분
			conn = DBUtil.getConnection();
			sql = "update evaluations set EVALUATION_SCORE = ?, EVALUATION_IS = ?, EVALUATION_STATUS_CD = ? where EVALUATION_ID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, score);			//점수 저장
			pstmt.setString(2, comment);	//평가코멘트 저장
			pstmt.setInt(4, eval_id);		//업데이트에 사용할 평가 번호 삽입
			System.out.println();
			System.out.println();
			System.out.println("[1]평가작성");
			System.out.println("[2]나가기");
			int save_eval_no = Integer.parseInt(br.readLine());
			if(save_eval_no == 1) {
				String submit = "SUBMITTED";
				pstmt.setString(3, submit);
				int cnt = pstmt.executeUpdate();
				System.out.println(cnt + "건의 평가를 작성 완료 하였습니다");
			}else if(save_eval_no == 2) {
				//
				System.out.println("작성한 평가를 임시저장 하시겠습니까?");
				System.out.print("[1] 저장한다.  [2] 나가기 >>");
				int saveTemp = Integer.parseInt(br.readLine());
				if(saveTemp == 1) {
					//임시저장하는 메서드
				}
				if(saveTemp == 2) {
					//그냥 나감(평가목록으로 나가기)
				}
				else {
					System.out.println("1 혹은 2를 입력 해주세요.");
				}
			}
		}
		catch(Exception e){e.printStackTrace();}
		finally {DBUtil.executeClose(null, pstmt, conn);}	
	}
	
	//임시평가 내용을 그대로 사용하는 메서드
	public void submiteval() {
		//임시평가를 사용하는 메서드를 작성한다.
	}
	
	//평가하기에서 점수를 올바르게 작성했는지 검사하는 메서드
	public int checkScore(String scoreInfo) {
		int score;
		while(true) {
			System.out.print(scoreInfo);	
			try {
				score = Integer.parseInt(br.readLine());
				if(score > 100 || score < 0) {// 점수가 100 초과 혹은 0 미만인 경우
					continue;
				}else {
					break;
				}
			}
			catch(IOException e) {e.printStackTrace();}
			catch(NumberFormatException e) {System.out.println("숫자를 입력해주세요.");}
		}
		return score;
	}
	
	
	//재평가 메서드
	
	//임시평가 체크 메서드
	public boolean checkTempEval(int eval_id) {
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		ResultSet rs = null;
		boolean doYN = false;
		
		try {
			conn = DBUtil.getConnection();
			//sql은 해당 평가번호를 임시평가 테이블에 검색한다
			sql = "select * from TEMP_EVAL WHERE TEMP_EVAL_EVALUATION_ID"
					+ "= ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, eval_id);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				//검색 결과가 있다는것 = 임시저장을 한 경우
				System.out.println("해당 과제에 대해 임시평가 기록이 존재합니다 해당 평가를 그대로 평가에 사용하시겠습니까?");
				System.out.println("[1] 사용한다.  [2]사용하지 않는다.(임시평가 내용이 지워집니다!)");
				System.out.print(">");
				int temp_choose = Integer.parseInt(br.readLine());
				if(temp_choose == 1) {
					//임시평가에 들어간 내용을 그대로 평가에 사용하는 메서드 작성
					//method1();
					doYN = true;
				}
				else if(temp_choose == 2) {
					//임시평가에 해당 행을 삭제하고 평가하기 메서드를 실행하는 메서드 작성
					sql = "delete from TEMP_EVAL where TEMP_EVAL_EVALUATION_ID = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setInt(1, eval_id);
					int delcnt = pstmt.executeUpdate();
					System.out.println(delcnt + "건의 임시평가를 제거 하였습니다.");
				}
				else {
					System.out.println("1 또는 2를 입력하여 주세요.");
				}
				
			}else {
				System.out.println("임시평가 테이블을 탐색중 SQL에러 발생하였습니다.");
			}
			
		}
		catch(Exception e){e.printStackTrace();}
		finally {DBUtil.executeClose(rs, pstmt, conn);}	
		return doYN;
	}
	
	//임시저장 메서드
	
	//내 정보 보기 메서드
	
	
	//평가목록 화면 메서드
	public void callReviewerMenu(String myCust_id, String myRole, String myField) {
		cust_id = myCust_id; //UserMain에서 가져온 사용자 ID를 MemberDAO에 있는 cust_id로 삽입
		role = myRole;	//UserMain에서 가져온 사용자의 권한을 role에 삽입
		field = myField;
		System.out.println("전달된 cust_id = [" + cust_id + "]");
		while(true) {
			System.out.println("┌────────────────────────────────────────────────────────┐");
			System.out.println("│							 │");
			System.out.println("│	국가 연구과제 관리 프로그램	「KRD Hubs」		 │");
			System.out.println("│							 │");
			System.out.println("│	1. 평가배정목록조회					 │");
			System.out.println("│	2. 평가기록조회					 │");
			System.out.println("│	3. 내정보						 │");
			System.out.println("│	4. 로그아웃					 │");
			System.out.println("│	5. 종료						 │");
			System.out.println("│							 │");
			System.out.println("│등급 : 평가위원					ver.1.0	 │");
			System.out.println("└────────────────────────────────────────────────────────┘");
			System.out.println("［원하시는 메뉴를 선택하세요 ]");
			System.out.print(">>");
			try {
				int rev_choose = Integer.parseInt(br.readLine());
				if(rev_choose == 1) {
					//평가배정목록조회
					readEval(); // 평가배정목록 조회
					System.out.println("[1]상세보기");
					System.out.println("[2]나가기");
					int eval_no = Integer.parseInt(br.readLine());
					if(eval_no == 1) {
						System.out.println();
						System.out.println("상세 보기할 대상의 번호를 입력 하세요.");
						String do_eval_no = br.readLine();
						//상세 보기 메서드
						viewAppDetail(do_eval_no);
					}else if(eval_no == 2) {
						//
						System.out.println("목록에서 메뉴로");
						
					}
				}else if(rev_choose == 2) {
					//평가기록조회
				}else if(rev_choose == 3) {
					//내정보
				}else if(rev_choose == 4) {
					//로그아웃
					return;
				}else if(rev_choose == 5) {
					System.out.println("프로그램 종료");
					System.exit(0);
				}
			}
			catch(Exception e) {e.printStackTrace();}
			//finally {if(br != null)try{br.close();}catch(IOException e) {}}
		}
	}
	
	//로그아웃 메서드
	public boolean logout() {
		return false;
	}
}
