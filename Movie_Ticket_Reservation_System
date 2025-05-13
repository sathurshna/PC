import java.util.*;
import java.io.*;
import java.time.*;
import java.time.format.*;
import java.nio.file.*;
import java.util.stream.Collectors;

// Custom Exception classes
class InvalidMovieCodeException extends Exception {
    public InvalidMovieCodeException(String code) {
        super("The movie code '" + code + "' is not valid. Please check available movies.");
    }
}

class InvalidDateTimeException extends Exception {
    public InvalidDateTimeException(String dateTime) {
        super("The date/time '" + dateTime + "' is not available. Please select from valid showtimes.");
    }
}

class InvalidTicketQuantityException extends Exception {
    public InvalidTicketQuantityException(int quantity) {
        super("Ticket quantity " + quantity + " is invalid. Must book between 1-10 tickets.");
    }
}

class OverbookingException extends Exception {
    public OverbookingException(int available, int requested) {
        super("Cannot book " + requested + " tickets. Only " + available + " seats remaining.");
    }
}

/**
 * Represents a Movie with relevant details
 */
class Movie {
    private String code;
    private String title;
    private String language;
    private String genre;

    public Movie(String code, String title, String language, String genre) {
        this.code = code.toUpperCase();
        this.title = title;
        this.language = language;
        this.genre = genre;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getLanguage() {
        return language;
    }

    public String getGenre() {
        return genre;
    }

    @Override
    public String toString() {
        return code + " - " + title + " (" + genre + ")";
    }
}

/**
 * Represents a Showtime with date, time, and pricing details
 */
class Showtime {
    public enum TimeOfDay {
        MORNING, AFTERNOON, EVENING
    }

    private Movie movie;
    private LocalDate date;
    private TimeOfDay timeOfDay;
    private int totalSeats;
    private int bookedSeats;
    private double ticketPrice;

    public Showtime(Movie movie, LocalDate date, TimeOfDay timeOfDay,
                   int totalSeats, int availableSeats, double ticketPrice) {
        this.movie = movie;
        this.date = date;
        this.timeOfDay = timeOfDay;
        this.totalSeats = totalSeats;
        this.bookedSeats = totalSeats - availableSeats;
        this.ticketPrice = ticketPrice;
    }

    public Movie getMovie() {
        return movie;
    }

    public boolean canBook(int numTickets) {
        return bookedSeats + numTickets <= totalSeats;
    }

    public void bookSeats(int numTickets) throws OverbookingException {
        if (!canBook(numTickets)) {
            throw new OverbookingException(getAvailableSeats(), numTickets);
        }
        bookedSeats += numTickets;
    }

    public int getAvailableSeats() {
        return totalSeats - bookedSeats;
    }

    public double getTicketPrice() {
        return ticketPrice;
    }

    public String getTimeString() {
        switch (timeOfDay) {
            case MORNING:
                return "Morning";
            case AFTERNOON:
                return "Afternoon";
            case EVENING:
                return "Evening";
            default:
                return "";
        }
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public String toString() {
        return date.format(DateTimeFormatter.ofPattern("M/d/yyyy")) + " " + getTimeString() +
               " - " + getAvailableSeats() + "/" + totalSeats + " seats available ($" +
               String.format("%.2f", ticketPrice) + ")";
    }
}

public class MovieTicketReservationGroup_CodingThunders {
    private List<Movie> movies;
    private List<Showtime> showtimes;

    public MovieTicketReservationGroup_CodingThunders(String csvFilePath) {
        movies = new ArrayList<>();
        showtimes = new ArrayList<>();
        loadDataFromCSV(csvFilePath);
    }

    /**
     * Loads data from a CSV file containing movie and showtime details
     */
    private void loadDataFromCSV(String filePath) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            // Skip header row if present
            int startIndex = lines.get(0).contains("Movie Code") ? 1 : 0;
            for (int i = startIndex; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] parts = line.split("\\|");
                if (parts.length >= 9) {
                    // Extract data from each column
                    String code = parts[0].trim();
                    String title = parts[1].trim();
                    String dateStr = parts[2].trim();
                    String timeStr = parts[3].trim();
                    int totalSeats = Integer.parseInt(parts[4].trim());
                    int availableSeats = Integer.parseInt(parts[5].trim());
                    double ticketPrice = Double.parseDouble(parts[6].trim());
                    String language = parts[7].trim();
                    String genre = parts[8].trim();

                    // Parse the date
                    LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("M/d/yyyy"));
                    
                    // Parse the time
                    Showtime.TimeOfDay timeOfDay = parseTimeOfDay(timeStr);

                    // Find or create the movie
                    Movie movie = movies.stream()
                            .filter(m -> m.getCode().equals(code))
                            .findFirst()
                            .orElseGet(() -> {
                                Movie newMovie = new Movie(code, title, language, genre);
                                movies.add(newMovie);
                                return newMovie;
                            });

                    // Create the showtime
                    showtimes.add(new Showtime(movie, date, timeOfDay,
                            totalSeats, availableSeats, ticketPrice));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading data from CSV: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error parsing data: " + e.getMessage());
        }
    }

    private Showtime.TimeOfDay parseTimeOfDay(String timeStr) {
        switch (timeStr.toUpperCase()) {
            case "MORNING":
                return Showtime.TimeOfDay.MORNING;
            case "AFTERNOON":
                return Showtime.TimeOfDay.AFTERNOON;
            case "EVENING":
                return Showtime.TimeOfDay.EVENING;
            default:
                throw new IllegalArgumentException("Invalid time of day: " + timeStr);
        }
    }

    // Display available movies and showtimes
    public void displayMovies() {
        System.out.println("\nAvailable Movies:");
        if (movies.isEmpty()) {
            System.out.println("No movies available.");
        } else {
            movies.forEach(movie -> {
                System.out.println(movie);
                showtimes.stream()
                        .filter(st -> st.getMovie().getCode().equals(movie.getCode()))
                        .forEach(System.out::println);
                System.out.println();
            });
        }
    }

    // Make a reservation
    public void makeReservation(Scanner scanner) {
        System.out.println("\nEnter the Movie Code from the available movies: ");
        String movieCode = scanner.nextLine().toUpperCase();
        Movie selectedMovie = movies.stream()
                .filter(movie -> movie.getCode().equals(movieCode))
                .findFirst()
                .orElse(null);

        if (selectedMovie == null) {
            System.out.println("Invalid Movie Code. Please try again.");
            return;
        }

        System.out.println("You selected: " + selectedMovie.getTitle());
        System.out.println("\nAvailable Showtimes:");
        
        // Show available showtimes for the selected movie
        List<Showtime> availableShowtimes = showtimes.stream()
                .filter(st -> st.getMovie().equals(selectedMovie))
                .collect(Collectors.toList());

        if (availableShowtimes.isEmpty()) {
            System.out.println("No available showtimes for this movie.");
            return;
        }

        availableShowtimes.forEach(System.out::println);

        // Get the user's choice for the showtime
        System.out.println("\nEnter the showtime (e.g., 4/1/2025 Morning): ");
        String showtimeInput = scanner.nextLine().trim();

        // Parse the date and time for comparison
        Showtime selectedShowtime = availableShowtimes.stream()
                .filter(st -> {
                    String formattedDate = st.getDate().format(DateTimeFormatter.ofPattern("M/d/yyyy"));
                    String formattedShowtime = formattedDate + " " + st.getTimeString();
                    return formattedShowtime.equalsIgnoreCase(showtimeInput);
                })
                .findFirst()
                .orElse(null);

        if (selectedShowtime == null) {
            System.out.println("Invalid showtime. Please try again.");
            return;
        }

        System.out.println("You selected the showtime: " + selectedShowtime);

        // Get the number of tickets to book
        System.out.println("Enter the number of tickets to book (1-10): ");
        int numTickets;
        try {
            numTickets = scanner.nextInt();
            scanner.nextLine(); // Consume the newline
        } catch (InputMismatchException e) {
            System.out.println("Please enter a valid number.");
            scanner.nextLine(); // Clear the invalid input
            return;
        }

        if (numTickets < 1 || numTickets > 10) {
            System.out.println("You can only book between 1 and 10 tickets.");
            return;
        }

        // Try to book the seats
        try {
            selectedShowtime.bookSeats(numTickets);
            System.out.println("Successfully booked " + numTickets + " tickets for the showtime: " + selectedShowtime);
        } catch (OverbookingException e) {
            System.out.println(e.getMessage());
        }
    }

    // Main method
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java MovieTicketReservationGroup_CodingThunders <movies_csv_file>");
            return;
        }

        MovieTicketReservationGroup_CodingThunders cinema = new MovieTicketReservationGroup_CodingThunders(args[0]);
        cinema.start();
    }

    // Main menu and interaction
    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to CodingThunders Cinema!");

        while (true) {
            try {
                System.out.println("\nMain Menu:");
                System.out.println("1. View Movies and Showtimes");
                System.out.println("2. Make a Reservation");
                System.out.println("3. Exit");
                System.out.print("Enter your choice: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        displayMovies();
                        break;
                    case 2:
                        makeReservation(scanner);
                        break;
                    case 3:
                        System.out.println("Thank you for using our system!");
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Please enter a valid number.");
                scanner.nextLine(); // Clear invalid input
            }
        }
    }
}
