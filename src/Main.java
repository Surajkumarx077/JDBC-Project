import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.*;
import java.sql.Statement;
import java.sql.ResultSet;
import java.lang.Thread;


public class Main {
    private static final String url = "jdbc:mysql://localhost:3306/hotel_db";
    private static final String username = "root";
    private static final String password = "Suraj321.S";

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            Scanner scanner = new Scanner(System.in);

            while(true){
                System.out.println();
                System.out.println("HOTEL MANAGEMENT SYSTEM");

                System.out.println("1. Reserve a room");
                System.out.println("2. View Reservation");
                System.out.println("3. Get Room Number");
                System.out.println("4. Update Reservation");
                System.out.println("5. Delete Reservation");
                System.out.println("0. Exit");
                System.out.println("Choose an option: ");
                int choice = scanner.nextInt();
                switch(choice){
                    case 1:
                        reserveRoom(connection, scanner);
                        break;
                    case 2:
                        viewReservations(connection);
                        break;
                    case 3:
                        getRoomNumber(connection, scanner);
                        break;
                    case 4:
                        updateReservation(connection, scanner);
                        break;
                    case 5:
                        deleteReservation(connection, scanner);
                        break;
                    case 0:
                        exit();
                        scanner.close();
                        break; // Add this break statement
                    default:
                        System.out.println("Invalid choice. Try again.");
                        break; // Add this break statement


                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }
    }
    private static void reserveRoom(Connection connection, Scanner scanner){
        try{
            System.out.println("Enter guest name: ");
            String guestName = scanner.next();

            // Add error handling for room number input
            int roomNumber;
            while (true) {
                System.out.println("Enter room number: ");
                try {
                    roomNumber = scanner.nextInt();
                    break; // Exit the loop if input is valid
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input. Please enter a numeric room number.");
                    scanner.next(); // Clear the invalid input
                }
            }

            System.out.println("Enter contact number: ");
            String contactNumber = scanner.next();

            String sql = "INSERT INTO reservation (guest_name, room_no, contact_no)" +
                    "VALUES ('" + guestName + "', " + roomNumber + ", '" + contactNumber + "')";

            try(Statement statement = connection.createStatement()){
                int affectedRows = statement.executeUpdate(sql);
                if(affectedRows>0){
                    System.out.println("Reservation successful!!");
                }else{
                    System.out.println("Reservation failed.");
                }
            } catch (SQLException e){
                System.out.println(e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Error in reservation process: " + e.getMessage());
            // Don't rethrow as RuntimeException - handle gracefully instead
        }
    }
    private static void viewReservations(Connection connection){
        String sql = "SELECT * fROM reservation";

        try(Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);){
            System.out.println("Current Reservations:");
            while(resultSet.next()){
                int reservationId = resultSet.getInt("reservation_id");
                String guestName = resultSet.getString("guest_name");
                int roomNumber = resultSet.getInt("room_no");
                String contactNumber = resultSet.getString("contact_no");
                String reservationDate = resultSet.getTimestamp("reservation_date").toString();
//Format and display
                System.out.printf("| %14d | %15s | %13d | %20s | %-19s |\n",
                        reservationId, guestName, roomNumber, contactNumber, reservationDate);

            }

        }catch (SQLException e){
            System.out.println(e.getMessage());
        }

    }
    private static void getRoomNumber(Connection connection,Scanner scanner){
        try{
            System.out.println("Enter Reservation Id: ");
            int reservationId = scanner.nextInt();
            System.out.println("Enter guest name: ");
            String guestName = scanner.next();

            String sql = "SELECT room_no FROM reservation "+
                    "WHERE reservation_id = " + reservationId+
                    " AND guest_name = '" + guestName + "'";
            try(Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql)){
                if(resultSet.next()){
                    int roomNumber = resultSet.getInt("room_no");
                    System.out.println("Room number for Reservation Id "+ reservationId+" and Guest " +
                          guestName+  " is: "+ roomNumber);
                }else{
                    System.out.println("Reservation not found for the given Id And guest name.");
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }

    }
    private static void updateReservation(Connection connection,Scanner scanner){
        try {
            System.out.println("Enter reservation Id to update: ");
            int reservationId = scanner.nextInt();
            scanner.nextLine(); // Consume the new line character

            if (!reservationExists(connection, reservationId)) {
                System.out.println("Reservation not found for the given ID.");
                return;
            }
            System.out.println("Enter new guest name: ");
            String newGuestName = scanner.nextLine();
            System.out.println("Enter new room number; ");
            int newRoomNumber = scanner.nextInt();
            System.out.println("Enter new contact number: ");
            String newContactNumber = scanner.next();

            String sql = "UPDATE reservation SET guest_name = '" + newGuestName +
                    "'," + "room_no = " + newRoomNumber + ", " +
                    "contact_no = '" + newContactNumber + "'" +
                    "WHERE reservation_id = " + reservationId;
            try (Statement statement = connection.createStatement()) {
                int affectedRows = statement.executeUpdate(sql);

                if (affectedRows > 0) {
                    System.out.println("Reservation updated successfully!");
                } else {
                    System.out.println("Reservation update failed.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
    private static void deleteReservation(Connection connection,Scanner scanner){
        try{
            System.out.println("Enter your reservation ID to delete : ");
            int reservationId = scanner.nextInt();

            if(!reservationExists(connection,reservationId)){
                System.out.println("Reservation not found for the given ID: ");
                return;
            }
            String sql = "DELETE FROM reservation WHERE reservation_id = "+ reservationId;
            try(Statement statement = connection.createStatement()){
                int affectedRows = statement.executeUpdate(sql);

                if(affectedRows>0){
                    System.out.println("Reservation deleted successfully!");
                } else{
                    System.out.println("Reservation deletion failed.");
                }
            }

        }catch (SQLException e){
            e.printStackTrace();
        }


    }
    private static boolean reservationExists(Connection connection,int resrvationId){
        try{
            String sql = "SELECT reservation_id FROM reservation WHERE reservation_id = " + resrvationId;
            try(Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql)){
                return resultSet.next();//if there exists reservation
            }
            }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }
    private static void exit() throws InterruptedException {
        System.out.println("Exiting System");
        int i = 5;
        while (i != 0) {
            System.out.print(".");
            Thread.sleep(450);
            i--;
        }
        System.out.println();
        System.out.println("ThankYou For Using Hotel Reservation System!!");


    }

}