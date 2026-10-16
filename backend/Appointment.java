public class Appointment {

    private long appointmentId;
    private String patientName;
    private int patientAge;
    private String phoneNumber;
    private Doctor doctor;
    private String appointmentDate;
    private String appointmentTime;

    // Constructor
    public Appointment(long appointmentId,
                       String patientName,
                       int patientAge,
                       String phoneNumber,
                       Doctor doctor,
                       String appointmentDate,
                       String appointmentTime) {

        this.appointmentId = appointmentId;
        this.patientName = patientName;
        this.patientAge = patientAge;
        this.phoneNumber = phoneNumber;
        this.doctor = doctor;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
    }

    // Get appointment ID
    public long getAppointmentId() {
        return appointmentId;
    }

    // Get patient name
    public String getPatientName() {
        return patientName;
    }

    // Get patient age
    public int getPatientAge() {
        return patientAge;
    }

    // Get phone number
    public String getPhoneNumber() {
        return phoneNumber;
    }

    // Get doctor
    public Doctor getDoctor() {
        return doctor;
    }

    // Get appointment date
    public String getAppointmentDate() {
        return appointmentDate;
    }

    // Get appointment time
    public String getAppointmentTime() {
        return appointmentTime;
    }

    // Display appointment details
    public void displayAppointment() {

        System.out.println("Appointment ID: " + appointmentId);
        System.out.println("Patient Name: " + patientName);
        System.out.println("Patient Age: " + patientAge);
        System.out.println("Phone Number: " + phoneNumber);
        System.out.println("Doctor Name: " + doctor.getName());
        System.out.println("Specialization: " +
                           doctor.getSpecialization());
        System.out.println("Appointment Date: " + appointmentDate);
        System.out.println("Appointment Time: " + appointmentTime);

        System.out.println("----------------------------------");
    }
}