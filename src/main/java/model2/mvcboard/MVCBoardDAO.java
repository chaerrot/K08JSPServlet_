package model2.mvcboard;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import common.DBConnPool;
import model1.board.BoardDTO;

public class MVCBoardDAO extends DBConnPool {

	public MVCBoardDAO() {
		super();
	}
	
	public int selectCount (Map<String, Object> map) {
		//카운트 변수
		int totalCount = 0;
		//쿼리문 작성
		String query = "SELECT COUNT(*) FROM mvcboard";
		//검색어가 있는 경우 where절을 동적으로 추가한다.
		if (map.get ("searchWord") != null) {
			query += " WHERE " + map.get("searchField") + " "
						+ " LIKE '%" + map.get("searchWord") +"%'";
		}
		
		try {
			//정적쿼리문(?가 없는 쿼리문) 실행을 위한 Statement객체 생성
			stmt = con.createStatement();
			//select 쿼리문을 실행 후 ResultSet 객체를 반환 받음
			rs = stmt.executeQuery(query);
			//커서를 이동시켜 결과데이터를 읽음
			rs.next();
			//결과값을 변수에 저장
			totalCount = rs.getInt(1);
		}
		catch (Exception e ) {
			System.out.println("게시물 수를 구하는 중 예외 발생");
			e.printStackTrace();
		}
		
		return totalCount;
	}
	
	public List <MVCBoardDTO> selectListPage(Map<String, Object> map) {
		/*
		 board테이블에서 select한 결과데이터를 저장하기 위한 리스트 컬렉션.
		 여러 가지의 List컬렉션 중 동기화가 보장되는 Vector를 사용한다.
		 */
		List<MVCBoardDTO> board = new Vector<MVCBoardDTO>();
		
		/*
		 목록에 출력할 게시물을 추출하기 위한 쿼리문으로 항상 일련번호의
		 역순(내림차순)으로 정렬해야 한다. 게시판의 목록은 최근 게시물이
		 제일 앞에 노출되기 때문이다.
		 */
		String query = " "
				+ "SELECT * FROM ( "
				+ "		SELECT Tb.*, ROWNUM rNum FROM ( "
				+ "			SELECT * FROM mvcboard ";
		if (map.get("searchWord") != null) {
			query += " WHERE " + map.get("searchField") +" "
						+ " LIKE '%" + map.get("searchWord") +"%' ";
		}
		query += " 			ORDER BY idx DESC "
				+ "		) Tb "
				+ " ) "
				+ " WHERE rNum BETWEEN ? AND ?";
		
		try {
			psmt = con.prepareStatement(query);
			psmt.setString(1, map.get("start").toString());
			psmt.setString(2, map.get("end").toString());
			rs = psmt.executeQuery();
			
			//추출된 결과에 따라 반복한다.
			while (rs.next()) {
				//하나의 레코드를 읽어서 DTO객체에 저장한다.
				MVCBoardDTO dto = new MVCBoardDTO();
				
				dto.setIdx(rs.getString(1));
				dto.setName(rs.getString(2));
				dto.setTitle(rs.getString(3));
				dto.setContent(rs.getString(4));
				dto.setPostdate(rs.getDate(5));
				dto.setOfile(rs.getString(6));
				dto.setSfile(rs.getString(7));
				dto.setDowncount(rs.getInt(8));
				dto.setPass(rs.getString(9));
				dto.setVisitcount(rs.getInt(10));
				
				//리스트 컬렉션에 DTO객체를 추가한다.
				board.add(dto);
			}
		}
		catch (Exception e) {
			System.out.println("게시물 조회 중 예외 발생");
			e.printStackTrace();
		}
		
		return board;
	}
	
	//새로운 게시물에 대한 입력 처리
	public int insertWrite(MVCBoardDTO dto) {
		int result = 0;
		try {
			String query = "INSERT INTO mvcboard ( "
						+ " idx, name, title, content, ofile, sfile, pass) "
						+ " VALUES ( "
						+ " seq_board_num.NEXTVAL, ?, ?, ?, ?, ?, ?)";
			psmt = con.prepareStatement(query);
			psmt.setString(1, dto.getName());
			psmt.setString(2, dto.getTitle());
			psmt.setString(3, dto.getContent());
			psmt.setString(4, dto.getOfile());
			psmt.setString(5, dto.getSfile());
			psmt.setString(6, dto.getPass());
			result = psmt.executeUpdate();
		}
		catch (Exception e) {
			System.out.println("게시물 입력 중 예외 발생");
			e.printStackTrace();
		}
		
		return result;
	}
	
}
