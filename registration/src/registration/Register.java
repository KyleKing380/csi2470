package registration;
import java.sql.*;
import java.util.Scanner;
public class Register {

	public static void main(String[] args) {
		int loopcheck = 0;
		do {
		try{  
			//connect to local database named messengerdata, on port 3307 using credentials "root" as username and "password" as password
			Class.forName("com.mysql.cj.jdbc.Driver");  
			Connection con=DriverManager.getConnection(  
			"jdbc:mysql://localhost:3307/messengerdata","root","password");  
			Statement stmt=con.createStatement();
			
			//this next block gets input and stores them into strings. print statements for debugging
			Scanner userInput = new Scanner(System.in);
			System.out.println("Enter Username");
			String userName = userInput.nextLine();
			System.out.println(userName);
			System.out.println("Enter password");
			String password = userInput.nextLine();
			System.out.println(password);
			System.out.println("Below is the table");
			
			//checking the database to see if the username is already being used
			String dbResult = null;
			ResultSet usercheck=stmt.executeQuery("select username from userpass where username='" + userName + "'");  
			while(usercheck.next()) 
			dbResult = usercheck.getString(1);
			System.out.println(dbResult);
			
				if (userName.equals(dbResult)) {
					System.out.println("Username already exists!");
					continue;
				}
				else {
					//adds user data into the database
				     String query = " insert into userpass (username, password)"
				        + " values (?, ?)";
				      PreparedStatement preparedStmt = con.prepareStatement(query);
				      preparedStmt.setString (1, userName);
				      preparedStmt.setString (2, password);
				      preparedStmt.execute();
				}
			
		    //prints out table contents for debugging
			ResultSet rs=stmt.executeQuery("select * from userpass");  
			while(rs.next())  
			System.out.println(rs.getString(1)+"  "+rs.getString(2));
		}	catch(Exception e){ System.out.println(e);}  
		loopcheck = 1;
	} while (loopcheck == 0); 
	} 

}
