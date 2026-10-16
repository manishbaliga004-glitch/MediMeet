public class Doctor {

    private int id;
    private String name;
    private String specialization;
    private int experience;
    private double consultationFee;

    // Constructor
    public Doctor(int id, String name, String specialization,
                  int experience, double consultationFee) {

        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.experience = experience;
        this.consultationFee = consultationFee;
    }

    // Get doctor ID
    public int getId() {
        return id;
    }

    // Get doctor name
    public String getName() {
        return name;
    }

    // Get specialization
    public String getSpecialization() {
        return specialization;
    }

    // Get experience
    public int getExperience() {
        return experience;
    }

    // Get consultation fee
    public double getConsultationFee() {
        return consultationFee;
    }

    // Update doctor name
    public void setName(String name) {
        this.name = name;
    }

    // Update specialization
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    // Update experience
    public void setExperience(int experience) {
        this.experience = experience;
    }

    // Update consultation fee
    public void setConsultationFee(double consultationFee) {
        this.consultationFee = consultationFee;
    }

    // Display doctor details
    public void displayDetails() {

        System.out.println("Doctor ID: " + id);
        System.out.println("Doctor Name: " + name);
        System.out.println("Specialization: " + specialization);
        System.out.println("Experience: " + experience + " years");
        System.out.println("Consultation Fee: Rs. " + consultationFee);

        System.out.println("------------------------------");
    }
}