// ========================================
// MediMeet - Hospital Appointment Booking
// Connected to Java Backend + MySQL
// ========================================

const API_BASE = "http://localhost:8080/api";

let doctors = [];
let appointments = [];

// ========================================
// 1. GET HTML ELEMENTS
// ========================================

const doctorList = document.getElementById("doctor-list");
const doctorSelect = document.getElementById("doctor-select");
const appointmentForm = document.getElementById("appointment-form");
const appointmentList = document.getElementById("appointment-list");
const appointmentDate = document.getElementById("appointment-date");

// ========================================
// 2. DATE AND TIME HELPERS
// ========================================

function setMinimumAppointmentDate() {
    if (!appointmentDate) return;

    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, "0");
    const day = String(today.getDate()).padStart(2, "0");

    appointmentDate.min = `${year}-${month}-${day}`;
}

// Convert 10:00 AM into 10:00
// Convert 1:00 PM into 13:00
function normalizeTime(value) {
    if (!value) return "";

    const text = String(value).trim();

    const twelveHour = text.match(
        /^(\d{1,2}):(\d{2})(?::\d{2})?\s*(AM|PM)$/i
    );

    if (twelveHour) {
        let hours = Number(twelveHour[1]);
        const minutes = twelveHour[2];
        const period = twelveHour[3].toUpperCase();

        if (period === "AM" && hours === 12) {
            hours = 0;
        }

        if (period === "PM" && hours !== 12) {
            hours += 12;
        }

        return String(hours).padStart(2, "0") + ":" + minutes;
    }

    const twentyFourHour = text.match(
        /^(\d{1,2}):(\d{2})(?::\d{2})?$/
    );

    if (twentyFourHour) {
        return (
            String(Number(twentyFourHour[1])).padStart(2, "0") +
            ":" +
            twentyFourHour[2]
        );
    }

    return "";
}

// Display 13:00 as 1:00 PM
function displayTime(value) {
    const time = normalizeTime(value);
    const match = time.match(/^(\d{2}):(\d{2})$/);

    if (!match) return value || "";

    let hours = Number(match[1]);
    const minutes = match[2];
    const period = hours >= 12 ? "PM" : "AM";

    hours = hours % 12 || 12;

    return hours + ":" + minutes + " " + period;
}

function getAppointmentId(appointment) {
    return appointment.id ?? appointment.appointmentId;
}

// ========================================
// 3. LOAD DOCTORS FROM JAVA BACKEND
// ========================================

async function fetchDoctors() {
    try {
        const response = await fetch(`${API_BASE}/doctors`);

        if (!response.ok) {
            throw new Error("Failed to fetch doctors");
        }

        const data = await response.json();

        doctors = data.map(function (doctor) {
            return {
                id: Number(doctor.id),
                name: doctor.name,
                specialization: doctor.specialization,
                experience: doctor.experience,
                fee: doctor.consultationFee ?? doctor.fee
            };
        });

        displayDoctors();
        loadDoctorsIntoDropdown();

        console.log("Doctors loaded from Java backend!");

    } catch (error) {
        console.error("Unable to load doctors:", error);

        if (doctorList) {
            doctorList.textContent =
                "Unable to load doctors. Please make sure the Java server is running.";
        }

        if (doctorSelect) {
            doctorSelect.innerHTML =
                '<option value="">Java server unavailable</option>';
        }
    }
}

// ========================================
// 4. DISPLAY DOCTOR CARDS
// ========================================

function displayDoctors() {
    if (!doctorList) return;

    doctorList.innerHTML = "";

    doctors.forEach(function (doctor) {
        const doctorCard = document.createElement("div");
        doctorCard.className = "doctor-card";

        const name = document.createElement("h3");
        name.textContent = doctor.name;

        const specialization = document.createElement("p");
        specialization.textContent =
            "Specialization: " + doctor.specialization;

        const experience = document.createElement("p");
        experience.textContent =
            "Experience: " + doctor.experience + " years";

        const fee = document.createElement("p");
        fee.textContent = "Consultation Fee: ₹" + doctor.fee;

        const bookButton = document.createElement("button");
        bookButton.className = "btn";
        bookButton.textContent = "Book Appointment";
        bookButton.style.backgroundColor = "#087f8c";
        bookButton.style.color = "white";
        bookButton.style.marginTop = "15px";

        bookButton.addEventListener("click", function () {
            if (doctorSelect) {
                doctorSelect.value = String(doctor.id);
            }

            const appointmentSection =
                document.getElementById("appointment");

            if (appointmentSection) {
                appointmentSection.scrollIntoView({
                    behavior: "smooth"
                });
            }
        });

        doctorCard.append(
            name,
            specialization,
            experience,
            fee,
            bookButton
        );

        doctorList.appendChild(doctorCard);
    });
}

// ========================================
// 5. LOAD DOCTORS INTO DROPDOWN
// ========================================

function loadDoctorsIntoDropdown() {
    if (!doctorSelect) return;

    doctorSelect.innerHTML =
        '<option value="">Select a doctor</option>';

    doctors.forEach(function (doctor) {
        const option = document.createElement("option");

        option.value = doctor.id;
        option.textContent =
            doctor.name + " - " + doctor.specialization;

        doctorSelect.appendChild(option);
    });
}

// ========================================
// 6. LOAD APPOINTMENTS FROM JAVA BACKEND
// ========================================

async function fetchAppointments() {
    try {
        const response = await fetch(`${API_BASE}/appointments`);

        if (!response.ok) {
            throw new Error("Failed to fetch appointments");
        }

        const data = await response.json();

        appointments = data.map(function (appointment) {
            return {
                id: getAppointmentId(appointment),
                patientName: appointment.patientName,
                age: appointment.age ?? appointment.patientAge,
                phone: appointment.phone ?? appointment.phoneNumber,
                doctorId: appointment.doctorId ?? appointment.doctor?.id,
                doctorName:
                    appointment.doctorName ?? appointment.doctor?.name,
                specialization:
                    appointment.specialization ??
                    appointment.doctor?.specialization,
                date: appointment.date ?? appointment.appointmentDate,
                time: appointment.time ?? appointment.appointmentTime,
                fee: appointment.fee ?? appointment.consultationFee
            };
        });

        displayAppointments();

        console.log("Appointments loaded from Java backend!");

    } catch (error) {
        console.error("Unable to load appointments:", error);

        if (appointmentList) {
            appointmentList.textContent =
                "Unable to load appointments. Please make sure the Java server is running.";
        }
    }
}

// ========================================
// 7. DISPLAY APPOINTMENTS
// ========================================

function displayAppointments() {
    if (!appointmentList) return;

    appointmentList.innerHTML = "";

    if (appointments.length === 0) {
        const message = document.createElement("p");
        message.textContent = "No appointments to display yet.";

        appointmentList.appendChild(message);
        return;
    }

    appointments.forEach(function (appointment) {
        const card = document.createElement("div");
        card.className = "appointment-card";

        const heading = document.createElement("h3");
        heading.textContent = "Appointment #" + appointment.id;

        const patient = document.createElement("p");
        patient.textContent = "Patient: " + (appointment.patientName || "");

        const age = document.createElement("p");
        age.textContent = "Age: " + (appointment.age ?? "");

        const phone = document.createElement("p");
        phone.textContent = "Phone: " + (appointment.phone || "");

        const doctor = document.createElement("p");
        doctor.textContent =
            "Doctor: " + (appointment.doctorName || "Unknown");

        const specialization = document.createElement("p");
        specialization.textContent =
            "Specialization: " +
            (appointment.specialization || "Unknown");

        const date = document.createElement("p");
        date.textContent = "Date: " + (appointment.date || "");

        const time = document.createElement("p");
        time.textContent = "Time: " + displayTime(appointment.time);

        const fee = document.createElement("p");
        fee.textContent =
            "Consultation Fee: ₹" + (appointment.fee ?? "N/A");

        const cancelButton = document.createElement("button");
        cancelButton.className = "btn";
        cancelButton.textContent = "Cancel Appointment";
        cancelButton.style.backgroundColor = "#d32f2f";
        cancelButton.style.color = "white";
        cancelButton.style.marginTop = "15px";

        cancelButton.addEventListener("click", function () {
            cancelAppointment(appointment.id);
        });

        card.append(
            heading,
            patient,
            age,
            phone,
            doctor,
            specialization,
            date,
            time,
            fee,
            cancelButton
        );

        appointmentList.appendChild(card);
    });
}

// ========================================
// 8. BOOK AN APPOINTMENT
// ========================================

if (appointmentForm) {
    appointmentForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const patientName =
            document.getElementById("patient-name").value.trim();

        const age =
            Number(document.getElementById("patient-age").value);

        const phone =
            document.getElementById("patient-phone").value.trim();

        const doctorId = Number(doctorSelect.value);
        const date = appointmentDate.value;

        const rawTime =
            document.getElementById("appointment-time").value;

        const time = normalizeTime(rawTime);

        // Validate patient name
        if (patientName.length < 2) {
            alert("Please enter a valid patient name.");
            return;
        }

        // Validate age
        if (!Number.isInteger(age) || age < 1 || age > 120) {
            alert("Please enter a valid age between 1 and 120.");
            return;
        }

        // Validate phone number
        if (!/^[0-9]{10}$/.test(phone)) {
            alert("Please enter a valid 10-digit phone number.");
            return;
        }

        // Validate selected doctor
        const selectedDoctor = doctors.find(function (doctor) {
            return doctor.id === doctorId;
        });

        if (!selectedDoctor) {
            alert("Please select a valid doctor.");
            return;
        }

        // Validate appointment date
        if (!date || date < appointmentDate.min) {
            alert("Please select today or a future date.");
            return;
        }

        // Validate appointment time
        if (!/^\d{2}:\d{2}$/.test(time)) {
            alert("Please select a valid appointment time.");
            return;
        }

        // Prepare appointment for the Java backend
        const appointment = {
            id: Date.now(),
            patientName: patientName,
            age: age,
            phone: phone,
            doctorId: selectedDoctor.id,
            date: date,
            time: time
        };

        try {
            const response = await fetch(`${API_BASE}/appointments`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(appointment)
            });

            const result = await response.json();

            if (!response.ok) {
                alert(
                    result.error ||
                    result.message ||
                    "Unable to book appointment."
                );
                return;
            }

            alert(
                "Appointment booked successfully!\n\n" +
                "Patient: " + patientName + "\n" +
                "Doctor: " + selectedDoctor.name + "\n" +
                "Date: " + date + "\n" +
                "Time: " + displayTime(time) + "\n" +
                "Consultation Fee: ₹" + selectedDoctor.fee
            );

            appointmentForm.reset();
            setMinimumAppointmentDate();

            await fetchAppointments();

        } catch (error) {
            console.error("Appointment booking failed:", error);

            alert(
                "Could not connect to the Java server. " +
                "Make sure the server is running."
            );
        }
    });
}

// ========================================
// 9. CANCEL AN APPOINTMENT
// ========================================

async function cancelAppointment(appointmentId) {
    const confirmed = confirm(
        "Are you sure you want to cancel this appointment?"
    );

    if (!confirmed) return;

    try {
        const response = await fetch(
            `${API_BASE}/appointments?id=${encodeURIComponent(appointmentId)}`,
            {
                method: "DELETE"
            }
        );

        const result = await response.json();

        if (!response.ok) {
            alert(
                result.error ||
                result.message ||
                "Unable to cancel appointment."
            );
            return;
        }

        alert("Appointment cancelled successfully!");

        await fetchAppointments();

    } catch (error) {
        console.error("Cancellation failed:", error);

        alert(
            "Could not connect to the Java server. " +
            "Make sure the server is running."
        );
    }
}

// ========================================
// 10. INITIALIZE WEBSITE
// ========================================

setMinimumAppointmentDate();
fetchDoctors();
fetchAppointments();

console.log("MediMeet loaded successfully!");