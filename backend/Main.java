import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        AppointmentService service = new AppointmentService();

        Doctor doctor1 = new Doctor(
                1, "Dr. Rahul Sharma",
                "Cardiologist", 10, 500
        );

        Doctor doctor2 = new Doctor(
                2, "Dr. Priya Nair",
                "Dermatologist", 8, 400
        );

        int choice;

        do {
            System.out.println("\n===== MEDIMEET =====");
            System.out.println("1. View Doctors");
            System.out.println("2. Book Appointment");
            System.out.println("3. View Appointments");
            System.out.println("4. Cancel Appointment");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");

            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {

                case 1:
                    doctor1.displayDetails();
                    doctor2.displayDetails();
                    break;

                case 2:
                    System.out.print("Enter Patient Name: ");
                    String name = sc.nextLine();

                    System.out.print("Enter Patient Age: ");
                    int age = sc.nextInt();
                    sc.nextLine();

                    System.out.print("Enter Phone Number: ");
                    String phone = sc.nextLine();

                    System.out.println("Select Doctor:");
                    System.out.println("1. Dr. Rahul Sharma");
                    System.out.println("2. Dr. Priya Nair");
                    System.out.print("Enter doctor choice: ");

                    int doctorChoice = sc.nextInt();
                    sc.nextLine();

                    Doctor selectedDoctor;

                    if (doctorChoice == 1) {
                        selectedDoctor = doctor1;
                    } else if (doctorChoice == 2) {
                        selectedDoctor = doctor2;
                    } else {
                        System.out.println("Invalid doctor choice.");
                        break;
                    }

                    System.out.print("Enter Appointment Date (YYYY-MM-DD): ");
                    String date = sc.nextLine();

                    System.out.print("Enter Appointment Time (e.g. 10:30 AM): ");
                    String time = sc.nextLine();

                    System.out.print("Enter Appointment ID: ");
                    int appointmentId = sc.nextInt();
                    sc.nextLine();

                    Appointment appointment = new Appointment(
                            appointmentId,
                            name,
                            age,
                            phone,
                            selectedDoctor,
                            date,
                            time
                    );

                    service.bookAppointment(appointment);
                    break;

                case 3:
                    service.viewAppointments();
                    break;

                case 4:
                    System.out.print("Enter Appointment ID to cancel: ");
                    int cancelId = sc.nextInt();
                    sc.nextLine();

                    service.cancelAppointment(cancelId);
                    break;

                case 5:
                    System.out.println("Thank you for using MediMeet!");
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }

        } while (choice != 5);

        sc.close();
    }
}