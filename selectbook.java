import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import redis.clients.jedis.Jedis;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

@WebServlet(urlPatterns = "/selectbook")
public class selectbook extends HttpServlet {
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://152.136.210.168/b_book";
    static final String USER = "root";
    static final String PASS = "WODEQQshi@255632";
    static final String SQL_SELECT_BOOK_BY_ID = "SELECT bookid, bookname, bookage FROM t_book WHERE bookid=?";
    static final String REDIS_URL = "152.136.210.168";

    static Connection conn = null;
    static Jedis jedis = null;

    public void init() {
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            jedis = new Jedis(REDIS_URL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        try {
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        getServletContext().log(request.getParameter("bookid"));

        String json = jedis.get(request.getParameter("bookid"));

        if (json == null) {
            book stu = getbook(Integer.parseInt(request.getParameter("bookid")));

            Gson gson = new Gson();
            json = gson.toJson(stu, new TypeToken<book>() {
            }.getType());

            jedis.set(request.getParameter("bookid"), json);
            out.println(json);

        } else {
            out.println(json);
        }
        out.flush();
        out.close();
    }

    public book getbook(int bookid) {
        book stu = new book();
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(SQL_SELECT_BOOK_BY_ID);
            stmt.setInt(1, bookid);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                stu.bookid = rs.getInt("bookid");
                stu.bookname = rs.getString("bookname");
                stu.bookage = rs.getString("bookage");     
        }

            rs.close();
            stmt.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

        return stu;

    }

}   

