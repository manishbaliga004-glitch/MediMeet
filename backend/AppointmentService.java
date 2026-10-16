import java.util.ArrayList;
import java.util.List;

public class AppointmentService {

    private List<Appointment> appointments;

    // Constructor
    public AppointmentService() {
        appointments = new ArrayList<>();
    }

    // Book an appointment
    public boolean bookAppointment(Appointment appointment) {

        if (appointment == null) {
            System.out.println("Invalid appointment details.");
            return false;
        }

        // Check whether the doctor's time slot is already booked
        for (Appointment existing : appointments) {

            boolean sameDoctor =
                    existing.getDoctor().getId()
                    == appointment.getDoctor().getId();

            boolean sameDate =
                    existing.getAppointmentDate()
                    .equals(appointment.getAppointmentDate());

            boolean sameTime =
                    existing.getAppointmentTime()
                    .equals(appointment.getAppointmentTime());

            if (sameDoctor && sameDate && sameTime) {
                System.out.println(
                    "This doctor's time slot is already booked."
                );
                return false;
            }
        }

        appointments.add(appointment);

        System.out.println("Appointment booked successfully!");

        return true;
    }

    // Display all appointments
    public void viewAppointments() {

        if (appointments.isEmpty()) {
            System.out.println("No appointments found.");
            return;
        }

        System.out.println("\n===== ALL APPOINTMENTS =====");

        for (Appointment appointment : appointments) {
            appointment.displayAppointment();
        }
    }

    // Cancel an appointment
    public boolean cancelAppointment(int appointmentId) {

        for (Appointment appointment : appointments) {

            if (appointment.getAppointmentId() == appointmentId) {

                appointments.remove(appointment);

                System.out.println(
                    "Appointment cancelled successfully."
                );

                return true;
            }
        }

        System.out.println("Appointment ID not found.");

        return false;
    }

    // Get total number of appointments
    public int getAppointmentCount() {
        return appointments.size();
    }
}