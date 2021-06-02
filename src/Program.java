import org.w3c.dom.*;

import javax.xml.parsers.*;
import java.io.File;
import java.io.IOException;
import java.sql.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.SAXException;

import java.io.File;
import java.util.Scanner;


public class Program {
    public static Connection connection;
    public static String URL = "jdbc:mysql://localhost:3306/lab5";
    public static String root = "root";
    public static String password = "12345";

    public static void main(String[] args) throws SQLException {
        Connection conn = DriverManager.getConnection(URL, root, password);
        //zapovn(conn);
        int comand;
        System.out.println("1 - Вивести вмість таблиці\n2 - Видалити поле з таблиці за індексом\n3 - Виводить точку в координатах з діапазоном(вже вказаний)");
        System.out.println("4 - Очищує таблицю\n5 - Змінює поле з інформацією, по індексу");
        System.out.println("6 - Перезаписує таблицю з XML файлу(Обережно, всі змінені користувачем значення, будуть перезаписані\n7 - Вихід з програми");

        do {
            Scanner sc = new Scanner(System.in);
            System.out.print("Виберіть команду: ");
            comand = sc.nextInt();
            switch (comand) {
                case 1:
                    see(conn);
                    break;
                case 2:
                    System.out.println("Введіть індекс, поле якого бажаєте видалити: ");
                    Scanner s = new Scanner(System.in);
                    delete(s.nextInt(), conn);
                    break;
                case 3:
                    coords(conn);
                    break;
                case 4:
                    cleartable(conn);
                    break;
                case 5:
                    System.out.println("Введіть індекс, поле якого бажаєте змінити: ");
                    Scanner c = new Scanner(System.in);
                    System.out.println("Введіть текст, який хочете змінити в цьому полі info");
                    Scanner st = new Scanner(System.in);
                    change(c.nextInt(), st.nextLine(), conn);
                    break;
                case 6:
                    zapovn(conn);
                    System.out.println("Таблицю було записано");
                    break;
                case 7:
                    conn.close();
                    break;
            }
        } while (comand != 7);

    }

    public static void zapovn(Connection conn) {
        try {

            File file = new File("output.xml");
            DocumentBuilderFactory dbFact = DocumentBuilderFactory.newInstance();
            DocumentBuilder dbuilder = dbFact.newDocumentBuilder();
            Document xml = dbuilder.parse(file);
            xml.getDocumentElement().normalize();

            NodeList ndesc = xml.getDocumentElement().getElementsByTagName("desc");
            NodeList nlink = xml.getDocumentElement().getElementsByTagName("link");
            NodeList nwpt = xml.getDocumentElement().getElementsByTagName("wpt");

            String sometext = "";
            String somedescription = "";
            double lon = 0;
            double lat = 0;
            int listlenght = ndesc.getLength();
            conn.createStatement().executeUpdate("TRUNCATE TABLE lab5info");
            for (int i = 0; i < listlenght; i++) {
                Node nNode = ndesc.item(i);
                sometext = nNode.getTextContent();

                Node link = nlink.item(i);
                NamedNodeMap attributes = link.getAttributes();
                Node attr = attributes.item(0);
                somedescription = attr.getTextContent();

                Node wpt = nwpt.item(i);
                NamedNodeMap wptatributes = wpt.getAttributes();
                Node wptattr = wptatributes.item(0);
                lat = Double.parseDouble(wptattr.getTextContent());

                Node wptforlon = nwpt.item(i);
                NamedNodeMap wptforlon_atributes = wptforlon.getAttributes();
                Node wptlonattr = wptforlon_atributes.item(1);
                lon = Double.parseDouble(wptlonattr.getTextContent());

                String query = "insert into lab5info (info,link,lat,lon)" + "values(?,?,?,?)";
                PreparedStatement prepareStatement = conn.prepareStatement(query);
                prepareStatement.setString(1, sometext);
                prepareStatement.setString(2, somedescription);
                prepareStatement.setDouble(3, lat);
                prepareStatement.setDouble(4, lon);
                prepareStatement.execute();
            }
        } catch (ParserConfigurationException | SAXException | IOException | SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static void see(Connection connection) throws SQLException {
        //Connection conn = DriverManager.getConnection(URL, root, password);
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM lab5info");
        System.out.println("------------------------------------------------------------------------------------");
        while (rs.next()) {
            System.out.println(rs.getInt(1) + "\t" + rs.getString(2) + "\t" + rs.getString(3) + "\t" + rs.getDouble(4) + "\t" + rs.getDouble(5));
        }
        System.out.println("------------------------------------------------------------------------------------");
        //connection.close();
    }

    public static void delete(int numb, Connection connection) throws SQLException {
        Statement st = connection.createStatement();
        // ResultSet rs = st.executeUpdate("DELETE * FROM lab5info WHERE (id) = " + numb);
        int rows = st.executeUpdate("DELETE  FROM lab5info WHERE id = " + numb);
        //connection.close();
    }

    public static void coords(Connection connection) throws SQLException {
        Statement st = connection.createStatement();
        System.out.println("Координати по ширині між 47.74 і 47.75, по довжині між 25.07 і 26");
        ResultSet rs = st.executeQuery("SELECT * FROM lab5info WHERE (lat BETWEEN 47.74 AND 47.75) and lon between 25.07 and 26");
        while (rs.next()) {
            System.out.println(rs.getInt(1) + "\t" + rs.getString(2) + "\t" + rs.getString(3) + "\t" + rs.getDouble(4) + "\t" + rs.getDouble(5));
        }
    }

    public static void cleartable(Connection connection) throws SQLException {
        connection.createStatement().executeUpdate("TRUNCATE TABLE lab5info");
        System.out.println("Таблицю було очищено");
    }

    // https://alvinalexander.com/java/java-mysql-update-query-example/
    public static void change(int numb, String sometext, Connection connection) throws SQLException {
        String query = "UPDATE lab5info SET info = ? WHERE id = ?";
        PreparedStatement stmnt = connection.prepareStatement(query);
        stmnt.setString(1, sometext);
        stmnt.setInt(2, numb);
        stmnt.executeUpdate();
        System.out.println("Значення було змінене");

    }
}
